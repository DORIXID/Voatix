import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root' })
export class SurveyService {
  constructor(private http: HttpClient) { }

  getSurveys(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/surveys/get', payload, { withCredentials: true });
  }

  createSurvey(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/surveys/new', payload, { withCredentials: true });
  }

  voteSurvey(payload: any) {
    return this.http.post<any>('http://localhost:8080/api/surveys/vote', payload, { withCredentials: true });
  }

  deleteSurvey(payload: any) {
    return this.http.delete<any>('http://localhost:8080/api/surveys', {
      body: payload,
      withCredentials: true
    });
  }
}
