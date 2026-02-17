import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { HttpClient, HttpParams, HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { ProjectService } from '../service/project.service';
import { Project } from '../service/interfaces/project.interface';
import { QrCodeService } from '../service/qr-code.service';
import { SurveyService } from '../service/survey.service';
import { UserRole } from '../service/enums/user-role.enum';

@Component({
  selector: 'surveys-form-component',
  standalone: true,
  imports: [MatFormFieldModule,
    CommonModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    MatButtonToggleModule,
    HttpClientModule],
  templateUrl: './surveys-form.component.html',
  styleUrls: ['./surveys-form.component.scss']
})

export class SurveysSidebarFormComponent implements OnInit, OnDestroy {

  survey: any = null;
  page = 0;
  limit = 1;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  project = '';
  selectedProject: Project | null = null;

  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  isQrModalOpen = false;
  qrCodeUrl: string = '';
  shareUrl: string = '';

  // Delete survey
  showDeleteBtn = false;
  showDeleteConfirm = false;
  isDeletingSurvey = false;
  userRole: UserRole = UserRole.VIEWER;

  private subs = new Subscription();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private projectService: ProjectService,
    private router: Router,
    private route: ActivatedRoute,
    private qrCodeService: QrCodeService,
    private surveyService: SurveyService
  ) { }

  ngOnInit(): void {
    // Сначала проверяем URL параметры
    this.subs.add(this.route.paramMap.subscribe(params => {
      const urlProjectTitle = params.get('projectTitle');
      if (urlProjectTitle && urlProjectTitle !== this.project) {
        this.project = urlProjectTitle;
        this.page = 0;
        this.loadSurvey();
      }
      this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;
    })
    }));

    // Затем проверяем выбранный проект в сервисе
    const current: Project | null = this.projectService.getSelectedProject();
    if (current && current.title && !this.project) {
      this.project = current.title;
      this.userRole = current.roleOfUser || UserRole.VIEWER;
      this.loadSurvey();
    }

    this.showDeleteBtn = this.canDeleteSurvey();

    // Подписываемся на смену проекта через сервис
    this.subs.add(this.projectService.selectedProject$.subscribe(p => {
      const key = p?.title || '';
      if (key && key !== this.project) {
        this.project = key;
        this.userRole = p?.roleOfUser || UserRole.VIEWER;
        this.page = 0;
        this.loadSurvey();
      }
    }));
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadSurvey(): void {
    this.loading = true;
    const params = new HttpParams()
      .set('project', this.project)
      .set('page', String(this.page))
      .set('limit', String(this.limit));

    this.http.get<any>('http://localhost:8080/api/surveys', { params }).subscribe({
      next: res => {
        console.log('surveys response', res);
        if (res && res.content && res.content.length > 0) {
          const s = res.content[0];
          const totalVotes = (s.votingEstimates || []).reduce((acc: number, v: any) => acc + (v.votesCount || 0), 0);
          const points = (s.votingEstimates || []).map((v: any) => {
            const percent = totalVotes > 0 ? Math.round(((v.votesCount || 0) * 100) / totalVotes) : 0;
            return {
              id: v.id,
              title: v.title,
              votes: v.votesCount || 0,
              percent,
              selectedByCurrentUser: !!v.isVoted
            };
          });

          this.survey = {
            id: s.id,
            title: s.title,
            description: s.description,
            startDate: s.startDate,
            endDate: s.endDate,
            type: s.type,
            totalVotes,
            points
          };
          console.log('mapped survey', this.survey);
        } else {
          this.survey = null;
        }

        this.totalPages = res && typeof res.totalPages !== 'undefined' ? res.totalPages : 0;
        this.totalElements = res && typeof res.totalElements !== 'undefined' ? res.totalElements : 0;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to load surveys', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  next(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadSurvey();
    }
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.loadSurvey();
    }
  }

  doNothing() { }

  onToggleOption(point: any): void {
    if (!this.survey) { return; }
    this.votePoint(point.id);
  }

  goToCreate(): void {
    this.router.navigate(['/main/surveys/create']);
  }

  showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.toastVisible = false;
      this.cdr.detectChanges();
    }, 3000);
  }

  closeToast(): void {
    this.toastVisible = false;
    this.cdr.detectChanges();
  }

  openQrModal(): void {
    this.shareUrl = `${window.location.origin}/main/surveys/${this.project}`;
    this.qrCodeUrl = this.qrCodeService.generateQrCodeUrl(this.shareUrl, 350);
    this.isQrModalOpen = true;
  }

  closeQrModal(): void {
    this.isQrModalOpen = false;
  }

  copyShareLink(): void {
    this.qrCodeService.copyToClipboard(this.shareUrl).then(() => {
      console.log('Link copied to clipboard');
    });
  }

  private votePoint(votingEstimateId: number) {
    this.http.post<any>(`http://localhost:8080/api/surveys/vote/${votingEstimateId}`, {}, { withCredentials: true }).subscribe({
      next: () => this.loadSurvey(),
      error: err => {
        console.error('Vote failed', err);
        // Проверяем на ошибку 410 (Gone/истёк срок голосования)
        if (err.status === 410) {
          this.showToast('Срок голосования истёк', 'error');
        } else {
          this.showToast('Ошибка при голосовании', 'error');
        }
        this.loadSurvey();
      }
    });
  }

  canDeleteSurvey(): boolean {
    return this.selectedProject?.roleOfUser === UserRole.OWNER || this.selectedProject?.roleOfUser === UserRole.MANAGER;
  }

  openDeleteConfirm(): void {
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm = false;
  }

  deleteSurvey(): void {
    if (!this.survey) return;

    this.isDeletingSurvey = true;
    const surveyIdToDelete = this.survey.id;

    this.surveyService.deleteSurvey(surveyIdToDelete).subscribe({
      next: () => {
        this.showToast('Голосование удалено', 'success');
        this.closeDeleteConfirm();
        this.isDeletingSurvey = false;
        // Загружаем следующее голосование
        this.loadSurvey();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при удалении голосования', err);
        this.showToast('Ошибка при удалении голосования', 'error');
        this.isDeletingSurvey = false;
        this.cdr.detectChanges();
      }
    });
  }
}
