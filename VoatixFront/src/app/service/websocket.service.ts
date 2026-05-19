import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

interface StompMessage {
  body: string;
}

interface MessageReceivedEvent {
  chatNickname?: string;
  chatId?: number;
  userId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stomp: any = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messageSubject$ = new BehaviorSubject<any>(null);
  private chatsUpdateSubject$ = new BehaviorSubject<any>(null);
  private readNotificationSubject$ = new BehaviorSubject<any>(null);
  private messageReceivedInChatSubject$ = new BehaviorSubject<MessageReceivedEvent | null>(null);

  constructor() {}

  /**
   * Подключиться к WebSocket
   */
  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stomp && this.stomp.connected) {
        console.log('WebSocket already connected');
        resolve();
        return;
      }

      if (typeof (window as any).Stomp === 'undefined') {
        console.error('STOMP library not loaded');
        reject('STOMP library not loaded');
        return;
      }

      console.log('Attempting WebSocket connection to ws://localhost:8080/ws');
      
      const socket = new WebSocket('ws://localhost:8080/ws');
      
      socket.addEventListener('open', () => {
        console.log('WebSocket socket opened');
      });
      
      socket.addEventListener('error', (event) => {
        console.error('WebSocket socket error:', event);
      });
      
      socket.addEventListener('close', () => {
        console.log('WebSocket socket closed');
        this.connected$.next(false);
      });
      
      this.stomp = (window as any).Stomp.over(socket);
      
      // Enable debug mode for better logging
      this.stomp.debug = (msg: string) => {
        console.log('[STOMP]:', msg);
      };

      this.stomp.connect(
        { Authorization: 'Bearer ' + token },
        (frame: any) => {
          console.log('✓ WebSocket connected successfully, version:', frame.version);
          this.connected$.next(true);

          const username = this.getUsernameFromToken(token);
          const userId = this.getUserIdFromToken(token);
          console.log('Current username:', username, 'userId:', userId);

          // Subscribe to chats updates (which may include messages)
          this.stomp.subscribe('/user/queue/chats', (msg: StompMessage) => {
            try {
              const data = JSON.parse(msg.body);
              console.log('📨 Chat update received:', data);
              
              // ALWAYS send chat update event (for list refresh)
              this.chatsUpdateSubject$.next(data);
              
              // ADDITIONALLY, if it has text OR files, also send it as a message
              const hasText = data.text && (data.text.trim().length > 0);
              const hasFiles = data.files && data.files.length > 0;
              const isMessage = hasText || hasFiles;
              if (isMessage) {
                this.messageSubject$.next(data);
              }
            } catch (e) {
              console.error('Error parsing chats event:', e);
              this.chatsUpdateSubject$.next(msg.body);
            }
          });

          // Subscribe to incoming messages for current user (direct channel using userId)
          if (userId) {
            console.log(`Subscribing to /user/queue/chat.${userId}`);
            this.stomp.subscribe(`/user/queue/chat.${userId}`, (msg: StompMessage) => {
              try {
                const message = JSON.parse(msg.body);
                console.log('💬 Direct message received:', message);
                this.messageSubject$.next(message);
              } catch (e) {
                console.error('Error parsing message:', e);
              }
            });

            // Subscribe to read notifications
            this.stomp.subscribe('/user/queue/read', (msg: StompMessage) => {
              try {
                const notification = JSON.parse(msg.body);
                console.log('✓ Read notification:', notification);
                this.readNotificationSubject$.next(notification);
              } catch (e) {
                console.error('Error parsing read notification:', e);
              }
            });
          }

          resolve();
        },
        (error: any) => {
          console.error('✗ WebSocket connection error:', error);
          this.connected$.next(false);
          reject(error);
        }
      );
    });
  }

  /**
   * Отправить сообщение
   */
  sendMessage(receiver: string | number, text: string, files: any[] = []): void {
    if (!this.stomp || !this.stomp.connected) {
      console.warn('✗ WebSocket not connected, cannot send message. Connected:', this.stomp?.connected, 'STOMP:', this.stomp);
      return;
    }

    // If receiver is a number, use it as receiverId; otherwise treat as nickname for backwards compatibility
    const messagePayload = typeof receiver === 'number' 
      ? {
          text: text.trim(),
          receiverId: receiver,
          files: files
        }
      : {
          text: text.trim(),
          receiver: receiver,
          files: files
        };

    console.log('📤 Sending message via WebSocket:', messagePayload);
    this.stomp.send('/app/messages.send', {}, JSON.stringify(messagePayload));
  }

  /**
   * Отметить сообщения как прочитанные
   */
  markAsRead(receiver: string | { userId: number }): void {
    if (!this.stomp || !this.stomp.connected) {
      console.warn('WebSocket not connected, cannot mark as read');
      return;
    }

    // ALWAYS use userId if available, never send username
    const dto = typeof receiver === 'string' 
      ? { userId: parseInt(receiver, 10) } // Try to parse as number
      : { userId: receiver.userId };

    console.log('📨 Marking as read:', dto);
    this.stomp.send('/app/messages.read', {}, JSON.stringify(dto));
  }

  /**
   * Получить Observable входящих сообщений
   */
  getMessages$(): Observable<any> {
    return this.messageSubject$.asObservable();
  }

  /**
   * Получить Observable обновлений чатов
   */
  getChatsUpdates$(): Observable<any> {
    return this.chatsUpdateSubject$.asObservable();
  }

  /**
   * Получить Observable уведомлений о прочтении
   */
  getReadNotifications$(): Observable<any> {
    return this.readNotificationSubject$.asObservable();
  }

  /**
   * Получить Observable событий "сообщение получено в открытом чате"
   */
  getMessageReceivedInChat$(): Observable<MessageReceivedEvent> {
    return this.messageReceivedInChatSubject$.asObservable().pipe(
      filter(event => event !== null)
    ) as Observable<MessageReceivedEvent>;
  }

  /**
   * Отправить сигнал, что сообщение получено в открытом чате
   */
  notifyMessageReceivedInChat(chatNickname: string, userId?: number): void {
    this.messageReceivedInChatSubject$.next({ chatNickname, userId });
  }

  /**
   * Получить статус подключения
   */
  isConnected$(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  /**
   * Проверить, подключен ли WebSocket
   */
  isConnected(): boolean {
    return this.stomp && (this.stomp.connected || (this.stomp.ws && this.stomp.ws.readyState === 1));
  }

  /**
   * Отключиться от WebSocket
   */
  disconnect(): Promise<void> {
    return new Promise((resolve) => {
      if (this.stomp && this.stomp.connected) {
        this.stomp.disconnect(() => {
          this.connected$.next(false);
          this.stomp = null;
          resolve();
        });
      } else {
        resolve();
      }
    });
  }

  /**
   * Извлечь username из JWT токена
   */
  private getUsernameFromToken(token: string): string {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded.sub || '';
    } catch (e) {
      console.error('Failed to decode token:', e);
      return '';
    }
  }

  /**
   * Извлечь userId из JWT токена
   */
  private getUserIdFromToken(token: string): number | undefined {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded.userId || decoded.id || undefined;
    } catch (e) {
      console.error('Failed to decode token for userId:', e);
      return undefined;
    }
  }
}
