import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../service/authorization/auth.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { IdeaService } from '../service/idea.service';
import { ChatDetailComponent } from '../chat-detail/chat-detail.component';
import { WebSocketService } from '../service/websocket.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Chat } from '../service/interfaces/chat.interface';

@Component({
  selector: 'messages-form-component',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    CommonModule,
    HttpClientModule,
    ChatDetailComponent],
  templateUrl: './messages-form.component.html',
  styleUrls: ['./messages-form.component.scss']
})
export class MessagesFormComponent implements OnInit, OnDestroy {

  chats: Chat[] = [];
  page = 0;
  limit = 10;
  totalPages = 0;
  loading = false;
  avatarUrls: Record<string, SafeUrl> = {};
  selectedChat: Chat | null = null;
  currentUsername: string = '';
  currentUserId?: number;
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
    private ideaService: IdeaService,
    private wsService: WebSocketService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.getCurrentUsername();
    this.loadChats();
    this.connectWebSocket();
    this.subscribeToChatsUpdates();
    this.subscribeToMessageReceivedInChat();
    
    // Handle chat parameter from query params
    this.route.queryParamMap.subscribe(params => {
      const chatNickname = params.get('chat');
      const userIdStr = params.get('userId');
      const userId = userIdStr ? parseInt(userIdStr, 10) : undefined;
      if (chatNickname) {
        // Try to find and select chat, if not in list yet, it will be selected when loaded
        this.selectChatByNickname(chatNickname, userId);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // Не отключаем WebSocket здесь - он управляется сервисом для всего приложения
  }

  loadChats(): void {
    this.loading = true;
    const payload = {
      page: this.page,
      limit: this.limit
    };

    this.http.post<any>('http://localhost:8080/api/messages/chats', payload, { withCredentials: true }).subscribe({
      next: res => {
        this.chats = res.content || [];
        this.totalPages = res.totalPages || 0;
        console.log('🔍 Loaded chats:', this.chats);
        // Verify userId in chat objects
        this.chats.forEach((chat, idx) => {
          console.log(`Chat ${idx}:`, {userNickname: chat.userNickname, userId: chat.userId, avatarId: chat.avatarId});
        });
        // Load avatars for all chats - handle both old (avatarKey) and new (avatarId/fileId) formats
        this.chats.forEach(chat => {
          const avatarId = chat.avatarId || chat.fileId || chat.avatarKey;
          if (avatarId) {
            const avatarIdStr = typeof avatarId === 'number' ? avatarId.toString() : avatarId;
            if (!this.avatarUrls[avatarIdStr]) {
              this.loadImage(avatarIdStr);
            }
          }
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to load chats', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  connectWebSocket(): void {
    const token = localStorage.getItem('jwt') || localStorage.getItem('token') || localStorage.getItem('auth_token');
    if (!token) {
      console.warn('No token found in localStorage');
      return;
    }

    this.wsService.connect(token).catch((err) => {
      console.error('Failed to connect WebSocket:', err);
    });
  }

  /**
   * Подписаться на обновления чатов из WebSocket
   */
  subscribeToChatsUpdates(): void {
    this.wsService.getChatsUpdates$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        (update) => {
          this.loadChats(); // Перезагрузить список чатов
        },
        (error) => console.error('Error in chats updates:', error)
      );
  }

  /**
   * Подписаться на событие получения сообщения в открытом чате
   */
  subscribeToMessageReceivedInChat(): void {
    this.wsService.getMessageReceivedInChat$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        (event) => {
          if (!event) return;
          // Find the chat and clear unread count
          // Try to find by userId first (if available), then by nickname
          const chatId = event.chatId || event.userId;
          let chat: Chat | undefined;
          
          if (chatId) {
            chat = this.chats.find(c => c.userId === chatId);
          }
          
          if (!chat && event.chatNickname) {
            chat = this.chats.find(c => c.userNickname === event.chatNickname);
          }
          
          if (chat) {
            chat.unreadCount = 0;
            this.cdr.detectChanges();
            console.log('✓ Cleared unread count for chat:', chat.userId || chat.userNickname);
          }
        },
        (error) => console.error('Error in message received event:', error)
      );
  }

  loadImage(key: string): void {
    const url = this.ideaService.getFileViewUrl(key);
    this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.avatarUrls[key] = safeUrl;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка загрузки изображения', key, err);
        this.cdr.detectChanges();
      }
    });
  }

  getAvatarUrl(key?: string): SafeUrl | string {
    if (!key) return '/assets/icons/default-avatar.png';
    if (this.avatarUrls[key]) {
      return this.avatarUrls[key];
    }
    // If avatar is not yet loaded, start loading it
    if (key) {
      this.loadImage(key);
    }
    return '/assets/icons/default-avatar.png';
  }

  next(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadChats();
    }
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.loadChats();
    }
  }

  getCurrentUsername(): void {
    const token = localStorage.getItem('jwt') || localStorage.getItem('token') || localStorage.getItem('auth_token');
    if (token) {
      try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(atob(payload));
        this.currentUsername = decoded.sub || '';
        // NEW: Extract userId if available in token
        this.currentUserId = decoded.userId || decoded.id || undefined;
      } catch (e) {
        console.error('Failed to decode token:', e);
      }
    }
  }

  selectChat(chat: Chat): void {
    this.selectedChat = chat;

    // Clear unread count locally and notify server that chat is read
    if (chat && (chat as any).unreadCount && (chat as any).unreadCount > 0) {
      (chat as any).unreadCount = 0;
      this.cdr.detectChanges();
    }

    if (this.wsService && chat.userId) {
      try {
        // ALWAYS use userId - it's the only reliable identifier
        this.wsService.markAsRead({ userId: chat.userId });
        console.log('✓ Marked chat as read with userId:', chat.userId);
      } catch (e) {
        console.error('Failed to mark chat as read via WebSocketService', e);
      }
    } else if (!chat.userId) {
      console.warn('⚠️ Chat does not have userId - cannot mark as read');
    }
  }

  closeChat(): void {
    this.selectedChat = null;
  }

  /**
   * Передать WebSocket сервис дочерней компоненте
   */
  getWebSocketService(): WebSocketService {
    return this.wsService;
  }

  /**
   * Выбрать чат по userId (основной способ) или никнейму (fallback)
   */
  selectChatByNickname(nickname: string, userId?: number): void {
    // Try to find chat by userId first (most reliable)
    let chat: Chat | undefined;
    
    if (userId) {
      chat = this.chats.find(c => c.userId === userId);
      console.log(`🔍 Looking for chat by userId ${userId}:`, chat ? 'found' : 'not found');
    }
    
    // Fall back to nickname search if not found by userId
    if (!chat) {
      chat = this.chats.find(c => c.userNickname === nickname);
      console.log(`🔍 Looking for chat by nickname ${nickname}:`, chat ? 'found' : 'not found');
    }
    
    if (chat) {
      this.selectChat(chat);
    } else {
      // If not found, create a temporary chat object
      // Use userId as primary identifier
      if (userId) {
        const newChat: Chat = {
          userNickname: nickname,
          userId: userId
        };
        console.log('📌 Creating new chat session with userId:', userId);
        this.selectChat(newChat);
      } else {
        console.warn('⚠️ Cannot select chat without userId or nickname');
      }
    }
  }
}