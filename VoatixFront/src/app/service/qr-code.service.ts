import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class QrCodeService {
  /**
   * Генерирует URL для QR кода через Google Charts API
   */
  generateQrCodeUrl(text: string, size: number = 300): string {
    const encoded = encodeURIComponent(text);
    return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encoded}`;
  }

  /**
   * Копирует текст в буфер обмена
   */
  copyToClipboard(text: string): Promise<void> {
    return navigator.clipboard.writeText(text);
  }
}
