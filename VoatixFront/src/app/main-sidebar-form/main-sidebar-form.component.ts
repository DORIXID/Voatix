import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Router } from "@angular/router";
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Project } from '../service/interfaces/project.interface';
import { ProjectService } from '../service/project.service';


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
    private cdr: ChangeDetectorRef
  ) {}

  public selectedSection: String = 'ideas';

  projects: Project[] = [];
  selectedProject: Project | null = null;

  isOpen = false;

  ngOnInit() {

    // 1. Подписываемся на сервис — sidebar всегда знает актуальный проект
    this.projectService.selectedProject$.subscribe(p => {
      this.selectedProject = p;
    });

    // 2. Загружаем проекты
    this.loadProjects();
  }
  

  loadProjects() {
    this.http.get<Project[]>('http://localhost:8080/api/projects', { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.projects = data;

          // 3. Если проект ещё не выбран — выбираем первый
          if (!this.projectService.getSelectedProject() && this.projects.length > 0) {
            this.selectProject(this.projects[0]);
          }
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Ошибка загрузки проектов:', err)
      });
  }

  openProject(projectId: number) {
    console.log("Открыть проект:", projectId);
  }

  select(selectedSection: String) {
    this.selectedSection = selectedSection;
  }

  clickOnIdeas() {
    this.select('ideas');
    this.router.navigate(['/main/ideas']);
  }

  clickOnSurveys() {
    this.select('surveys');
    this.router.navigate(['/main/surveys']);
  }

  clickOnMessages() {
    this.select('messages');
    this.router.navigate(['/main/messages']);
  }

  clickOnExit() {
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

    this.openProject(p.projectId);
  }
}
