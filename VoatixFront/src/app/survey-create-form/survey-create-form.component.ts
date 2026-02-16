import { Component, OnInit } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ProjectService } from '../service/project.service';
import { Project } from '../service/interfaces/project.interface';

@Component({
  selector: 'survey-create-form-component',
  standalone: true,
  imports: [
    MatFormFieldModule,
    CommonModule,
    MatInputModule,
    FormsModule,
    MatIconModule,
    RouterModule,
    MatButtonToggleModule,
    HttpClientModule
  ],
  templateUrl: './survey-create-form.component.html',
  styleUrls: ['./survey-create-form.component.scss']
})
export class SurveyCreateFormComponent implements OnInit {
  // Парсит datetime-local строку в Date без учета таймзоны
  private parseDatetimeLocal(dateTimeStr: string): Date {
    if (!dateTimeStr) return new Date();
    // Формат: "2026-02-16T15:30"
    const parts = dateTimeStr.split('T');
    if (parts.length !== 2) return new Date();
    
    const [year, month, day] = parts[0].split('-').map(Number);
    const [hours, minutes] = parts[1].split(':').map(Number);
    
    return new Date(year, month - 1, day, hours, minutes, 0, 0);
  }

  // Конвертирует локальное время в ISO, учитывая смещение таймзоны
  private toISOStringWithLocalTime(date: Date): string {
    const offset = date.getTimezoneOffset();
    const adjustedDate = new Date(date.getTime() - offset * 60 * 1000);
    return adjustedDate.toISOString();
  }
  title: string = '';
  description: string = '';
  type: string = 'RADIO_BUTTON';
  startDateStr: string = '';
  endDateStr: string = '';
  votingPoints: Array<{ title: string }> = [
    { title: '' },
    { title: '' }
  ];

  submitting = false;
  toastVisible = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  selectedProject: Project | null = null;

  constructor(
    private http: HttpClient,
    private projectService: ProjectService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.projectService.selectedProject$.subscribe(project => {
      this.selectedProject = project;
    });
  }

  back(): void {
    this.router.navigate(['/main/surveys']);
  }

  addOption(): void {
    this.votingPoints.push({ title: '' });
  }

  removeOption(index: number): void {
    if (this.votingPoints.length > 2) {
      this.votingPoints.splice(index, 1);
    }
  }

  showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    setTimeout(() => {
      this.toastVisible = false;
    }, 3000);
  }

  validateForm(): boolean {
    if (!this.title.trim()) {
      this.showToast('Введите название голосования', 'error');
      return false;
    }
    if (this.title.length > 100) {
      this.showToast('Название не может быть больше 100 символов', 'error');
      return false;
    }
    if (this.description.length > 500) {
      this.showToast('Описание не может быть больше 500 символов', 'error');
      return false;
    }
    if (!this.endDateStr) {
      this.showToast('Выберите конец голосования', 'error');
      return false;
    }

    const now = new Date();
    let startDate: Date;
    
    if (this.startDateStr) {
      startDate = this.parseDatetimeLocal(this.startDateStr);
      if (startDate <= now) {
        this.showToast('Начало голосования должно быть в будущем', 'error');
        return false;
      }
    } else {
      // Используем текущее время + 2-3 минуты
      startDate = new Date(now.getTime() + 2.5 * 60 * 1000);
    }

    const endDate = this.parseDatetimeLocal(this.endDateStr);
    if (endDate <= startDate) {
      this.showToast('Конец голосования должен быть после начала', 'error');
      return false;
    }

    const filledOptions = this.votingPoints.filter(o => o.title.trim());
    if (filledOptions.length < 2) {
      this.showToast('Минимум 2 варианта голосования', 'error');
      return false;
    }
    if (filledOptions.length > 20) {
      this.showToast('Максимум 20 вариантов голосования', 'error');
      return false;
    }

    return true;
  }

  submitSurvey(): void {
    if (!this.validateForm()) {
      return;
    }

    this.submitting = true;

    // Расчет начальной даты
    const now = new Date();
    let startDate: Date;
    if (this.startDateStr) {
      startDate = this.parseDatetimeLocal(this.startDateStr);
    } else {
      // текущее время + 2-3 минуты
      startDate = new Date(now.getTime() + 2.5 * 60 * 1000);
    }

    const endDate = this.parseDatetimeLocal(this.endDateStr);

    const payload = {
      title: this.title.trim(),
      description: this.description.trim(),
      type: this.type,
      startDate: this.toISOStringWithLocalTime(startDate),
      endDate: this.toISOStringWithLocalTime(endDate),
      projectName: this.selectedProject?.title || '',
      votingPoints: this.votingPoints
        .filter(o => o.title.trim())
        .map(o => ({ title: o.title.trim() }))
    };

    this.http.post<any>('http://localhost:8080/api/surveys/new', payload, {
      withCredentials: true
    }).subscribe({
      next: (res) => {
        this.submitting = false;
        this.showToast('Голосование успешно создано!', 'success');
        setTimeout(() => {
          this.router.navigate(['/main/surveys']);
        }, 1500);
      },
      error: (err) => {
        this.submitting = false;
        const errorMessage = err?.error?.message || 'Ошибка при создании голосования';
        this.showToast(errorMessage, 'error');
      }
    });
  }
}
