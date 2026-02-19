import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ProjectService } from '../service/project.service';
import { Project } from '../service/interfaces/project.interface';

interface Moderator {
  nickname: string;
  key: string;
  avatarUrl?: SafeUrl | null;
}

interface ProjectProfile {
  title: string;
  key: string;
  moderators: Moderator[];
}

@Component({
  selector: 'project-settings-component',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, RouterModule],
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
})
export class ProjectSettingsComponent implements OnInit {
  projectTitle: string = '';
  projectAvatarUrl: SafeUrl | null = null;
  projectAvatarKey: string = '';
  moderators: Moderator[] = [];

  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // Модальные окна
  showAvatarModal = false;

  // Удаление модератора
  showDeleteConfirm = false;
  moderatorToDelete: string | null = null;
  isDeleting = false;

  // Удаление проекта
  showDeleteProjectConfirm = false;
  isDeleteProjectLoading = false;
  deleteTimer = 5;
  deleteTimerInterval: any = null;

  // Добавление модератора
  showAddModeratorModal = false;
  showAddModeratorConfirm = false;
  newModeratorNickname: string = '';
  confirmModeratorNickname: string = '';
  isAdding = false;

  // Avatar upload
  selectedAvatarFile: File | null = null;
  uploadedAvatarPreview: string | SafeUrl | null = null;
  
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

  isSubmitting = false;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer,
    private router: Router,
    private route: ActivatedRoute,
    private projectService: ProjectService
  ) {}

  ngOnInit() {
    // Получаем параметр projectTitle из маршрута или из selectedProject в сервисе
    this.route.params.subscribe(params => {
      const titleFromRoute = params['projectTitle'];
      const selectedProject = this.projectService.getSelectedProject();
      
      this.projectTitle = titleFromRoute || selectedProject?.title || '';
      if (this.projectTitle) {
        this.loadProjectProfile();
      }
    });
  }

  loadProjectProfile() {
    this.http.get<ProjectProfile>(
      `http://localhost:8080/api/projects/${this.projectTitle}/profile`,
      { withCredentials: true }
    ).subscribe({
      next: (data) => {
        this.projectTitle = data.title || '';
        this.projectAvatarKey = data.key || '';
        this.moderators = data.moderators || [];
        
        if (data.key) {
          this.loadProjectAvatar(data.key);
        }
        
        // Загружаем аватарки для каждого модератора
        this.moderators.forEach(moderator => {
          if (moderator.key) {
            this.loadModeratorAvatar(moderator);
          }
        });
        
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load project profile:', err);
        this.showToast('Не удалось загрузить профиль проекта', 'error');
      }
    });
  }

  loadProjectAvatar(avatarKey: string) {
    this.http.get(`http://localhost:8080/api/files/${avatarKey}/view`, {
      responseType: 'blob',
      withCredentials: true
    }).subscribe({
      next: (blob: Blob) => {
        const blobUrl = URL.createObjectURL(blob);
        this.projectAvatarUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Failed to load project avatar:', err)
    });
  }

  loadModeratorAvatar(moderator: Moderator) {
    this.http.get(`http://localhost:8080/api/files/${moderator.key}/view`, {
      responseType: 'blob',
      withCredentials: true
    }).subscribe({
      next: (blob: Blob) => {
        const blobUrl = URL.createObjectURL(blob);
        moderator.avatarUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Failed to load moderator avatar:', err)
    });
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
    const fileInput = document.getElementById('projectAvatarFileInput') as HTMLInputElement;
    fileInput?.click();
  }

  onAvatarFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.showToast('Файл должен быть изображением', 'error');
      return;
    }

    this.selectedAvatarFile = file;

    const reader = new FileReader();
    reader.onload = (e) => {
      this.originalImage = e.target?.result as string;
      this.showCropEditor = true;
      this.cdr.detectChanges();
      
      setTimeout(() => this.initializeCropper(), 100);
    };
    reader.readAsDataURL(file);
  }

  initializeCropper() {
    const canvas = document.getElementById('projectCropperCanvas') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.cropperCanvas = canvas;

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

      this.cropSize = Math.min(scaledWidth, scaledHeight) * 0.8;
      this.cropX = (this.canvasWidth - this.cropSize) / 2;
      this.cropY = (this.canvasHeight - this.cropSize) / 2;

      this.drawCropArea();
    };
    img.src = this.originalImage;

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

      ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
      ctx.fillRect(0, 0, this.cropX, this.canvasHeight);
      ctx.fillRect(this.cropX + this.cropSize, 0, this.canvasWidth - this.cropX - this.cropSize, this.canvasHeight);
      ctx.fillRect(this.cropX, 0, this.cropSize, this.cropY);
      ctx.fillRect(this.cropX, this.cropY + this.cropSize, this.cropSize, this.canvasHeight - this.cropY - this.cropSize);

      ctx.strokeStyle = '#cf78ef';
      ctx.lineWidth = 2;
      ctx.strokeRect(this.cropX, this.cropY, this.cropSize, this.cropSize);

      const handleSize = 12;
      ctx.fillStyle = '#cf78ef';
      
      ctx.fillRect(this.cropX - handleSize / 2, this.cropY - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX + this.cropSize - handleSize / 2, this.cropY - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX - handleSize / 2, this.cropY + this.cropSize - handleSize / 2, handleSize, handleSize);
      ctx.fillRect(this.cropX + this.cropSize - handleSize / 2, this.cropY + this.cropSize - handleSize / 2, handleSize, handleSize);

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
    croppedCanvas.width = 512;
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

      croppedCanvas.toBlob((blob) => {
        if (blob) {
          this.croppedImageBlob = blob;
          
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

    const formData = new FormData();
    formData.append('file', this.croppedImageBlob, 'avatar.jpg');

    this.http.post<any>('http://localhost:8080/api/files/upload', formData, { withCredentials: true })
      .subscribe({
        next: (response) => {
          const fileKey = response.key;
          this.updateProjectAvatarKey(fileKey);
        },
        error: (err) => {
          console.error('Failed to upload avatar:', err);
          this.showToast('Ошибка при загрузке файла', 'error');
          this.isSubmitting = false;
        }
      });
  }

  updateProjectAvatarKey(key: string) {
    this.http.patch(
      `http://localhost:8080/api/projects/avatar?key=${key}&title=${this.projectTitle}`,
      {},
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.projectAvatarKey = key;
        this.loadProjectAvatar(key);
        this.showToast('Аватар проекта успешно изменён', 'success');
        this.closeAvatarModal();
        this.isSubmitting = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to update project avatar:', err);
        this.showToast('Ошибка при изменении аватара', 'error');
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

  // Удаление модератора
  openDeleteConfirm(nickname: string) {
    this.moderatorToDelete = nickname;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.moderatorToDelete = null;
  }

  confirmDeleteModerator() {
    if (!this.moderatorToDelete) return;

    this.isDeleting = true;
    const nicknameToDelete = this.moderatorToDelete;

    this.http.delete(
      `http://localhost:8080/api/projects/${this.projectTitle}/moderators/${nicknameToDelete}`,
      { withCredentials: true }
    ).subscribe({
      next: () => {
        // Удаляем модератора из списка
        this.moderators = this.moderators.filter(m => m.nickname !== nicknameToDelete);
        this.showToast('Модератор успешно удален', 'success');
        this.closeDeleteConfirm();
        this.isDeleting = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при удалении модератора:', err);
        this.showToast('Ошибка при удалении модератора', 'error');
        this.isDeleting = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Добавление модератора
  openAddModeratorModal() {
    this.newModeratorNickname = '';
    this.confirmModeratorNickname = '';
    this.showAddModeratorModal = true;
    this.showAddModeratorConfirm = false;
  }

  closeAddModeratorModal() {
    this.showAddModeratorModal = false;
    this.showAddModeratorConfirm = false;
    this.newModeratorNickname = '';
    this.confirmModeratorNickname = '';
  }

  openAddModeratorConfirm() {
    if (!this.newModeratorNickname || this.newModeratorNickname.trim().length === 0) {
      this.showToast('Введите никнейм пользователя', 'error');
      return;
    }
    this.showAddModeratorConfirm = true;
  }

  closeAddModeratorConfirm() {
    this.showAddModeratorConfirm = false;
  }

  confirmAddModerator() {
    if (this.newModeratorNickname !== this.confirmModeratorNickname) {
      this.showToast('Никнеймы не совпадают', 'error');
      return;
    }

    this.isAdding = true;
    const nicknameToAdd = this.newModeratorNickname.trim();

    this.http.post(
      `http://localhost:8080/api/projects/${this.projectTitle}/moderators/${nicknameToAdd}`,
      {},
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.showToast('Модератор успешно добавлен', 'success');
        this.closeAddModeratorModal();
        this.isAdding = false;
        // Перезагружаем профиль проекта чтобы показать нового модератора с аватаркой
        this.loadProjectProfile();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при добавлении модератора:', err);
        this.showToast('Ошибка при добавлении модератора', 'error');
        this.isAdding = false;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    this.router.navigate(['/main/ideas', this.projectTitle]);
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

    if (!file.type.startsWith('image/')) {
      this.showToast('Файл должен быть изображением', 'error');
      return;
    }

    const event = { target: { files: [file] } };
    this.onAvatarFileSelected(event);
  }

  openDeleteProjectConfirm() {
    this.showDeleteProjectConfirm = true;
    this.deleteTimer = 5;
    this.startDeleteTimer();
  }

  closeDeleteProjectConfirm() {
    this.showDeleteProjectConfirm = false;
    if (this.deleteTimerInterval) {
      clearInterval(this.deleteTimerInterval);
      this.deleteTimerInterval = null;
    }
  }

  startDeleteTimer() {
    if (this.deleteTimerInterval) {
      clearInterval(this.deleteTimerInterval);
    }
    
    this.deleteTimerInterval = setInterval(() => {
      this.deleteTimer--;
      if (this.deleteTimer <= 0) {
        clearInterval(this.deleteTimerInterval);
        this.deleteTimerInterval = null;
      }
    }, 1000);
  }

  deleteProject() {
    if (this.deleteTimer > 0) {
      return; // Таймер еще не истек
    }
    
    this.isDeleteProjectLoading = true;
    
    this.http.delete(
      `http://localhost:8080/api/projects/${this.projectTitle}`,
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.showToast('Проект успешно удален', 'success');
        this.closeDeleteProjectConfirm();
        this.isDeleteProjectLoading = false;
        
        // Перенаправляем на главную после удаления
        setTimeout(() => {
          this.router.navigate(['/main/ideas']);
        }, 1000);
      },
      error: (err) => {
        console.error('Ошибка при удалении проекта:', err);
        this.showToast('Ошибка при удалении проекта', 'error');
        this.isDeleteProjectLoading = false;
      }
    });
  }
}
