import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class IdeaService {
  constructor(private http: HttpClient) { }

  loadIdeas(projectId: number, page: number, limit: number, filterBy: string, search: string = '') {
    const payload = {
      projectId,
      page,
      limit,
      filterBy,
      search
    };
    return this.http.post<any>('http://localhost:8080/api/ideas/get', payload, { withCredentials: true });
  }

  getIdeaById(id: number) {
    return this.http.get<any>(`http://localhost:8080/api/ideas/${id}`, { withCredentials: true });
  }

  createIdea(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/ideas', payload, { withCredentials: true });
  }

  updateIdea(payload: any) {
    return this.http.patch<any>('http://localhost:8080/api/ideas', payload, { withCredentials: true });
  }

  updateIdeaStatus(payload: any) {
    return this.http.patch<any>('http://localhost:8080/api/ideas/status', payload, { withCredentials: true });
  }

  deleteIdea(payload: any) {
    return this.http.delete<any>('http://localhost:8080/api/ideas', {
      body: payload,
      withCredentials: true
    });
  }

  doVote(payload: any) {
    return this.http.put<any>('http://localhost:8080/api/ideas/like', payload, { withCredentials: true });
  }

  uploadFile(file: File) {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<any>('http://localhost:8080/api/files/upload', form, {
      withCredentials: true,
      reportProgress: true,
      observe: 'events'
    });
  }

  deleteFile(key: string) {
    return this.http.delete<any>(`http://localhost:8080/api/files/${encodeURIComponent(key)}`, { withCredentials: true });
  }

  getFileViewUrl(key: string) {
    return `http://localhost:8080/api/files/${encodeURIComponent(key)}/view`;
  }

  // Comments
  getComments(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/comments/get', payload, { withCredentials: true });
  }

  createComment(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/comments/new', payload, { withCredentials: true });
  }

  updateComment(payload: any) {
    return this.http.patch<any>('http://localhost:8080/api/comments', payload, { withCredentials: true });
  }

  deleteComment(payload: any) {
    return this.http.delete<any>('http://localhost:8080/api/comments', {
      body: payload,
      withCredentials: true
    });
  }

  likeComment(payload: any) {
    return this.http.put<any>('http://localhost:8080/api/comments/like', payload, { withCredentials: true });
  }

}
