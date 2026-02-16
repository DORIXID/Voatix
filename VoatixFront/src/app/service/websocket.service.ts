import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

interface StompMessage {
  body: string;
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
  private messageReceivedInChatSubject$ = new BehaviorSubject<{ chatNickname: string } | null>(null);

  constructor() {}

  /**
   * Подключиться к WebSocket
   */
  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.stomp && this.stomp.connected) {
        console.log('Already connected');
        resolve();
        return;
      }

      if (typeof (window as any).Stomp === 'undefined') {
        console.error('STOMP library not loaded');
        reject('STOMP library not loaded');
        return;
      }

      const socket = new WebSocket('ws://localhost:8080/ws');
      this.stomp = (window as any).Stomp.over(socket);

      this.stomp.connect(
        { Authorization: 'Bearer ' + token },
        (frame: any) => {
          console.log('✅ WS CONNECTED:', frame);
          this.connected$.next(true);

          const username = this.getUsernameFromToken(token);

          // Subscribe to chats updates (which may include messages)
          this.stomp.subscribe('/user/queue/chats', (msg: StompMessage) => {
            console.log('🔔 Chats channel event:', msg.body);
            try {
              const data = JSON.parse(msg.body);
              console.log('📊 Event data keys:', Object.keys(data));
              console.log('📊 Event data:', data);
              
              // ALWAYS send chat update event (for list refresh)
              console.log('🔄 Sending CHAT UPDATE event');
              this.chatsUpdateSubject$.next(data);
              
              // ADDITIONALLY, if it has text, also send it as a message
              const isMessage = data.text && (data.text.trim().length > 0);
              if (isMessage) {
                console.log('📩 Also sending as MESSAGE');
                this.messageSubject$.next(data);
              }
            } catch (e) {
              console.error('Error parsing chats event:', e);
              this.chatsUpdateSubject$.next(msg.body);
            }
          });

          // Subscribe to incoming messages for current user (direct channel)
          if (username) {
            this.stomp.subscribe(`/user/queue/chat.${username}`, (msg: StompMessage) => {
              console.log('📩 Incoming message via user queue:', msg.body);
              try {
                const message = JSON.parse(msg.body);
                this.messageSubject$.next(message);
              } catch (e) {
                console.error('Error parsing message:', e);
              }
            });

            // Subscribe to read notifications
            this.stomp.subscribe('/user/queue/read', (msg: StompMessage) => {
              console.log('📘 Read notification:', msg.body);
              try {
                const notification = JSON.parse(msg.body);
                this.readNotificationSubject$.next(notification);
              } catch (e) {
                console.error('Error parsing read notification:', e);
              }
            });
          }

          console.log('✅ All subscriptions established');
          resolve();
        },
        (error: any) => {
          console.error('❌ WS ERROR:', error);
          this.connected$.next(false);
          reject(error);
        }
      );
    });
  }

  /**
   * Отправить сообщение
   */
  sendMessage(receiver: string, text: string, files: any[] = []): void {
    if (!this.stomp || !this.stomp.connected) {
      console.warn('STOMP not connected, cannot send message');
      return;
    }

    const message = {
      text: text.trim(),
      receiver: receiver,
      files: files
    };

    this.stomp.send('/app/messages.send', {}, JSON.stringify(message));
    console.log('➡️ Message sent:', message);
  }

  /**
   * Отметить сообщения как прочитанные
   */
  markAsRead(receiver: string): void {
    if (!this.stomp || !this.stomp.connected) {
      console.warn('STOMP not connected, cannot mark as read');
      return;
    }

    const dto = {
      receiver: receiver
    };

    this.stomp.send('/app/messages.read', {}, JSON.stringify(dto));
    console.log('📘 Marked as read:', dto);
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
  getMessageReceivedInChat$(): Observable<{ chatNickname: string } | null> {
    return this.messageReceivedInChatSubject$.asObservable().pipe(
      filter(event => event !== null)
    );
  }

  /**
   * Отправить сигнал, что сообщение получено в открытом чате
   */
  notifyMessageReceivedInChat(chatNickname: string): void {
    this.messageReceivedInChatSubject$.next({ chatNickname });
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
    return this.stomp && this.stomp.connected;
  }

  /**
   * Отключиться от WebSocket
   */
  disconnect(): Promise<void> {
    return new Promise((resolve) => {
      if (this.stomp && this.stomp.connected) {
        this.stomp.disconnect(() => {
          console.log('🔌 WS disconnected');
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
}
