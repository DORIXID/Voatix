import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../service/authorization/auth.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'user-profile-component',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, RouterModule],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  nickname: string = '';
  email: string = '';
  userId: number | null = null;
  avatarUrl: SafeUrl | null = null;
  avatarKey: string = '';

  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // Модальные окна
  showNicknameModal = false;
  showEmailModal = false;
  showPasswordModal = false;
  showAvatarModal = false;

  // Формы редактирования
  editNickname: string = '';
  editEmail: string = '';
  editPassword: string = '';
  editPasswordConfirm: string = '';
  isSubmitting = false;
  
  // Avatar upload
  avatarFileInput: HTMLInputElement | null = null;
  uploadedAvatarPreview: string | SafeUrl | null = null;
  selectedAvatarFile: File | null = null;
  
  // Crop editor
  showCropEditor = false;
  originalImage: string = '';
  croppedImageBlob: Blob | null = null;
  cropperCanvas: HTMLCanvasElement | null = null;
  cropX = 0;
  cropY = 0;
  cropSize = 200;
  isDragging = false;
  dragStartX = 0;
  dragStartY = 0;
  dragType: 'move' | 'resize' | null = null;
  canvasWidth = 400;
  canvasHeight = 400;
  
  // Drag and drop
  isDragOverDropZone = false;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserProfile();
  }

  loadUserProfile() {
    // Получаем данные пользователя
    this.http.get<any>('http://localhost:8080/api/users/profile', { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.userId = data.userId || null;
          this.nickname = data.nickname || '';
          this.email = data.email || '';
          this.avatarKey = data.fileId || '';
          
          // Загружаем аватар если есть
          if (data.fileId) {
            this.loadAvatar(data.fileId.toString());
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to load user profile:', err);
          this.showToast('Не удалось загрузить профиль', 'error');
        }
      });
  }

  loadAvatar(avatarKey: string) {
    this.http.get(`http://localhost:8080/api/files/${avatarKey}/view`, {
      responseType: 'blob',
      withCredentials: true
    }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        this.avatarUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load avatar:', err);
        this.avatarUrl = null;
        this.cdr.detectChanges();
      }
    });
  }

  // Модальные окна
  openNicknameModal() {
    this.editNickname = this.nickname;
    this.showNicknameModal = true;
  }

  closeNicknameModal() {
    this.showNicknameModal = false;
    this.editNickname = '';
  }

  openEmailModal() {
    this.editEmail = this.email;
    this.showEmailModal = true;
  }

  closeEmailModal() {
    this.showEmailModal = false;
    this.editEmail = '';
  }

  openPasswordModal() {
    this.showPasswordModal = true;
  }

  closePasswordModal() {
    this.showPasswordModal = false;
    this.editPassword = '';
    this.editPasswordConfirm = '';
  }

  // Avatar modal
  openAvatarModal() {
    this.showAvatarModal = true;
    this.uploadedAvatarPreview = null;
    this.selectedAvatarFile = null;
  }

  closeAvatarModal() {
    this.showAvatarModal = false;
    this.uploadedAvatarPreview = null;
    this.selectedAvatarFile = null;
  }

  triggerFileInput() {
    const fileInput = document.getElementById('avatarFileInput') as HTMLInputElement;
    fileInput?.click();
  }

  onAvatarFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    // Проверяем тип файла
    if (!file.type.startsWith('image/')) {
      this.showToast('Файл должен быть изображением', 'error');
      return;
    }

    this.selectedAvatarFile = file;

    // Открываем crop editor
    const reader = new FileReader();
    reader.onload = (e) => {
      this.originalImage = e.target?.result as string;
      this.showCropEditor = true;
      this.cdr.detectChanges();
      
      // Инициализируем canvas с изображением
      setTimeout(() => this.initializeCropper(), 100);
    };
    reader.readAsDataURL(file);
  }

  initializeCropper() {
    const canvas = document.getElementById('cropperCanvas') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.cropperCanvas = canvas;

    const img = new Image();
    img.onload = () => {
      // Масштабируем изображение в canvas
      const scale = Math.min(this.canvasWidth / img.width, this.canvasHeight / img.height);
      const scaledWidth = img.width * scale;
      const scaledHeight = img.height * scale;

      // Центрируем изображение
      const offsetX = (this.canvasWidth - scaledWidth) / 2;
      const offsetY = (this.canvasHeight - scaledHeight) / 2;

      ctx.fillStyle = '#f5f5f5';
      ctx.fillRect(0, 0, this.canvasWidth, this.canvasHeight);

      ctx.drawImage(img, offsetX, offsetY, scaledWidth, scaledHeight);

      // Инициализируем crop area в центре
      this.cropSize = Math.min(scaledWidth, scaledHeight) * 0.8;
      this.cropX = (this.canvasWidth - this.cropSize) / 2;
      this.cropY = (this.canvasHeight - this.cropSize) / 2;

      this.drawCropArea();
    };
    img.src = this.originalImage;

    // Добавляем обработчики мыши
    canvas.addEventListener('mousedown', (e) => this.onCropperMouseDown(e));
    canvas.addEventListener('mousemove', (e) => this.onCropperMouseMove(e));
    canvas.addEventListener('mouseup', () => this.onCropperMouseUp());
    canvas.addEventListener('mouseleave', () => this.onCropperMouseUp());
  }

  drawCropArea() {
    if (!this.cropperCanvas) return;
    const ctx = this.cropperCanvas.getContext('2d');
    if (!ctx) return;

    const img = new Image();
    img.onload = () => {
      const scale = Math.min(this.canvasWidth / img.width, this.canvasHeight / img.height);
      const scaledWidth = img.width * scale;
      const scaledHeight = img.height * scale;
      const offsetX = (this.canvasWidth - scaledWidth) / 2;
      const offsetY = (this.canvasHeight - scaledHeight) / 2;

      ctx.fillStyle = '#f5f5f5';
      ctx.fillRect(0, 0, this.canvasWidth, this.canvasHeight);
      ctx.drawImage(img, offsetX, offsetY, scaledWidth, scaledHeight);

      // Затемняем область вне crop area
      ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
      ctx.fillRect(0, 0, this.cropX, this.canvasHeight);
      ctx.fillRect(this.cropX + this.cropSize, 0, this.canvasWidth - this.cropX - this.cropSize, this.canvasHeight);
      ctx.fillRect(this.cropX, 0, this.cropSize, this.cropY);
      ctx.fillRect(this.cropX, this.cropY + this.cropSize, this.cropSize, this.canvasHeight - this.cropY - this.cropSize);

      // Рисуем границу crop area
      ctx.strokeStyle = '#cf78ef';
      ctx.lineWidth = 2;
      ctx.strokeRect(this.cropX, this.cropY, this.cropSize, this.cropSize);

      // Рисуем ручки для изменения размера
      const handleSize = 12;
      ctx.fillStyle = '#cf78ef';
      
      // Углы
      ctx.fillRect(this.cropX - handleSize / 2, this.cropY - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX + this.cropSize - handleSize / 2, this.cropY - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX - handleSize / 2, this.cropY + this.cropSize - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX + this.cropSize - handleSize / 2, this.cropY + this.cropSize - handleSize / 2, handleSize, handleSize);

      // Сетка 3x3
      ctx.strokeStyle = 'rgba(207, 120, 239, 0.3)';
      ctx.lineWidth = 1;
      const cellWidth = this.cropSize / 3;
      const cellHeight = this.cropSize / 3;
      for (let i = 1; i < 3; i++) {
        ctx.beginPath();
        ctx.moveTo(this.cropX + i * cellWidth, this.cropY);
        ctx.lineTo(this.cropX + i * cellWidth, this.cropY + this.cropSize);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(this.cropX, this.cropY + i * cellHeight);
        ctx.lineTo(this.cropX + this.cropSize, this.cropY + i * cellHeight);
        ctx.stroke();
      }
    };
    img.src = this.originalImage;
  }

  onCropperMouseDown(e: MouseEvent) {
    if (!this.cropperCanvas) return;

    const rect = this.cropperCanvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const handleSize = 15;

    // Проверяем, нажали ли на ручку изменения размера
    if (
      Math.abs(x - (this.cropX + this.cropSize)) < handleSize &&
      Math.abs(y - (this.cropY + this.cropSize)) < handleSize
    ) {
      this.isDragging = true;
      this.dragType = 'resize';
      this.dragStartX = x;
      this.dragStartY = y;
    } else if (
      x > this.cropX &&
      x < this.cropX + this.cropSize &&
      y > this.cropY &&
      y < this.cropY + this.cropSize
    ) {
      this.isDragging = true;
      this.dragType = 'move';
      this.dragStartX = x - this.cropX;
      this.dragStartY = y - this.cropY;
    }
  }

  onCropperMouseMove(e: MouseEvent) {
    if (!this.isDragging || !this.cropperCanvas) return;

    const rect = this.cropperCanvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    if (this.dragType === 'resize') {
      const newSize = Math.max(100, Math.min(x - this.cropX, y - this.cropY));
      this.cropSize = Math.min(newSize, this.canvasWidth - this.cropX, this.canvasHeight - this.cropY);
      this.drawCropArea();
    } else if (this.dragType === 'move') {
      let newX = x - this.dragStartX;
      let newY = y - this.dragStartY;

      newX = Math.max(0, Math.min(newX, this.canvasWidth - this.cropSize));
      newY = Math.max(0, Math.min(newY, this.canvasHeight - this.cropSize));

      this.cropX = newX;
      this.cropY = newY;
      this.drawCropArea();
    }
  }

  onCropperMouseUp() {
    this.isDragging = false;
    this.dragType = null;
  }

  confirmCrop() {
    if (!this.cropperCanvas) return;

    const croppedCanvas = document.createElement('canvas');
    croppedCanvas.width = 512; // Размер финального аватара
    croppedCanvas.height = 512;

    const ctx = croppedCanvas.getContext('2d');
    if (!ctx) return;

    const img = new Image();
    img.onload = () => {
      const scale = Math.min(this.canvasWidth / img.width, this.canvasHeight / img.height);
      const scaledWidth = img.width * scale;
      const scaledHeight = img.height * scale;
      const offsetX = (this.canvasWidth - scaledWidth) / 2;
      const offsetY = (this.canvasHeight - scaledHeight) / 2;

      // Вычисляем координаты на исходном изображении
      const sourceX = (this.cropX - offsetX) / scale;
      const sourceY = (this.cropY - offsetY) / scale;
      const sourceSize = this.cropSize / scale;

      ctx.drawImage(
        img,
        sourceX,
        sourceY,
        sourceSize,
        sourceSize,
        0,
        0,
        512,
        512
      );

      // Преобразуем в Blob
      croppedCanvas.toBlob((blob) => {
        if (blob) {
          this.croppedImageBlob = blob;
          
          // Показываем превью
          const reader = new FileReader();
          reader.onload = (e) => {
            this.uploadedAvatarPreview = this.sanitizer.bypassSecurityTrustUrl(e.target?.result as string);
            this.showCropEditor = false;
            this.cdr.detectChanges();
          };
          reader.readAsDataURL(blob);
        }
      }, 'image/jpeg', 0.95);
    };
    img.src = this.originalImage;
  }

  cancelCrop() {
    this.showCropEditor = false;
    this.uploadedAvatarPreview = null;
    this.croppedImageBlob = null;
    this.originalImage = '';
  }

  saveAvatar() {
    if (!this.croppedImageBlob) {
      this.showToast('Сначала обрежьте изображение', 'error');
      return;
    }

    this.isSubmitting = true;

    // Загружаем обрезанное изображение на сервер
    const formData = new FormData();
    formData.append('file', this.croppedImageBlob, 'avatar.jpg');

    this.http.post<any>('http://localhost:8080/api/files/upload', formData, { withCredentials: true })
      .subscribe({
        next: (response) => {
          const fileId = response.id;
          this.updateAvatarKey(fileId);
        },
        error: (err) => {
          console.error('Failed to upload avatar:', err);
          this.showToast('Ошибка при загрузке файла', 'error');
          this.isSubmitting = false;
        }
      });
  }

  updateAvatarKey(fileId: number) {
    this.http.patch('http://localhost:8080/api/users/avatar', { fileId: fileId }, { withCredentials: true })
      .subscribe({
        next: () => {
          this.avatarKey = fileId.toString();
          this.loadAvatar(fileId.toString());
          this.showToast('Аватар успешно изменён', 'success');
          this.closeAvatarModal();
          this.isSubmitting = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update avatar:', err);
          this.showToast('Ошибка при изменении аватара', 'error');
          this.isSubmitting = false;
        }
      });
  }

  // Сохранение данных
  saveNickname() {
    if (!this.editNickname || this.editNickname.length < 6 || this.editNickname.length > 30) {
      this.showToast('Никнейм должен быть от 6 до 30 символов', 'error');
      return;
    }

    this.isSubmitting = true;
    const payload = {
      nickname: this.editNickname
    };

    this.http.patch('http://localhost:8080/api/users/edit', payload, { withCredentials: true })
      .subscribe({
        next: () => {
          this.nickname = this.editNickname;
          this.showToast('Никнейм успешно изменён', 'success');
          this.closeNicknameModal();
          this.isSubmitting = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update nickname:', err);
          this.showToast('Ошибка при изменении никнейма', 'error');
          this.isSubmitting = false;
        }
      });
  }

  saveEmail() {
    if (!this.editEmail || !this.isValidEmail(this.editEmail)) {
      this.showToast('Некорректный адрес электронной почты', 'error');
      return;
    }

    this.isSubmitting = true;
    const payload = {
      eMail: this.editEmail
    };

    this.http.patch('http://localhost:8080/api/users/edit', payload, { withCredentials: true })
      .subscribe({
        next: () => {
          this.email = this.editEmail;
          this.showToast('Почта успешно изменена', 'success');
          this.closeEmailModal();
          this.isSubmitting = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update email:', err);
          this.showToast('Ошибка при изменении почты', 'error');
          this.isSubmitting = false;
        }
      });
  }

  savePassword() {
    if (!this.editPassword || this.editPassword.length < 6 || this.editPassword.length > 30) {
      this.showToast('Пароль должен быть от 6 до 30 символов', 'error');
      return;
    }

    if (this.editPassword !== this.editPasswordConfirm) {
      this.showToast('Пароли не совпадают', 'error');
      return;
    }

    this.isSubmitting = true;
    const payload = {
      password: this.editPassword
    };

    this.http.patch('http://localhost:8080/api/users/edit', payload, { withCredentials: true })
      .subscribe({
        next: () => {
          this.showToast('Пароль успешно изменён', 'success');
          this.closePasswordModal();
          this.isSubmitting = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update password:', err);
          this.showToast('Ошибка при изменении пароля', 'error');
          this.isSubmitting = false;
        }
      });
  }

  // Toast
  showToast(message: string, type: 'success' | 'error') {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    setTimeout(() => {
      this.toastVisible = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  closeToast() {
    this.toastVisible = false;
  }

  // Утилиты
  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  goBack() {
    this.router.navigate(['/main/ideas']);
  }

  // Drag and drop
  onDragOverDropZone(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragOverDropZone = true;
  }

  onDragLeaveDropZone(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragOverDropZone = false;
  }

  onDropFile(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragOverDropZone = false;

    const files = e.dataTransfer?.files;
    if (!files || files.length === 0) return;

    const file = files[0];

    // Проверяем тип файла
    if (!file.type.startsWith('image/')) {
      this.showToast('Файл должен быть изображением', 'error');
      return;
    }

    // Обрабатываем файл как обычный выбор
    const event = { target: { files: [file] } };
    this.onAvatarFileSelected(event);
  }
}
