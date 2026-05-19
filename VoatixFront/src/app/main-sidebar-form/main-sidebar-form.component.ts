import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Router, ActivatedRoute } from "@angular/router";
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Project } from '../service/interfaces/project.interface';
import { ProjectService } from '../service/project.service';
import { FileService } from '../service/file.service';
import { AuthService } from '../service/authorization/auth.service';
import { UserRole } from '../service/enums/user-role.enum';
import { Observable, of } from 'rxjs';


@Component({
  selector: 'main-sidebar-form-component',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule
  ],
  templateUrl: './main-sidebar-form.component.html',
  styleUrls: ['./main-sidebar-form.component.scss']
})

export class MainSidebarFormComponent implements OnInit {

  constructor(
    private router: Router,
    private http: HttpClient,
    private projectService: ProjectService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private fileService: FileService,
    private auth: AuthService
  ) {}

  public selectedSection: String = 'ideas';

  projects: Project[] = [];
  selectedProject: Project | null = null;

  isOpen = false;
  projectAvatarUrls: Map<string, Observable<string | null>> = new Map();

  // Создание проекта
  showCreateProjectModal = false;
  newProjectTitle: string = '';
  newProjectDescription: string = '';
  selectedProjectAvatarFile: File | null = null;
  uploadedProjectAvatarPreview: string | null = null;
  projectAvatarKey: string = '';
  isCreatingProject = false;
  isDragOverDropZone = false;

  // Поиск проекта
  showProjectSearch = false;
  searchProjectName: string = '';
  showFindOrCreateDropdown = false;

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
  // Масштабированные координаты изображения
  scaledImageX = 0;
  scaledImageY = 0;
  scaledImageWidth = 0;
  scaledImageHeight = 0;
  imageScale = 1;

  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  ngOnInit() {

    // 1. Подписываемся на сервис — sidebar всегда знает актуальный проект
    this.projectService.selectedProject$.subscribe(p => {
      this.selectedProject = p;
    });

    // Определяем текущий раздел на основе URL
    this.route.children.forEach(child => {
      child.url.subscribe(segments => {
        if (segments.length > 0) {
          const section = segments[0].path;
          if (['ideas', 'surveys', 'messages'].includes(section)) {
            this.selectedSection = section;
          }
        }
      });
    });

    // 2. Загружаем проекты
    this.loadProjects();
  }
  

  loadProjects() {
    this.projectService.getProjects().subscribe({
      next: (data) => {
        this.projects = data.content || data;

        // Загружаем аватары для всех проектов
        this.projects.forEach(p => {
          // Use avatarId (new format), fileId, avatarKey or key (legacy) — prefer numeric ids when possible
          const avatarId = p.avatarId || p.fileId || p.avatarKey || p.key;
          
          if (avatarId) {
            const avatarIdStr = typeof avatarId === 'number' ? avatarId.toString() : avatarId;
            
            // Check if already loading
            if (!this.projectAvatarUrls.has(p.title)) {
              this.projectAvatarUrls.set(p.title, this.fileService.getImageDataUrl(avatarIdStr));
            }
          }
        });

        // 3. Если в URL есть projectId — выбираем соответствующий проект
        const child = this.route.snapshot.firstChild;
        const urlProjectId = child?.paramMap.get('projectId');

        if (urlProjectId) {
          const projectIdNum = parseInt(urlProjectId, 10);
          let p = this.projects.find(pr => pr.id === projectIdNum);
          if (p) {
            this.selectProject(p);
          } else {
            // Проект не найден в списке — выбираем первый
            if (!this.projectService.getSelectedProject() && this.projects.length > 0) {
              this.selectProject(this.projects[0]);
            }
          }
        } else {
          // 4. Если проект ещё не выбран — выбираем первый
          if (!this.projectService.getSelectedProject() && this.projects.length > 0) {
            this.selectProject(this.projects[0]);
          }
        }
        this.cdr.detectChanges();
      },
      error: (err) => {}
    });
  }

  openProject(projectTitle?: string) {
  }

  select(selectedSection: String) {
    this.selectedSection = selectedSection;
  }

  clickOnIdeas() {
    this.select('ideas');
    if (this.selectedProject) {
      this.router.navigate(['/main/ideas', this.selectedProject.id]);
    } else {
      this.router.navigate(['/main/ideas']);
    }
  }

  clickOnSurveys() {
    this.select('surveys');
    if (this.selectedProject) {
      this.router.navigate(['/main/surveys', this.selectedProject.id]);
    } else {
      this.router.navigate(['/main/surveys']);
    }
  }

  clickOnMessages() {
    this.select('messages');
    this.router.navigate(['/main/messages']);
  }

  clickOnProfile() {
    this.router.navigate(['/main/profile']);
  }

  clickOnFAQ() {
    this.router.navigate(['/main/faq']);
  }

  clickOnProjectSettings() {
    if (this.selectedProject) {
      this.router.navigate(['/main/project-settings', this.selectedProject.id]);
    }
  }

  clickOnExit() {
    // Очищаем данные перед выходом
    this.projects = [];
    this.selectedProject = null;
    this.projectAvatarUrls.clear();
    this.projectService.clearProject();
    
    // Вызываем logout для очистки токенов и куков
    this.auth.logout();
    
    this.router.navigate(['/login']);
  }

  toggle() {
    this.isOpen = !this.isOpen;
  }

  selectProject(p: Project) {
    this.selectedProject = p;
    this.isOpen = false;

    // 4. Обновляем сервис — теперь все компоненты знают выбранный проект
    this.projectService.setProject(p);

    this.openProject(p.title);
    // Обновляем URL для возможностей шаринга Deep Link (используем id проекта)
    if (this.selectedSection === 'ideas') {
      this.router.navigate(['/main/ideas', p.id]);
    } else if (this.selectedSection === 'surveys') {
      this.router.navigate(['/main/surveys', p.id]);
    }
  }

  // Создание проекта
  openCreateProjectModal() {
    this.newProjectTitle = '';
    this.newProjectDescription = '';
    this.uploadedProjectAvatarPreview = null;
    this.selectedProjectAvatarFile = null;
    this.projectAvatarKey = '';
    this.showCreateProjectModal = true;
    this.closeFindOrCreateDropdown();
  }

  closeCreateProjectModal() {
    this.showCreateProjectModal = false;
    this.newProjectTitle = '';
    this.newProjectDescription = '';
    this.uploadedProjectAvatarPreview = null;
    this.selectedProjectAvatarFile = null;
    this.projectAvatarKey = '';
  }

  triggerProjectAvatarFileInput() {
    const fileInput = document.getElementById('projectAvatarFileInput') as HTMLInputElement;
    fileInput?.click();
  }

  onProjectAvatarFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.showToast('Файл должен быть изображением', 'error');
      return;
    }

    this.selectedProjectAvatarFile = file;

    const reader = new FileReader();
    reader.onload = (e) => {
      this.originalImage = e.target?.result as string;
      this.showCropEditor = true;
      this.cdr.detectChanges();
      
      setTimeout(() => this.initializeCropper(), 100);
    };
    reader.readAsDataURL(file);
  }

  onProjectAvatarDragOver(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragOverDropZone = true;
  }

  onProjectAvatarDragLeave(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragOverDropZone = false;
  }

  onProjectAvatarDrop(e: DragEvent) {
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
    this.onProjectAvatarFileSelected(event);
  }

  initializeCropper() {
    this.cropperCanvas = document.getElementById('projectCropperCanvas') as HTMLCanvasElement;
    if (!this.cropperCanvas) return;

    const context = this.cropperCanvas.getContext('2d');
    if (!context) return;

    const image = new Image();
    image.onload = () => {
      this.drawCropArea(image);
    };
    image.src = this.originalImage;
  }

  drawCropArea(image: HTMLImageElement) {
    const context = this.cropperCanvas!.getContext('2d');
    if (!context) return;

    context.fillStyle = 'rgba(0, 0, 0, 0.5)';
    context.fillRect(0, 0, this.canvasWidth, this.canvasHeight);

    context.clearRect(this.cropX, this.cropY, this.cropSize, this.cropSize);

    // Масштабируем изображение для размещения в canvas
    const maxWidth = this.canvasWidth;
    const maxHeight = this.canvasHeight;
    let imgWidth = image.width;
    let imgHeight = image.height;

    // Рассчитываем масштаб для сохранения пропорций
    this.imageScale = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
    this.scaledImageWidth = imgWidth * this.imageScale;
    this.scaledImageHeight = imgHeight * this.imageScale;

    // Центрируем масштабированное изображение
    this.scaledImageX = (maxWidth - this.scaledImageWidth) / 2;
    this.scaledImageY = (maxHeight - this.scaledImageHeight) / 2;
    context.drawImage(image, this.scaledImageX, this.scaledImageY, this.scaledImageWidth, this.scaledImageHeight);

    // Рисуем сетку
    context.strokeStyle = 'rgba(255, 255, 255, 0.3)';
    context.lineWidth = 1;
    for (let i = 1; i < 3; i++) {
      context.beginPath();
      context.moveTo(this.cropX + (this.cropSize / 3) * i, this.cropY);
      context.lineTo(this.cropX + (this.cropSize / 3) * i, this.cropY + this.cropSize);
      context.stroke();

      context.beginPath();
      context.moveTo(this.cropX, this.cropY + (this.cropSize / 3) * i);
      context.lineTo(this.cropX + this.cropSize, this.cropY + (this.cropSize / 3) * i);
      context.stroke();
    }

    // Рисуем границы
    context.strokeStyle = 'rgba(207, 120, 239, 1)';
    context.lineWidth = 2;
    context.strokeRect(this.cropX, this.cropY, this.cropSize, this.cropSize);

    // Рисуем углы для изменения размера
    const cornerSize = 10;
    context.fillStyle = 'rgba(207, 120, 239, 1)';
    context.fillRect(
      this.cropX + this.cropSize - cornerSize,
      this.cropY + this.cropSize - cornerSize,
      cornerSize,
      cornerSize
    );
  }

  onCropperMouseDown(event: MouseEvent) {
    const canvas = this.cropperCanvas;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Проверяем, в углу ли мышка (изменение размера)
    const cornerSize = 10;
    if (
      x >= this.cropX + this.cropSize - cornerSize &&
      x <= this.cropX + this.cropSize &&
      y >= this.cropY + this.cropSize - cornerSize &&
      y <= this.cropY + this.cropSize
    ) {
      this.dragType = 'resize';
    } else if (
      x >= this.cropX &&
      x <= this.cropX + this.cropSize &&
      y >= this.cropY &&
      y <= this.cropY + this.cropSize
    ) {
      this.dragType = 'move';
    }

    if (this.dragType) {
      this.isDragging = true;
      this.dragStartX = x;
      this.dragStartY = y;
    }
  }

  onCropperMouseMove(event: MouseEvent) {
    const canvas = this.cropperCanvas;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Изменяем курсор
    const cornerSize = 10;
    if (
      x >= this.cropX + this.cropSize - cornerSize &&
      x <= this.cropX + this.cropSize &&
      y >= this.cropY + this.cropSize - cornerSize &&
      y <= this.cropY + this.cropSize
    ) {
      canvas.style.cursor = 'nwse-resize';
    } else if (
      x >= this.cropX &&
      x <= this.cropX + this.cropSize &&
      y >= this.cropY &&
      y <= this.cropY + this.cropSize
    ) {
      canvas.style.cursor = 'grab';
    } else {
      canvas.style.cursor = 'default';
    }

    if (!this.isDragging) return;

    const deltaX = x - this.dragStartX;
    const deltaY = y - this.dragStartY;

    if (this.dragType === 'move') {
      this.cropX = Math.max(0, Math.min(this.cropX + deltaX, this.canvasWidth - this.cropSize));
      this.cropY = Math.max(0, Math.min(this.cropY + deltaY, this.canvasHeight - this.cropSize));
    } else if (this.dragType === 'resize') {
      const newSize = this.cropSize + deltaX;
      if (newSize >= 50 && newSize <= Math.min(this.canvasWidth - this.cropX, this.canvasHeight - this.cropY)) {
        this.cropSize = newSize;
      }
    }

    this.dragStartX = x;
    this.dragStartY = y;

    const image = new Image();
    image.onload = () => {
      this.drawCropArea(image);
    };
    image.src = this.originalImage;
  }

  onCropperMouseUp() {
    this.isDragging = false;
    this.dragType = null;
  }

  confirmCrop() {
    const canvas = this.cropperCanvas;
    if (!canvas) return;

    const context = canvas.getContext('2d');
    if (!context) return;

    // Создаем новый canvas для результата 512x512
    const resultCanvas = document.createElement('canvas');
    resultCanvas.width = 512;
    resultCanvas.height = 512;
    const resultContext = resultCanvas.getContext('2d');
    if (!resultContext) return;

    const image = new Image();
    image.onload = () => {
      // Переводим координаты crop'а в координаты исходного изображения
      const sourceX = (this.cropX - this.scaledImageX) / this.imageScale;
      const sourceY = (this.cropY - this.scaledImageY) / this.imageScale;
      const sourceSize = this.cropSize / this.imageScale;

      resultContext.drawImage(
        image,
        sourceX,
        sourceY,
        sourceSize,
        sourceSize,
        0,
        0,
        512,
        512
      );

      resultCanvas.toBlob((blob) => {
        if (blob) {
          this.croppedImageBlob = blob;
          this.uploadedProjectAvatarPreview = resultCanvas.toDataURL('image/jpeg', 0.95);
          this.selectedProjectAvatarFile = new File([blob], 'project-avatar.jpg', { type: 'image/jpeg' });
          this.showCropEditor = false;
          this.cdr.detectChanges();
        }
      }, 'image/jpeg', 0.95);
    };
    image.src = this.originalImage;
  }

  cancelCrop() {
    this.showCropEditor = false;
    this.originalImage = '';
    this.croppedImageBlob = null;
    this.selectedProjectAvatarFile = null;
    this.cdr.detectChanges();
  }

  createProject() {
    if (!this.newProjectTitle || this.newProjectTitle.trim().length < 3 || this.newProjectTitle.trim().length > 30) {
      this.showToast('Название проекта должно быть от 3 до 30 символов', 'error');
      return;
    }

    this.isCreatingProject = true;

    if (this.selectedProjectAvatarFile) {
      // Загружаем аватарку сначала
      const formData = new FormData();
      formData.append('file', this.selectedProjectAvatarFile);

      this.http.post<any>('http://localhost:8080/api/files/upload', formData, { withCredentials: true })
        .subscribe({
          next: (response) => {
            this.projectAvatarKey = response.id;
            this.createProjectWithAvatar();
          },
          error: (err) => {
            this.showToast('Ошибка при загрузке аватарки', 'error');
            this.isCreatingProject = false;
          }
        });
    } else {
      this.createProjectWithAvatar();
    }
  }

  private createProjectWithAvatar() {
    const projectDto = {
      title: this.newProjectTitle.trim()
    } as any;

    if (this.projectAvatarKey) {
      projectDto.fileId = parseInt(this.projectAvatarKey, 10);
    }

    this.http.post<any>(
      'http://localhost:8080/api/projects',
      projectDto,
      { withCredentials: true }
    ).subscribe({
      next: (response) => {
        this.showToast('Проект успешно создан', 'success');
        this.closeCreateProjectModal();
        this.isCreatingProject = false;
        
        // Перезагружаем список проектов
        this.loadProjects();
        
        // Переходим на новый проект по его ID
        const projectId = response.id || response.projectId;
        if (projectId) {
          this.router.navigate(['/main/ideas', projectId]);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.showToast('Ошибка при создании проекта', 'error');
        this.isCreatingProject = false;
        this.cdr.detectChanges();
      }
    });
  }

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

  toggleFindOrCreateDropdown() {
    this.showFindOrCreateDropdown = !this.showFindOrCreateDropdown;
  }

  closeFindOrCreateDropdown() {
    this.showFindOrCreateDropdown = false;
  }

  openProjectSearch() {
    this.showProjectSearch = true;
    this.searchProjectName = '';
    this.closeFindOrCreateDropdown();
    this.cdr.detectChanges();
  }

  closeProjectSearch() {
    this.showProjectSearch = false;
    this.searchProjectName = '';
  }

  findProject() {
    if (!this.searchProjectName.trim()) {
      return;
    }
    const projectTitle = this.searchProjectName.trim();
    this.closeProjectSearch();
    
    // Ищем проект по названию
    const foundProject = this.projects.find(p => p.title.toLowerCase() === projectTitle.toLowerCase());
    
    if (foundProject) {
      this.selectProject(foundProject);
    } else {
      // Если не найден в списке, можно попробовать загрузить с сервера
      this.router.navigate(['/main/ideas']).then(() => {
        this.loadProjects();
      });
    }
  }

  onSearchKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.findProject();
    }
  }

  isProjectOwner(): boolean {
    return this.selectedProject?.roleOfUser === UserRole.OWNER;
  }

  getProjectAvatarUrl(project: Project | null): Observable<string | null> {
    if (!project) {
      return of(null);
    }
    
    // Get avatar ID from any available field (avatarId, fileId, avatarKey, key)
    const avatarId = project.avatarId || project.fileId || project.avatarKey || project.key;
    
    if (!avatarId) {
      return of(null);
    }
    
    if (!this.projectAvatarUrls.has(project.title)) {
      const avatarIdStr = typeof avatarId === 'number' ? avatarId.toString() : avatarId;
      this.projectAvatarUrls.set(project.title, this.fileService.getImageDataUrl(avatarIdStr));
    }
    
    const url = this.projectAvatarUrls.get(project.title);
    return url!;
  }

  /**
   * Проверить есть ли у проекта аватарка
   */
  hasProjectAvatar(project: Project | null): boolean {
    if (!project) {
      return false;
    }
    
    return !!(project.avatarId || project.fileId || project.avatarKey || project.key);
  }
}
