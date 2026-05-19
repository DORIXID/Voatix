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
  senderId?: number;
  receiverId?: number;
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
  @Input() chatNickname: string = ''; // Keep for backwards compatibility, but use chatUserId
  @Input() chatUserId?: number; // NEW: User ID of the chat partner
  @Input() currentUsername: string = '';
  @Input() currentUserId?: number; // NEW: Current user's ID
  @Input() avatarKey?: string;
  @Input() wsService: WebSocketService | null = null;
  @Input() senderName?: string; // Имя отправителя из списка чатов

  messages: Message[] = [];
  messageText: string = '';
  loading: boolean = false;
  avatarUrl: SafeUrl | string | null = '/assets/icons/default-avatar.png';
  
  // File upload
  selectedFiles: Array<{
    file: File;
    name: string;
    key?: string;
    fileId?: number;
    progress: number;
    status: 'pending' | 'uploading' | 'done' | 'error';
  }> = [];
  maxFiles = 5;

  // Image modal
  selectedImageUrl: SafeUrl | null = null;
  isImageModalOpen = false;
  imageUrls: Record<string, SafeUrl | string> = {}; // Store blob URLs and safe URLs
  
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
    public ideaService: IdeaService
  ) {}

  ngOnInit(): void {
    if (this.chatNickname || this.chatUserId) {
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
    if ((changes['chatNickname'] || changes['chatUserId']) && 
        (changes['chatNickname'] && !changes['chatNickname'].firstChange || 
         changes['chatUserId'] && !changes['chatUserId'].firstChange)) {
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
    // Use companionId (userId) for the request - ALWAYS use userId, not nickname
    if (!this.chatUserId) {
      console.warn('⚠️ No chatUserId available, cannot load messages');
      this.loading = false;
      return;
    }

    const payload = { companionId: this.chatUserId, page: 0, limit: 12 };
    
    console.log('📨 Loading messages with payload:', payload);
    this.http.post<any>('http://localhost:8080/api/messages/chat', payload, { withCredentials: true }).subscribe({
      next: (res) => {
        console.log('✓ Messages loaded:', res);
        // Extract messages from response (could be array or paginated response)
        let messages = Array.isArray(res) ? res : (res?.content || []);
        
        // Process messages: determine sender/receiver based on senderId/receiverId
        messages = messages.map((msg: any) => {
          // senderId tells us who sent the message
          if (msg.senderId === this.currentUserId) {
            // This is a message I sent
            msg.sender = this.currentUsername;
          } else if (msg.senderId === this.chatUserId) {
            // This is a message from the chat partner
            msg.sender = this.chatNickname;
          }
          
          // receiverId tells us who receives the message
          if (msg.receiverId === this.currentUserId) {
            msg.receiver = this.currentUsername;
          } else if (msg.receiverId === this.chatUserId) {
            msg.receiver = this.chatNickname;
          }
          
          console.log(`📬 Message processed:`, {
            text: msg.text?.substring(0, 20) || '(empty)',
            senderId: msg.senderId,
            sender: msg.sender,
            isCurrentUser: msg.senderId === this.currentUserId
          });
          
          return msg;
        });
        
        // Reverse to show newest at bottom
        this.messages = messages.reverse();
        
        // Load all images from messages
        this.messages.forEach((message, index) => {
          console.log(`Loading images for message ${index}:`, message.files);
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
          // Handle null messages
          if (!newMessage) {
            console.warn('Received null message');
            return;
          }
          
          // Try to determine if message belongs to this chat
          let belongsToThisChat = false;
          
          // PRIORITY 1: Check by senderId (MOST RELIABLE - userId from backend)
          if (newMessage.senderId !== undefined && this.chatUserId !== undefined) {
            belongsToThisChat = newMessage.senderId === this.chatUserId;
            if (belongsToThisChat) {
              console.log('✅ Message matched by senderId:', newMessage.senderId);
            }
          }
          
          // PRIORITY 2: Fall back to receiverId check 
          if (!belongsToThisChat && newMessage.receiverId !== undefined && this.currentUserId !== undefined) {
            belongsToThisChat = newMessage.receiverId === this.currentUserId && newMessage.senderId === this.chatUserId;
            if (belongsToThisChat) {
              console.log('✅ Message matched by receiverId:', newMessage.receiverId);
            }
          }
          
          // PRIORITY 3: Fall back to nickname comparison (old format - LESS RELIABLE)
          if (!belongsToThisChat && newMessage.sender && this.chatNickname) {
            belongsToThisChat = newMessage.sender === this.chatNickname;
            if (belongsToThisChat) {
              console.log('⚠️ Message matched by nickname (old format):', newMessage.sender);
            }
          }
          
          // PRIORITY 4: If sender/receiver are null, assume message is from the current chat
          if (!belongsToThisChat && !newMessage.sender && !newMessage.receiver && !newMessage.senderId && !newMessage.receiverId) {
            belongsToThisChat = true;
            console.log('⚠️ Message matched by default (no identifiers provided)');
          }
          
          if (belongsToThisChat) {
            this.messages.push(newMessage);
            
            // Load images from new message
            this.loadMessageImages(newMessage);
            
            // Mark as read on server
            if (this.wsService) {
              // ALWAYS use userId if available
              if (this.chatUserId) {
                this.wsService.markAsRead({ userId: this.chatUserId });
              } else if (this.chatNickname) {
                this.wsService.markAsRead(this.chatNickname);
              }
              // Notify parent that message was received in open chat - pass userId
              this.wsService.notifyMessageReceivedInChat(this.chatNickname || `user-${this.chatUserId}`, this.chatUserId);
            }
            
            // Force UI update - call multiple times to ensure update
            this.cdr.detectChanges();
            
            // Schedule another update after a short delay
            setTimeout(() => {
              this.cdr.detectChanges();
            }, 100);
            
            this.scrollToBottom();
          } else {
            console.log('❌ Message does not belong to this chat', {
              senderId: newMessage.senderId,
              chatUserId: this.chatUserId,
              sender: newMessage.sender,
              chatNickname: this.chatNickname
            });
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

    // Prepare file keys for sending - use fileIds if available
    const fileKeys = this.selectedFiles
      .filter(f => f.status === 'done' && (f.key || f.fileId))
      .map(f => f.fileId ? f.fileId.toString() : f.key);

    console.log('📤 Sending message with files:', fileKeys);
    console.log('🔍 chatUserId:', this.chatUserId, 'chatNickname:', this.chatNickname, 'currentUserId:', this.currentUserId);

    // Send via WebSocket using userId if available, fall back to nickname
    // ALWAYS use userId if available - never fall back to nickname!
    if (this.chatUserId) {
      console.log('✅ Sending with receiverId:', this.chatUserId);
      this.wsService?.sendMessage(this.chatUserId, this.messageText.trim(), fileKeys);
    } else {
      console.warn('⚠️ No chatUserId, falling back to nickname:', this.chatNickname);
      this.wsService?.sendMessage(this.chatNickname, this.messageText.trim(), fileKeys);
    }

    // Add to local messages immediately for better UX
    const newMessage: Message = {
      text: this.messageText.trim(),
      sender: this.currentUsername,
      senderId: this.currentUserId,
      receiver: this.chatNickname,
      receiverId: this.chatUserId,
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
        this.avatarUrl = null;
        this.cdr.detectChanges();
      }
    });
  }

  getMessageImageUrl(fileKey: string): SafeUrl | string | null {
    return this.imageUrls[fileKey] || null;
  }


  loadMessageImages(message: Message) {
    if (!message.files || message.files.length === 0) {
      console.log('No files in message');
      return;
    }
    console.log('📸 Loading message images:', message.files);
    message.files.forEach((fileKey: string | number) => {
      // Handle both string fileKey and numeric ID
      const keyStr = typeof fileKey === 'number' ? fileKey.toString() : fileKey;
      
      console.log(`   Processing file: ${keyStr}, isImage: ${this.isImageFile(keyStr)}`);
      
      // Skip if already loaded
      if (this.imageUrls[keyStr]) {
        console.log(`   ✓ Already cached: ${keyStr}`);
        return;
      }
      
      const url = this.ideaService.getFileViewUrl(keyStr);
      console.log(`🔗 Loading file URL: ${url}`);
      this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
        next: (blob) => {
          console.log(`✓ File blob loaded, size: ${blob.size}`);
          const blobUrl = URL.createObjectURL(blob);
          this.imageUrls[keyStr] = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.warn(`✗ Failed to load message image ${keyStr}`, err);
        }
      });
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

    // ALWAYS use userId if available
    if (this.chatUserId) {
      this.wsService.markAsRead({ userId: this.chatUserId });
    } else if (this.chatNickname) {
      this.wsService.markAsRead(this.chatNickname);
    }
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
            // Backend returns either fileId (new) or key (old format)
            const fileId = res?.fileId ?? res?.id;
            const key = res?.key ?? res?.name ?? file.name;
            
            // Store both for backwards compatibility
            this.selectedFiles[idx].fileId = fileId;
            this.selectedFiles[idx].key = key;
            this.selectedFiles[idx].status = 'done';
            this.selectedFiles[idx].progress = 100;
            console.log(`✓ File uploaded: fileId=${fileId}, key=${key}`);
          } else if (event.type === HttpEventType.UploadProgress) {
            const loaded = event.loaded ?? 0;
            const total = event.total ?? loaded;
            const percent = Math.round((loaded / total) * 100);
            this.selectedFiles[idx].progress = percent;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('✗ Ошибка загрузки файла', file.name, err);
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
    const hasImageExtension = imageExtensions.some(ext => lowerKey.includes(ext));
    
    // If has image extension - definitely an image
    if (hasImageExtension) {
      console.log(`🔍 File ${fileKey}: ✅ Has image extension`);
      return true;
    }
    
    // If it's just a number (ID) - assume it's an image (for backward compatibility with numeric file IDs)
    const isNumericId = /^\d+$/.test(fileKey);
    if (isNumericId) {
      console.log(`🔍 File ${fileKey}: ✅ Is numeric ID (treating as image)`);
      return true;
    }
    
    console.log(`🔍 File ${fileKey}: ❌ Not an image`);
    return false;
  }

openImageModal(imageUrl: string | SafeUrl | null | undefined): void {
    if (!imageUrl) return;
    
    // If already SafeUrl, use directly; otherwise sanitize
    if (typeof imageUrl === 'string') {
      this.selectedImageUrl = this.sanitizer.bypassSecurityTrustUrl(imageUrl);
    } else {
      this.selectedImageUrl = imageUrl;
    }
    
    this.isImageModalOpen = true;
  }

  closeImageModal(): void {
    this.selectedImageUrl = null;
    this.isImageModalOpen = false;
  }
}
