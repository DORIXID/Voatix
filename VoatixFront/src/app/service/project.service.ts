import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Project } from './interfaces/project.interface';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private selectedProjectSubject = new BehaviorSubject<Project | null>(null);
  selectedProject$ = this.selectedProjectSubject.asObservable();

  constructor(private http: HttpClient) {}

  setProject(project: Project) {
    this.selectedProjectSubject.next(project);
  }

  getSelectedProject(): Project | null {
    return this.selectedProjectSubject.value;
  }

  clearProject() {
    this.selectedProjectSubject.next(null);
  }

  // API methods
  getProjects() {
    return this.http.get<any>('http://localhost:8080/api/projects', { withCredentials: true });
  }

  getProjectById(id: number) {
    return this.http.get<any>(`http://localhost:8080/api/projects/${id}`, { withCredentials: true });
  }

  createProject(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/projects', payload, { withCredentials: true });
  }

  updateProjectAvatar(payload: any) {
    return this.http.patch<any>('http://localhost:8080/api/projects/avatar', payload, { withCredentials: true });
  }

  deleteProject(payload: any) {
    return this.http.delete<any>('http://localhost:8080/api/projects', {
      body: payload,
      withCredentials: true
    });
  }

  addModerator(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/projects/moderators', payload, { withCredentials: true });
  }

  deleteModerator(payload: any) {
    return this.http.delete<any>('http://localhost:8080/api/projects/moderators', {
      body: payload,
      withCredentials: true
    });
  }
}
