import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, shareReplay } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class FileService {
  private imageCache = new Map<string, BehaviorSubject<string | null>>();

  constructor(private http: HttpClient) {}

  /**
   * Загружает изображение с авторизацией и конвертирует его в data URL
   * Результат кэшируется
   */
  getImageDataUrl(fileKey: string): Observable<string | null> {
    // Проверяем кэш
    if (this.imageCache.has(fileKey)) {
      return this.imageCache.get(fileKey)!.asObservable();
    }

    // Создаём subject для кэширования
    const subject = new BehaviorSubject<string | null>(null);
    this.imageCache.set(fileKey, subject);

    // Загружаем изображение как blob
    this.http
      .get(`http://localhost:8080/api/files/${encodeURIComponent(fileKey)}/view`, {
        responseType: 'blob',
        withCredentials: true
      })
      .subscribe({
        next: (blob) => {
          // Конвертируем blob в data URL
          const reader = new FileReader();
          reader.onload = () => {
            subject.next(reader.result as string);
          };
          reader.readAsDataURL(blob);
        },
        error: (err) => {
          console.error('Failed to load image:', fileKey, err);
          subject.next(null);
        }
      });

    return subject.asObservable();
  }
}
