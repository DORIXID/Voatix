import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpEventType } from '@angular/common/http';
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
  
  // File upload
  selectedFiles: Array<{
    file: File;
    name: string;
    key: string;
    progress: number;
    status: 'pending' | 'uploading' | 'done' | 'error';
  }> = [];
  maxFiles = 5;

  // Image modal
  selectedImageUrl: SafeUrl | null = null;
  isImageModalOpen = false;
  imageUrls: Record<string, string> = {}; // Store blob URLs as strings
  
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
    public ideaService: IdeaService
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
        // Reverse to show newest at bottom
        this.messages = (res || []).reverse();
        // Load all images from messages
        this.messages.forEach((message, index) => {
          this.loadMessageImages(message);
        });
        this.loading = false;
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('❌ Failed to load messages', err);
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
          
          // Try to determine if message belongs to this chat
          let belongsToThisChat = false;
          
          // Method 1: Direct comparison - message FROM this chat user TO current user
          if (newMessage.sender && newMessage.receiver) {
            // For receiving: sender is the chat user, receiver is current user (us)
            belongsToThisChat = newMessage.sender === this.chatNickname;
          }
          
          // Method 2: If sender/receiver are null, assume message is from the current chat
          // This happens when backend sends messages without explicit sender/receiver fields
          if (!belongsToThisChat && !newMessage.sender && !newMessage.receiver) {
            // Accept message from the current chat since there's no other way to identify it
            belongsToThisChat = true;
          }
          
          if (belongsToThisChat) {
            this.messages.push(newMessage);
            
            // Load images from new message
            this.loadMessageImages(newMessage);
            
            // Mark as read on server
            if (this.wsService) {
              this.wsService.markAsRead(this.chatNickname);
              // Notify parent that message was received in open chat
              this.wsService.notifyMessageReceivedInChat(this.chatNickname);
            }
            
            // Force UI update - call multiple times to ensure update
            this.cdr.detectChanges();
            
            // Schedule another update after a short delay
            setTimeout(() => {
              this.cdr.detectChanges();
            }, 100);
            
            this.scrollToBottom();
          }
        },
        (error) => console.error('Error receiving messages:', error)
      );
  }

  sendMessage(): void {
    if ((!this.messageText.trim() && this.selectedFiles.length === 0) || !this.wsService || !this.wsService?.isConnected()) {
      console.warn('Cannot send message: empty message and no files, or WebSocket not connected');
      return;
    }

    // Prepare file keys for sending
    const fileKeys = this.selectedFiles
      .filter(f => f.status === 'done' && f.key)
      .map(f => f.key);

    // Send via WebSocket
    this.wsService?.sendMessage(this.chatNickname, this.messageText.trim(), fileKeys);

    // Add to local messages immediately for better UX
    const newMessage: Message = {
      text: this.messageText.trim(),
      sender: this.currentUsername,
      receiver: this.chatNickname,
      isRead: false,
      date: new Date().toISOString(),
      files: fileKeys
    };
    this.messages.push(newMessage);
    
    // Load images for sent message
    this.loadMessageImages(newMessage);
    this.messageText = '';
    this.selectedFiles = [];
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

  // File upload methods
  triggerFileInput(): void {
    const fileInput = document.getElementById('chatFileInput') as HTMLInputElement;
    fileInput?.click();
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;
    const filesArr = Array.from(input.files);
    const remaining = this.maxFiles - this.selectedFiles.length;
    if (remaining <= 0) {
      alert(`Можно загрузить не более ${this.maxFiles} файлов`);
      input.value = '';
      return;
    }
    const files = filesArr.slice(0, remaining);
    this.handleFiles(files);
    if (filesArr.length > files.length) {
      alert(`Добавлено ${files.length} файла(ов). Можно загрузить максимум ${this.maxFiles}.`);
    }
    input.value = '';
  }

  handleFiles(files: File[]): void {
    const remaining = this.maxFiles - this.selectedFiles.length;
    if (remaining <= 0) {
      alert(`Можно загрузить не более ${this.maxFiles} файлов`);
      return;
    }
    const toAdd = files.slice(0, remaining);
    toAdd.forEach((file) => {
      const item = { file, name: file.name, key: '', progress: 0, status: 'uploading' as const };
      this.selectedFiles.push(item);
      const idx = this.selectedFiles.length - 1;

      this.ideaService.uploadFile(file).subscribe({
        next: (event: any) => {
          if (event.type === HttpEventType.Response) {
            const res = event.body;
            const key = res?.key ?? res?.name ?? file.name;
            this.selectedFiles[idx].key = key;
            this.selectedFiles[idx].status = 'done';
            this.selectedFiles[idx].progress = 100;
          } else if (event.type === HttpEventType.UploadProgress) {
            const loaded = event.loaded ?? 0;
            const total = event.total ?? loaded;
            const percent = Math.round((loaded / total) * 100);
            this.selectedFiles[idx].progress = percent;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Ошибка загрузки файла', file.name, err);
          this.selectedFiles[idx].status = 'error';
          this.cdr.detectChanges();
        }
      });
    });
  }

  removeFile(index: number): void {
    const item = this.selectedFiles[index];
    if (!item) return;

    if (item.status === 'uploading') {
      alert('Файл ещё загружается — дождитесь завершения или попробуйте позже.');
      return;
    }

    const key = item.key;
    if (key) {
      this.ideaService.deleteFile(key).subscribe({
        next: () => {
          this.selectedFiles.splice(index, 1);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Ошибка удаления файла', key, err);
          this.selectedFiles.splice(index, 1);
          this.cdr.detectChanges();
        }
      });
    } else {
      this.selectedFiles.splice(index, 1);
      this.cdr.detectChanges();
    }
  }

  hasUploadingFiles(): boolean {
    return this.selectedFiles.some(f => f.status === 'uploading');
  }

  canSendMessage(): boolean {
    const hasText = this.messageText.trim().length > 0;
    const hasFiles = this.selectedFiles.some(f => f.status === 'done');
    const isUploading = this.hasUploadingFiles();
    return (hasText || hasFiles) && !isUploading;
  }

  // Image display methods
  isImageFile(fileKey: string): boolean {
    if (!fileKey) {
      return false;
    }
    
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg'];
    const lowerKey = fileKey.toLowerCase();
    
    // Check if any image extension is in the key
    const isImage = imageExtensions.some(ext => lowerKey.includes(ext));
    
    return isImage;
  }

  loadMessageImages(message: Message): void {
    if (!message.files || message.files.length === 0) {
      return;
    }
    
    message.files.forEach(fileKey => {
      
      if (this.isImageFile(fileKey)) {
        if (this.imageUrls[fileKey]) {
          this.cdr.detectChanges();
          return;
        }
        
        const url = this.ideaService.getFileViewUrl(fileKey);
        
        this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
          next: (blob) => {
            const blobUrl = URL.createObjectURL(blob);
            this.imageUrls[fileKey] = blobUrl;
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('❌ Error loading image', fileKey, err);
            // Still try to display via API URL - will be loaded by fallback in template
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  openImageModal(imageUrl: string | null | undefined): void {
    if (!imageUrl) return;
    this.selectedImageUrl = this.sanitizer.bypassSecurityTrustUrl(imageUrl);
    this.isImageModalOpen = true;
  }

  closeImageModal(): void {
    this.selectedImageUrl = null;
    this.isImageModalOpen = false;
  }

  getMessageImageUrl(fileKey: string): string | null {
    const url = this.imageUrls[fileKey];
    return url || null;
  }
}
