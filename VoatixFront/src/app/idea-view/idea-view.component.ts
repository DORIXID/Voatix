import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { IdeaService } from '../service/idea.service';
import { ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'idea-view-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './idea-view.component.html',
  styleUrls: ['./idea-view.component.scss']
})
export class IdeaViewComponent implements OnInit {
  idea: any = null;
  likes = 0;
  disLikes = 0;
  userVote = 0;
  commentsCount = 0;
  avatarUrl: SafeUrl | null = null;
  imageUrls: Record<string, SafeUrl> = {};
  
  // modal for photos
  selectedImageUrl: SafeUrl | null = null;
  isImageModalOpen = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ideaService: IdeaService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    if (id) {
      this.loadIdea(id);
    }
  }

  loadIdea(id: number) {
    this.ideaService.getIdeaById(id).subscribe({
      next: (res) => {
        this.idea = res.idea;
        this.likes = res.likes ?? 0;
        this.disLikes = res.disLikes ?? 0;
        this.userVote = res.vote ?? 0;
        this.commentsCount = res.commentsCount ?? 0;
        // load avatar
        if (this.idea.avatarKey) {
          this.loadImage(this.idea.avatarKey, 'avatar');
        }
        // load file images
        if (this.idea.fileKeys?.length) {
          this.idea.fileKeys.forEach((key: string) => {
            this.loadImage(key, 'file');
          });
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка загрузки идеи', err);
      }
    });
  }

  loadImage(key: string, type: 'avatar' | 'file') {
    const url = this.ideaService.getFileViewUrl(key);
    this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        if (type === 'avatar') {
          this.avatarUrl = safeUrl;
        } else {
          this.imageUrls[key] = safeUrl;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка загрузки изображения', key, err);
      }
    });
  }

  back() {
    this.router.navigate(['/main/ideas']);
  }

  getAvatarUrl(key?: string) {
    if (!key || !this.avatarUrl) return '/assets/icons/default-avatar.png';
    return this.avatarUrl;
  }

  getImageUrl(key: string) {
    return this.imageUrls[key] || this.ideaService.getFileViewUrl(key);
  }

  totalScore() {
    return (this.likes || 0) - (this.disLikes || 0);
  }

  scoreClass() {
    const s = this.totalScore();
    if (s > 0) return 'score-positive';
    if (s < 0) return 'score-negative';
    return 'score-neutral';
  }

  toggleLike() {
    if (!this.idea) return;
    if (this.userVote === 1) {
      // Отменяем лайк
      this.ideaService.doVote(this.idea.id, 0).subscribe(() => {
        this.likes = Math.max(0, this.likes - 1);
        this.userVote = 0;
        this.cdr.detectChanges();
      });
    } else {
      // Ставим лайк (и убираем дизлайк если он был)
      this.ideaService.doVote(this.idea.id, 1).subscribe(() => {
        if (this.userVote === -1) {
          this.disLikes = Math.max(0, this.disLikes - 1);
          this.likes = this.likes + 1;
        } else {
          this.likes = this.likes + 1;
        }
        this.userVote = 1;
        this.cdr.detectChanges();
      });
    }
  }

  toggleDislike() {
    if (!this.idea) return;
    if (this.userVote === -1) {
      // Отменяем дизлайк
      this.ideaService.doVote(this.idea.id, 0).subscribe(() => {
        this.disLikes = Math.max(0, this.disLikes - 1);
        this.userVote = 0;
        this.cdr.detectChanges();
      });
    } else {
      // Ставим дизлайк (и убираем лайк если он был)
      this.ideaService.doVote(this.idea.id, -1).subscribe(() => {
        if (this.userVote === 1) {
          this.likes = Math.max(0, this.likes - 1);
          this.disLikes = this.disLikes + 1;
        } else {
          this.disLikes = this.disLikes + 1;
        }
        this.userVote = -1;
        this.cdr.detectChanges();
      });
    }
  }

  goToComments() {
    // placeholder — route for comments will be added later
    this.router.navigate(['/main/ideas']);
  }

  openImageModal(imageUrl: SafeUrl) {
    this.selectedImageUrl = imageUrl;
    this.isImageModalOpen = true;
  }

  closeImageModal() {
    this.isImageModalOpen = false;
    this.selectedImageUrl = null;
  }
}
