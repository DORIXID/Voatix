import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../service/authorization/auth.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { IdeaService } from '../service/idea.service';
import { ChatDetailComponent } from '../chat-detail/chat-detail.component';
import { WebSocketService } from '../service/websocket.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

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

  chats: any[] = [];
  page = 0;
  limit = 10;
  totalPages = 0;
  loading = false;
  avatarUrls: Record<string, SafeUrl> = {};
  selectedChat: any = null;
  currentUsername: string = '';
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
    private ideaService: IdeaService,
    private wsService: WebSocketService
  ) { }

  ngOnInit(): void {
    this.getCurrentUsername();
    this.loadChats();
    this.connectWebSocket();
    this.subscribeToChatsUpdates();
    this.subscribeToMessageReceivedInChat();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // Не отключаем WebSocket здесь - он управляется сервисом для всего приложения
  }

  loadChats(): void {
    this.loading = true;
    const params = new HttpParams()
      .set('page', String(this.page))
      .set('limit', String(this.limit));

    this.http.get<any>('http://localhost:8080/api/messages/chats', { params, withCredentials: true }).subscribe({
      next: res => {
        console.log('chats response', res);
        this.chats = res.content || [];
        this.totalPages = res.totalPages || 0;
        // Load avatars for all chats
        this.chats.forEach(chat => {
          if (chat.avatarKey && !this.avatarUrls[chat.avatarKey]) {
            this.loadImage(chat.avatarKey);
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
          console.log('🔄 Chat list update received:', update);
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
          console.log('🔔 Message received in chat:', event.chatNickname);
          // Find the chat and clear unread count
          const chat = this.chats.find(c => c.userNickname === event.chatNickname);
          if (chat) {
            console.log('📭 Clearing unread count for chat:', chat.userNickname);
            chat.unreadCount = 0;
            this.cdr.detectChanges();
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
        console.log('Current username:', this.currentUsername);
      } catch (e) {
        console.error('Failed to decode token:', e);
      }
    }
  }

  selectChat(chat: any): void {
    this.selectedChat = chat;
    console.log('Selected chat:', chat);

    // Clear unread count locally and notify server that chat is read
    if (chat && chat.unreadCount && chat.unreadCount > 0) {
      chat.unreadCount = 0;
      this.cdr.detectChanges();
    }

    if (this.wsService) {
      try {
        this.wsService.markAsRead(chat.userNickname);
      } catch (e) {
        console.error('Failed to mark chat as read via WebSocketService', e);
      }
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
}