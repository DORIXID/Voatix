import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class SurveyService {
  constructor(private http: HttpClient) { }

  deleteSurvey(id: number) {
    return this.http.delete<any>(`http://localhost:8080/api/surveys/${id}`, { withCredentials: true });
  }
}
