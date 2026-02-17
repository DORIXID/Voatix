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
import { UserRole } from '../service/enums/user-role.enum';
import { Observable } from 'rxjs';


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
    private fileService: FileService
  ) {}

  public selectedSection: String = 'ideas';

  projects: Project[] = [];
  selectedProject: Project | null = null;

  isOpen = false;
  projectAvatarUrls: Map<string, Observable<string | null>> = new Map();

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
    this.http.get<Project[]>('http://localhost:8080/api/projects', { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.projects = data;

          // Загружаем аватары для всех проектов
          this.projects.forEach(p => {
            if (p.key) {
              this.projectAvatarUrls.set(p.title, this.fileService.getImageDataUrl(p.key));
            }
          });

          // 3. Если в URL есть ключ проекта — выбираем соответствующий проект
          const child = this.route.snapshot.firstChild;
          const urlProjectTitle = child?.paramMap.get('projectTitle');

          if (urlProjectTitle) {
            let p = this.projects.find(pr => pr.title === urlProjectTitle);
            if (p) {
              this.selectProject(p);
            } else {
              // Проект не возвращён в общем списке — пытаемся получить его по title
              this.http.get<Project>(`http://localhost:8080/api/projects/${encodeURIComponent(urlProjectTitle)}`, { withCredentials: true })
                .subscribe({
                  next: (proj) => {
                    // Устанавливаем роль VIEWER для проектов, полученных по отдельному запросу
                    proj.roleOfUser = UserRole.VIEWER;
                    // Вставляем проект в список и выбираем его
                    this.projects.unshift(proj);
                    if (proj.key) {
                      this.projectAvatarUrls.set(proj.title, this.fileService.getImageDataUrl(proj.key));
                    }
                    this.selectProject(proj);
                    this.cdr.detectChanges();
                  },
                  error: (err) => {
                    console.warn('Не удалось загрузить проект по title из URL:', urlProjectTitle, err);
                    if (!this.projectService.getSelectedProject() && this.projects.length > 0) {
                      this.selectProject(this.projects[0]);
                    }
                  }
                });
            }
          } else {
            // 4. Если проект ещё не выбран — выбираем первый
            if (!this.projectService.getSelectedProject() && this.projects.length > 0) {
              this.selectProject(this.projects[0]);
            }
          }
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Ошибка загрузки проектов:', err)
      });
  }

  openProject(projectTitle?: string) {
    console.log("Открыть проект:", projectTitle);
  }

  select(selectedSection: String) {
    this.selectedSection = selectedSection;
  }

  clickOnIdeas() {
    this.select('ideas');
    if (this.selectedProject) {
      this.router.navigate(['/main/ideas', this.selectedProject.title]);
    } else {
      this.router.navigate(['/main/ideas']);
    }
  }

  clickOnSurveys() {
    this.select('surveys');
    if (this.selectedProject) {
      this.router.navigate(['/main/surveys', this.selectedProject.title]);
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

    this.openProject(p.title);
    // Обновляем URL для возможностей шаринга Deep Link (используем title проекта)
    if (this.selectedSection === 'ideas') {
      this.router.navigate(['/main/ideas', p.title]);
    } else if (this.selectedSection === 'surveys') {
      this.router.navigate(['/main/surveys', p.title]);
    }
  }

  getProjectAvatarUrl(project: Project | null): Observable<string | null> {
    if (!project || !project.key) {
      return new Observable(observer => {
        observer.next(null);
        observer.complete();
      });
    }
    
    if (!this.projectAvatarUrls.has(project.title)) {
      this.projectAvatarUrls.set(project.title, this.fileService.getImageDataUrl(project.key));
    }
    
    return this.projectAvatarUrls.get(project.title)!;
  }
}
