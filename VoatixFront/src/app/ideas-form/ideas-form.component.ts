import { Component, ElementRef, ViewChild } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { CommonModule } from '@angular/common';
import { IdeaStatus, IdeaStatusRu } from '../service/enums/idea-status.enum';
import { OnInit } from '@angular/core';
import { IdeaService } from '../service/idea.service';
import { ChangeDetectorRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import localeRu from '@angular/common/locales/ru';
import { registerLocaleData } from '@angular/common';
import { Project } from '../service/interfaces/project.interface';
import { ProjectService } from '../service/project.service';
import { IdeaWithStats } from '../service/interfaces/idea-with-stats.interface';
import { QrCodeService } from '../service/qr-code.service';
import { UserRole } from '../service/enums/user-role.enum';

registerLocaleData(localeRu);

@Component({
  selector: 'ideas-form-component',
  standalone: true,
  imports: [
    MatFormFieldModule,
    CommonModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    MatButtonToggleModule,
  ],
  templateUrl: './ideas-form.component.html',
  styleUrls: ['./ideas-form.component.scss']
})

export class IdeasFormComponent implements OnInit {

  // вкладки
  statuses = ['Все', 'Создан', 'В работе', 'Готово', 'Отклонено'];
  selected = 'Все';

  selectedProject: Project | null = null;
  projectAvatarUrl: SafeUrl | null = null;

  // данные
  ideas: IdeaWithStats[] = [];
  page = 0;
  limit = 12;
  totalPages = 0;
  totalElements = 0;
  searchedValue: string = '';

  // модальное окно
  selectedImageUrl: SafeUrl | null = null;
  isImageModalOpen = false;

  // Avatar upload
  isAvatarModalOpen = false;
  selectedAvatarFile: File | null = null;
  cropperCanvas: HTMLCanvasElement | null = null;
  isUploadingAvatar = false;

  // QR код модальное окно
  isQrModalOpen = false;
  qrCodeUrl: string = '';
  shareUrl: string = '';

  // Удаление идеи
  showDeleteConfirm = false;
  deleteIdeaId: number | null = null;
  isDeleting = false;

  // для шаблона
  IdeaStatusRu = IdeaStatusRu;

  @ViewChild('ideasList') ideasList!: ElementRef;

  constructor(
    private projectService: ProjectService,
    public ideaService: IdeaService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private qrCodeService: QrCodeService
  ) { }

  ngOnInit() {
    // read page from query params (if present)
    this.route.queryParamMap.subscribe(params => {
      const p = params.get('page');
      if (p !== null) {
        const pn = Number(p);
        this.page = isNaN(pn) ? 0 : pn;
      }
    });

    // Initialize with current selected project if available
    const currentProject = this.projectService.getSelectedProject();
    if (currentProject) {
      this.selectedProject = currentProject;
      this.loadIdeas(currentProject);
      this.loadProjectAvatar(currentProject);
    }

    // Subscribe to project changes
    this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;

      if (project) {
        this.loadIdeas(project);
        this.loadProjectAvatar(project);
      }
    });
  }

  loadIdeas(project: Project) {
    const filterBy = this.mapStatusToBackend(this.selected);

    this.ideaService
      .loadIdeas(project.id || 0, this.page, this.limit, filterBy, this.searchedValue)
      .subscribe({
        next: (data) => {
          console.log('🔍 Ideas API response:', data);
          this.ideas = data.content.map((i: any) => ({
            ...i,
            userVote: i.vote === 1
              ? 1
              : i.vote === -1
                ? -1
                : 0,
            imageUrls: {} as Record<string, SafeUrl>
          }));

          // Load images for each idea
          this.ideas.forEach((idea, idx) => {
            // Use fileIds from API (backend no longer returns fileKeys)
            const fileIds = idea.idea.fileIds || [];
            
            console.log(`📸 Idea ${idx} (${idea.idea.title}):`, { 
              fileIds: fileIds
            });
            
            if (fileIds && fileIds.length > 0) {
              fileIds.slice(0, 5).forEach((fileId: number) => {
                const fileIdStr = fileId.toString();
                this.loadImageAsBlob(fileIdStr).then(blobUrl => {
                  if (!idea.imageUrls) idea.imageUrls = {};
                  idea.imageUrls[fileIdStr] = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
                  console.log(`✓ Image URL установлена для ${fileIdStr}:`, idea.imageUrls[fileIdStr]);
                  this.cdr.detectChanges();
                }).catch(err => {
                  console.error(`✗ Failed to load image ${fileIdStr}:`, err);
                });
              });
            }
          });

          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;

          this.cdr.detectChanges();
          
          // Скролл после отрисовки элементов
          setTimeout(() => {
            this.ideasList.nativeElement.scrollTo({ top: 0, behavior: 'smooth' });
          }, 0);
        },
        error: (err) => console.error('Ошибка загрузки идей:', err)
      });
  }

  private loadImageAsBlob(fileKey: string): Promise<string> {
    return new Promise((resolve, reject) => {
      this.http.get(`http://localhost:8080/api/files/${fileKey}/view`, {
        responseType: 'blob',
        withCredentials: true
      }).subscribe({
        next: (blob) => {
          console.log(`✓ Изображение загружено: ${fileKey}, размер:`, blob.size);
          const blobUrl = URL.createObjectURL(blob);
          console.log(`✓ BlobUrl создан:`, blobUrl);
          resolve(blobUrl);
        },
        error: (err) => {
          console.error(`✗ Ошибка загрузки изображения ${fileKey}:`, err);
          reject(err);
        }
      });
    });
  }


  private mapStatusToBackend(status: string): string {
    const map: Record<string, string> = {
      'Создан': 'CREATED',
      'В работе': 'IN_WORK',
      'Готово': 'DONE',
      'Отклонено': 'CANCELLED'
    };

    return map[status] ?? 'ALL';
  }

  // CSS классы
  getStatusClass(status: IdeaStatus) {
    switch (status) {
      case IdeaStatus.DONE: return 'status-done';
      case IdeaStatus.IN_WORK: return 'status-progress';
      case IdeaStatus.CREATED: return 'status-new';
      case IdeaStatus.CANCELLED: return 'status-rejected';
      default: return 'status-default';
    }
  }

  getIdeaImageUrl(idea: IdeaWithStats, fileKey: string): string | SafeUrl {
    return idea.imageUrls?.[fileKey] || this.ideaService.getFileViewUrl(fileKey);
  }

  selectTab(event: any, status: string) {
    this.selected = status;

    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');

    this.page = 0;
    this.updateQueryPage();
    this.loadIdeas(this.selectedProject!);
  }

  toggleLike(idea: IdeaWithStats) {
    if (idea.userVote === 1) {
      this.ideaService.doVote({ ideaId: idea.idea.id, like: 0 }).subscribe(() => {});
      idea.likes = idea.likes - 1;
      idea.userVote = 0;
    } else if (idea.userVote === -1) {
      this.ideaService.doVote({ ideaId: idea.idea.id, like: 1 }).subscribe(() => {});
      idea.disLikes = idea.disLikes - 1;
      idea.likes = idea.likes + 1;
      idea.userVote = 1;
    } else {
      this.ideaService.doVote({ ideaId: idea.idea.id, like: 1 }).subscribe(() => {});
      idea.likes = idea.likes + 1;
      idea.userVote = 1;
    }
  }

  toggleDislike(idea: IdeaWithStats) {
    if (idea.userVote === -1) {
      idea.userVote = 0;
      this.ideaService.doVote({ ideaId: idea.idea.id, like: 0 }).subscribe(() => {});
      idea.disLikes = idea.disLikes - 1;
    } else if (idea.userVote === 1) {
      this.ideaService.doVote({ ideaId: idea.idea.id, like: -1 }).subscribe(() => {});
      idea.likes = idea.likes - 1;
      idea.disLikes = idea.disLikes + 1;
      idea.userVote = -1;
    } else {
      this.ideaService.doVote({ ideaId: idea.idea.id, like: -1 }).subscribe(() => {});
      idea.disLikes = idea.disLikes + 1;
      idea.userVote = -1;
    }
  }


  //Пагинация
  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.updateQueryPage();
      this.loadIdeas(this.selectedProject!);
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.updateQueryPage();
      this.loadIdeas(this.selectedProject!);
    }
  }

  openImageModal(imageUrl: SafeUrl | null | undefined) {
    if (!imageUrl) return;
    this.selectedImageUrl = imageUrl;
    this.isImageModalOpen = true;
  }

  closeImageModal() {
    this.selectedImageUrl = null;
    this.isImageModalOpen = false;
  }

  openQrModal() {
    if (!this.selectedProject) return;
    this.shareUrl = `${window.location.origin}/main/ideas/${this.selectedProject.title}`;
    this.qrCodeUrl = this.qrCodeService.generateQrCodeUrl(this.shareUrl, 350);
    this.isQrModalOpen = true;
  }

  closeQrModal() {
    this.isQrModalOpen = false;
  }

  copyShareLink() {
    this.qrCodeService.copyToClipboard(this.shareUrl).then(() => {
      // Можно добавить уведомление о копировании
      console.log('Link copied to clipboard');
    });
  }

  createIdea() {
    this.router.navigate(['/main/ideas/create']);
  }

  viewIdea(ideaId: number) {
    this.router.navigate(['/main/ideas/view', ideaId], { queryParams: { page: this.page } });
  }

  private updateQueryPage() {
    // update the URL query param without reloading the route
    this.router.navigate([], { relativeTo: this.route, queryParams: { page: this.page }, queryParamsHandling: 'merge' });
  }

  canDeleteIdea(): boolean {
    return this.selectedProject?.roleOfUser === UserRole.OWNER || this.selectedProject?.roleOfUser === UserRole.MANAGER;
  }

  isProjectOwner(): boolean {
    return this.selectedProject?.roleOfUser === UserRole.OWNER;
  }

  openProjectSettings() {
    if (this.selectedProject) {
      this.router.navigate(['/main/project-settings', this.selectedProject.title]);
    }
  }

  openDeleteConfirm(ideaId: number, event: Event) {
    event.stopPropagation();
    this.deleteIdeaId = ideaId;
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.deleteIdeaId = null;
  }

  confirmDeleteIdea() {
    if (!this.deleteIdeaId) return;

    this.isDeleting = true;
    const ideaIdToDelete = this.deleteIdeaId;

    this.ideaService.deleteIdea({ ideaId: ideaIdToDelete }).subscribe({
      next: () => {
        // Удаляем идею из списка
        this.ideas = this.ideas.filter(idea => idea.idea.id !== ideaIdToDelete);
        this.totalElements--;
        
        // Если на странице нет больше идей, переходим на предыдущую
        if (this.ideas.length === 0 && this.page > 0) {
          this.page--;
          this.updateQueryPage();
        }

        this.closeDeleteConfirm();
        this.isDeleting = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при удалении идеи:', err);
        this.isDeleting = false;
        this.cdr.detectChanges();
      }
    });
  }

  //Затычка
  doNothing() { }

  openChat(nickname: string, event: Event) {
    event.stopPropagation();
    this.router.navigate(['/main/messages'], { queryParams: { chat: nickname } });
  }

  // Avatar upload
  loadProjectAvatar(project: Project) {
    // Use fileId (new format) or avatarKey (old format)
    const avatarKey = project.avatarKey || (project.fileId ? project.fileId.toString() : null);
    if (!avatarKey) {
      this.projectAvatarUrl = null;
      return;
    }

    this.http.get(`http://localhost:8080/api/files/${avatarKey}/view`, {
      responseType: 'blob',
      withCredentials: true
    }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        this.projectAvatarUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
      },
      error: (err) => {
        console.error('Error loading project avatar:', err);
        this.projectAvatarUrl = null;
      }
    });
  }

  openAvatarModal() {
    if (!this.isProjectOwner()) return;
    this.isAvatarModalOpen = true;
  }

  closeAvatarModal() {
    this.isAvatarModalOpen = false;
    this.selectedAvatarFile = null;
    this.cropperCanvas = null;
  }

  onAvatarFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedAvatarFile = input.files[0];
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        if (e.target?.result) {
          const img = new Image();
          img.onload = () => {
            this.initializeCropper(img);
          };
          img.src = e.target.result as string;
        }
      };
      reader.readAsDataURL(this.selectedAvatarFile);
    }
  }

  initializeCropper(img: HTMLImageElement) {
    const canvas = document.getElementById('avatarCropper') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d')!;
    const size = Math.min(img.width, img.height);
    
    canvas.width = 400;
    canvas.height = 400;

    const x = (img.width - size) / 2;
    const y = (img.height - size) / 2;

    ctx.drawImage(img, x, y, size, size, 0, 0, 400, 400);
    this.cropperCanvas = canvas;
  }

  saveProjectAvatar() {
    if (!this.selectedAvatarFile || !this.cropperCanvas || !this.selectedProject) return;

    this.isUploadingAvatar = true;
    this.cropperCanvas.toBlob((blob) => {
      if (!blob) return;

      const formData = new FormData();
      formData.append('file', new File([blob], 'avatar.png', { type: 'image/png' }));

      this.http.post(`http://localhost:8080/api/files/upload`, formData, {
        withCredentials: true
      }).subscribe({
        next: (response: any) => {
          const fileId = response.id;
          
          this.projectService.updateProjectAvatar({
            projectId: this.selectedProject!.id,
            fileId: fileId
          }).subscribe({
            next: () => {
              this.selectedProject!.avatarKey = fileId;
              this.loadProjectAvatar(this.selectedProject!);
              this.closeAvatarModal();
              this.isUploadingAvatar = false;
              this.cdr.detectChanges();
            },
            error: (err) => {
              console.error('Error updating project avatar:', err);
              this.isUploadingAvatar = false;
              this.cdr.detectChanges();
            }
          });
        },
        error: (err) => {
          console.error('Error uploading avatar:', err);
          this.isUploadingAvatar = false;
          this.cdr.detectChanges();
        }
      });
    }, 'image/png');
  }
}
