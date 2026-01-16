import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class IdeaService {
  constructor(private http: HttpClient) {}

  loadIdeas(project: string, page: number, limit: number, filterBy: string, searchedValue: string = '') {
    return this.http.get<any>('http://localhost:8080/api/base/ideas', {
      params: { project, page, limit, filterBy, searchedValue },
      withCredentials: true
    });
  }
}
