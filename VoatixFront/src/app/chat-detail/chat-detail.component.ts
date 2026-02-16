import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../service/authorization/auth.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { IdeaService } from '../service/idea.service';
import { WebSocketService } from '../service/websocket.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface Message {
  text: string;
  sender: string;
  receiver: string;
  isRead: boolean;
  date: string;
  files: any[];
}

@Component({
  selector: 'chat-detail-component',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, HttpClientModule],
  templateUrl: './chat-detail.component.html',
  styleUrls: ['./chat-detail.component.scss']
})
export class ChatDetailComponent implements OnInit, OnDestroy, OnChanges {
  @Input() chatNickname: string = '';
  @Input() currentUsername: string = '';
  @Input() avatarKey?: string;
  @Input() wsService: WebSocketService | null = null;
  @Input() senderName?: string; // Имя отправителя из списка чатов

  messages: Message[] = [];
  messageText: string = '';
  loading: boolean = false;
  avatarUrl: SafeUrl | string = '/assets/icons/default-avatar.png';
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
    private ideaService: IdeaService
  ) {}

  ngOnInit(): void {
    if (this.chatNickname) {
      this.loadMessages();
    }

    // Subscribe to incoming messages stream once; handling of which chat receives the message
    // is done inside the subscription (checks this.chatNickname).
    this.subscribeToMessages();
    if (this.avatarKey) {
      this.loadAvatarImage();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chatNickname'] && !changes['chatNickname'].firstChange) {
      // Chat changed: reset messages and reload for the new chat
      this.messages = [];
      this.loadMessages();
    }
    if (changes['avatarKey'] && !changes['avatarKey'].firstChange) {
      if (this.avatarKey) this.loadAvatarImage();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadMessages(): void {
    this.loading = true;
    const url = `http://localhost:8080/api/messages/chat/${this.chatNickname}`;
    
    this.http.get<Message[]>(url, { withCredentials: true }).subscribe({
      next: (res) => {
        console.log('Messages loaded:', res);
        // Reverse to show newest at bottom
        this.messages = (res || []).reverse();
        this.loading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Failed to load messages', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  subscribeToMessages(): void {
    if (!this.wsService) {
      console.warn('WebSocketService not provided');
      return;
    }

    // Subscribe to incoming messages from WebSocket
    this.wsService.getMessages$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        (newMessage: Message) => {
          console.log('📩 Message received in chat-detail:', newMessage);
          console.log('📋 Current chat nickname:', this.chatNickname);
          console.log('📋 Current username:', this.currentUsername);
          console.log('📋 Message sender:', newMessage.sender);
          console.log('📋 Message receiver:', newMessage.receiver);
          console.log('📋 Sender name from chat list:', this.senderName);
          
          // Try to determine if message belongs to this chat
          let belongsToThisChat = false;
          
          // Method 1: Direct comparison with sender/receiver
          if (newMessage.sender && newMessage.receiver) {
            belongsToThisChat = 
              newMessage.sender === this.chatNickname || 
              newMessage.receiver === this.chatNickname;
            console.log('✅ Method 1 (direct sender/receiver comparison):', belongsToThisChat);
          }
          
          // Method 2: If sender/receiver are null, use senderName from chat list
          if (!belongsToThisChat && !newMessage.sender && !newMessage.receiver && this.senderName) {
            belongsToThisChat = this.senderName === this.chatNickname;
            console.log('✅ Method 2 (using senderName from chat list):', belongsToThisChat);
          }
          
          // Method 3: Fallback - accept all messages if we're in a chat (but log warning)
          if (!belongsToThisChat && !newMessage.sender && !newMessage.receiver) {
            console.warn('⚠️ Cannot determine message ownership, but accepting it (fallback mode)');
            belongsToThisChat = true; // Accept it anyway
          }
          
          if (belongsToThisChat) {
            console.log('✅ Adding message to this chat');
            this.messages.push(newMessage);
            
            // Mark as read on server
            if (this.wsService) {
              console.log('📭 Marking as read on server');
              this.wsService.markAsRead(this.chatNickname);
              // Notify parent that message was received in open chat
              this.wsService.notifyMessageReceivedInChat(this.chatNickname);
            }
            
            this.cdr.detectChanges();
            this.scrollToBottom();
          } else {
            console.log('❌ Message does not belong to this chat, ignoring');
          }
        },
        (error) => console.error('Error receiving messages:', error)
      );

    console.log('✅ Subscribed to messages for chat:', this.chatNickname);
  }

  sendMessage(): void {
    if (!this.messageText.trim() || !this.wsService || !this.wsService.isConnected()) {
      console.warn('Cannot send message: text empty or WebSocket not connected');
      return;
    }

    // Send via WebSocket
    this.wsService.sendMessage(this.chatNickname, this.messageText);

    // Add to local messages immediately for better UX
    const newMessage: Message = {
      text: this.messageText.trim(),
      sender: this.currentUsername,
      receiver: this.chatNickname,
      isRead: false,
      date: new Date().toISOString(),
      files: []
    };
    this.messages.push(newMessage);
    this.messageText = '';
    this.cdr.detectChanges();
    this.scrollToBottom();
  }

  loadAvatarImage(): void {
    if (!this.avatarKey) return;
    
    const url = this.ideaService.getFileViewUrl(this.avatarKey);
    this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        this.avatarUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load avatar', err);
      }
    });
  }

  getMessageClass(message: Message): string {
    return message.sender === this.currentUsername ? 'message-sent' : 'message-received';
  }

  formatDate(date: string): string {
    try {
      const msgDate = new Date(date);
      const today = new Date();
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);

      if (msgDate.toDateString() === today.toDateString()) {
        return msgDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
      } else if (msgDate.toDateString() === yesterday.toDateString()) {
        return 'Вчера ' + msgDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
      } else {
        return msgDate.toLocaleDateString('ru-RU', { month: 'short', day: 'numeric' }) + ' ' + 
               msgDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
      }
    } catch {
      return date;
    }
  }

  scrollToBottom(): void {
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container');
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 0);
  }

  markMessagesAsRead(): void {
    if (!this.wsService || !this.wsService.isConnected()) {
      console.warn('WebSocket not connected');
      return;
    }

    this.wsService.markAsRead(this.chatNickname);
  }

  onMessageInput(): void {
    // Optional: Handle input events
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
