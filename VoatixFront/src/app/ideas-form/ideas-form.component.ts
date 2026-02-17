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
    private ideaService: IdeaService,
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

    this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;

      if (project) {
        this.loadIdeas(project);
      }
    });
  }

  loadIdeas(project: Project) {
    const filterBy = this.mapStatusToBackend(this.selected);

    this.ideaService
      .loadIdeas(project.title, this.page, this.limit, filterBy, this.searchedValue)
      .subscribe({
        next: (data) => {
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
          this.ideas.forEach(idea => {
            if (idea.idea.fileKeys && idea.idea.fileKeys.length > 0) {
              idea.idea.fileKeys.slice(0, 5).forEach(fileKey => {
                this.loadImageAsBlob(fileKey).then(blobUrl => {
                  if (!idea.imageUrls) idea.imageUrls = {};
                  idea.imageUrls[fileKey] = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
                  console.log(`✓ Image URL установлена для ${fileKey}:`, idea.imageUrls[fileKey]);
                  this.cdr.detectChanges();
                }).catch(err => {
                  console.error(`✗ Failed to load image ${fileKey}:`, err);
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
      this.ideaService.doVote(idea.idea.id, 0).subscribe(() => {});
      idea.likes = idea.likes - 1;
      idea.userVote = 0;
    } else if (idea.userVote === -1) {
      this.ideaService.doVote(idea.idea.id, 1).subscribe(() => {});
      idea.disLikes = idea.disLikes - 1;
      idea.likes = idea.likes + 1;
      idea.userVote = 1;
    } else {
      this.ideaService.doVote(idea.idea.id, 1).subscribe(() => {});
      idea.likes = idea.likes + 1;
      idea.userVote = 1;
    }
  }

  toggleDislike(idea: IdeaWithStats) {
    if (idea.userVote === -1) {
      idea.userVote = 0;
      this.ideaService.doVote(idea.idea.id, 0).subscribe(() => {});
      idea.disLikes = idea.disLikes - 1;
    } else if (idea.userVote === 1) {
      this.ideaService.doVote(idea.idea.id, -1).subscribe(() => {});
      idea.likes = idea.likes - 1;
      idea.disLikes = idea.disLikes + 1;
      idea.userVote = -1;
    } else {
      this.ideaService.doVote(idea.idea.id, -1).subscribe(() => {});
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

    this.ideaService.deleteIdea(ideaIdToDelete).subscribe({
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
}
