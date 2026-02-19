import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Project } from './interfaces/project.interface';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private selectedProjectSubject = new BehaviorSubject<Project | null>(null);
  selectedProject$ = this.selectedProjectSubject.asObservable();

  setProject(project: Project) {
    this.selectedProjectSubject.next(project);
  }

  getSelectedProject(): Project | null {
    return this.selectedProjectSubject.value;
  }

  clearProject() {
    this.selectedProjectSubject.next(null);
  }
}
