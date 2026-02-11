import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class IdeaService {
  constructor(private http: HttpClient) { }

  loadIdeas(project: string, page: number, limit: number, filterBy: string, searchedValue: string = '') {
    return this.http.get<any>('http://localhost:8080/api/ideas', {
      params: { project, page, limit, filterBy, searchedValue },
      withCredentials: true
    });
  }

  doVote(ideaId: number, like: number) {
    return this.http.put<any>(
      `http://localhost:8080/api/ideas/${ideaId}/likes?like=${like}`,
      {},
      { withCredentials: true }
    );
  }

  createIdea(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/ideas', payload, { withCredentials: true });
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

  getIdeaById(id: number) {
    return this.http.get<any>(`http://localhost:8080/api/ideas/${id}`, { withCredentials: true });
  }

}
