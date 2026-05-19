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
import { HttpEventType } from '@angular/common/http';
import { Router } from '@angular/router';
import localeRu from '@angular/common/locales/ru';
import { registerLocaleData } from '@angular/common';
import { Project } from '../service/interfaces/project.interface';
import { ProjectService } from '../service/project.service';
import { IdeaWithStats } from '../service/interfaces/idea-with-stats.interface';

registerLocaleData(localeRu);

@Component({
  selector: 'idea-create-form-component',
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
  templateUrl: './idea-create-form.component.html',
  styleUrls: ['./idea-create-form.component.scss']
})

export class IdeaCreateFormComponent implements OnInit {

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

  // form fields
  title: string = '';
  description: string = '';
  submitting: boolean = false;
  // validation limits (match backend)
  titleMin = 10;
  titleMax = 50;
  descMin = 30;
  descMax = 1600;
  
  // files with upload state
  selectedFiles: Array<{
    file?: File;
    name: string;
    key?: string;
    fileId?: number;  // Backend fileId (new format)
    progress: number;
    status: 'pending' | 'uploading' | 'done' | 'error';
  }> = [];

  isDragOver = false;
  maxFiles = 5;
  // toast
  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // для шаблона
  IdeaStatusRu = IdeaStatusRu;

  @ViewChild('ideasList') ideasList!: ElementRef;

  constructor(
    private projectService: ProjectService,
    private ideaService: IdeaService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) { }

  back() {
    this.router.navigate(['/main/ideas']);
  }

  ngOnInit() {
    this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;

      if (project) {
        this.page = 0;
        this.loadIdeas(project);
      }
    });
  }

  submitIdea() {
    if (!this.selectedProject) {
      alert('Проект не выбран');
      return;
    }

    const trimmedTitle = this.title?.trim();
    const trimmedDescription = this.description?.trim();

    if (!trimmedTitle) {
      alert('Введите название идеи');
      return;
    }

    if (!trimmedDescription) {
      alert('Введите описание идеи');
      return;
    }

    // client-side length validation matching backend constraints
    if (trimmedTitle.length < this.titleMin) {
      this.showToast(`Название должно содержать не менее ${this.titleMin} символов (сейчас ${trimmedTitle.length})`, 'error');
      return;
    }
    if (trimmedTitle.length > this.titleMax) {
      this.showToast(`Название должно содержать не более ${this.titleMax} символов (сейчас ${trimmedTitle.length})`, 'error');
      return;
    }
    if (trimmedDescription.length < this.descMin) {
      this.showToast(`Описание должно содержать не менее ${this.descMin} символов (сейчас ${trimmedDescription.length})`, 'error');
      return;
    }
    
    if (!this.selectedProject?.id) {
      this.showToast('Невозможно создать идею: у проекта отсутствует идентификатор (projectId).', 'error');
      return;
    }
    if (trimmedDescription.length > this.descMax) {
      this.showToast(`Описание должно содержать не более ${this.descMax} символов (сейчас ${trimmedDescription.length})`, 'error');
      return;
    }

    // Prepare numeric fileIds array (prefer fileId, fall back to numeric key when possible)
    const numericFileIds: number[] = this.selectedFiles
      .filter(f => (f.fileId !== undefined && f.fileId !== null) || (f.key && /^\d+$/.test(f.key)))
      .map(f => f.fileId ?? parseInt(f.key!, 10));

    const payload = {
      projectId: this.selectedProject.id,
      title: trimmedTitle,
      description: trimmedDescription,
      fileIds: numericFileIds
    };

    this.submitting = true;
    this.ideaService.createIdea(payload).subscribe({
      next: (res) => {
        this.submitting = false;
        this.title = '';
        this.description = '';
        if (this.selectedProject) {
          this.projectService.setProject(this.selectedProject);
        }
        this.showToast('Идея успешно создана', 'success');
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/main/ideas']), 1100);
      },
      error: (err) => {
        console.error('Ошибка при создании идеи', err);
        this.submitting = false;
        this.showToast('Не удалось создать идею', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    // auto-hide
    setTimeout(() => {
      this.toastVisible = false;
      this.cdr.detectChanges();
    }, 2500);
  }

  onFilesSelected(event: Event) {
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

  handleFiles(files: File[]) {
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
          if (event.type === HttpEventType.UploadProgress) {
            const loaded = event.loaded ?? 0;
            const total = event.total ?? loaded;
            const percent = Math.round((loaded / total) * 100);
            this.selectedFiles[idx].progress = percent;
          } else if (event.type === HttpEventType.Response) {
            const res = event.body;
            // Backend returns either fileId (new) or key (old format)
            const fileId = res?.fileId ?? res?.id;
            const key = res?.key ?? res?.name ?? file.name;
            
            // Store both for backwards compatibility, but prefer fileId
            this.selectedFiles[idx].fileId = fileId;
            this.selectedFiles[idx].key = key;
            this.selectedFiles[idx].status = 'done';
            this.selectedFiles[idx].progress = 100;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Ошибка загрузки файла', file.name, err);
          this.selectedFiles[idx].status = 'error';
          this.cdr.detectChanges();
        }
      });
    });
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    const dt = event.dataTransfer;
    if (!dt) return;
    const files = Array.from(dt.files || []);
    if (files.length) {
      this.handleFiles(files);
    }
  }

  removeFile(index: number) {
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
          // remove locally to keep UX responsive
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

  loadIdeas(project: Project) {
    const filterBy = this.mapStatusToBackend(this.selected);

    this.ideaService
      .loadIdeas(project.id || 0, this.page, this.limit, filterBy, this.searchedValue)
      .subscribe({
        next: (data) => {
          this.ideas = data.content.map((i: any) => ({
            ...i,
            userVote: i.vote === 1
              ? 1
              : i.vote === -1
                ? -1
                : 0
          }));

          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;

          this.cdr.detectChanges();
          this.ideasList.nativeElement.scrollTo({ top: 0, behavior: 'smooth' });
        },
        error: (err) => console.error('Ошибка загрузки идей:', err)
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
      this.loadIdeas(this.selectedProject!);
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadIdeas(this.selectedProject!);
    }
  }

  //Затычка
  doNothing() { }
}
