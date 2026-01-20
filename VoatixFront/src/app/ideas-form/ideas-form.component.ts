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
import localeRu from '@angular/common/locales/ru';
import { registerLocaleData } from '@angular/common';
import { Project } from '../service/interfaces/project.interface';
import { ProjectService } from '../service/project.service';
import { IdeaWithStats } from '../service/interfaces/idea-with-stats.interface';

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

  // для шаблона
  IdeaStatusRu = IdeaStatusRu;

  @ViewChild('ideasList') ideasList!: ElementRef;

  constructor(
    private projectService: ProjectService,
    private ideaService: IdeaService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;

      if (project) {
        this.page = 0;
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
