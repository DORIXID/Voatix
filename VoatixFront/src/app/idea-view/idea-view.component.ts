import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { IdeaService } from '../service/idea.service';
import { ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ProjectService } from '../service/project.service';
import { UserRole } from '../service/enums/user-role.enum';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'idea-view-component',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  templateUrl: './idea-view.component.html',
  styleUrls: ['./idea-view.component.scss']
})
export class IdeaViewComponent implements OnInit {
  @ViewChild('commentsSection') commentsSection: ElementRef | undefined;

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

  // Comments
  comments: any[] = [];
  page = 0;
  limit = 10;
  totalPages = 0;
  totalElements = 0;
  commentVotes: Record<number, number> = {};
  commentAvatarUrls: Record<string, SafeUrl> = {};
  // Comment creation form
  commentText: string = '';
  submittingComment: boolean = false;
  commentToastVisible: boolean = false;
  commentToastMessage: string = '';
  commentToastType: 'success' | 'error' = 'success';
  previousPage: number = 0;
  projectTitle: string = '';

  // Delete idea
  showDeleteConfirm = false;
  userRole: UserRole = UserRole.VIEWER;
  isDeleting = false;

  // Update idea status
  isUpdatingStatus = false;

  // Delete comment
  showDeleteCommentConfirm = false;
  deleteCommentId: number | null = null;
  isDeletingComment = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ideaService: IdeaService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private projectService: ProjectService
  ) {}

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    
    // read page from query params
    const pageParam = this.route.snapshot.queryParamMap.get('page');
    this.previousPage = pageParam ? Number(pageParam) : 0;

    // get project title from parent route
    const parentRoute = this.route.parent?.snapshot;
    this.projectTitle = parentRoute?.paramMap.get('projectTitle') || '';

    // get user role from project service
    const selectedProject = this.projectService.getSelectedProject();
    if (selectedProject) {
      this.userRole = selectedProject.roleOfUser;
    }

    if (id) {
      this.loadIdea(id);
      this.loadComments(id);
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
        if (type === 'avatar') {
          this.avatarUrl = null;
        }
        this.cdr.detectChanges();
      }
    });
  }

  back() {
    this.router.navigate(['/main/ideas', this.projectTitle], { queryParams: { page: this.previousPage } });
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
    this.router.navigate(['/main/ideas', this.projectTitle], { queryParams: { page: this.previousPage } });
  }

  loadComments(ideaId: number, pageNum: number = 0, shouldScroll: boolean = false) {
    const url = `http://localhost:8080/api/comments?ideaId=${ideaId}&page=${pageNum}&limit=${this.limit}`;
    this.http.get<any>(url, { withCredentials: true }).subscribe({
      next: (res) => {
        this.comments = res.content || [];
        this.page = res.number;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        // Initialize comment votes and load avatars
        res.content?.forEach((item: any) => {
          if (item.comment?.id) {
            this.commentVotes[item.comment.id] = item.vote ?? 0;
          }
          // Load comment avatar if not already loaded
          if (item.comment?.avatarKey && !this.commentAvatarUrls[item.comment.avatarKey]) {
            this.loadCommentAvatar(item.comment.avatarKey);
          }
        });
        this.cdr.detectChanges();
        // Scroll to comments section when explicitly requested
        if (shouldScroll) {
          this.scrollToComments();
        }
      },
      error: (err) => {
        console.error('Ошибка загрузки комментариев', err);
      }
    });
  }

  scrollToComments() {
    setTimeout(() => {
      if (this.commentsSection?.nativeElement) {
        this.commentsSection.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        // Fallback: scroll to top if section not found
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }, 150);
  }

  prevPage() {
    if (this.idea && this.page > 0) {
      this.loadComments(this.idea.id, this.page - 1, true);
    }
  }

  nextPage() {
    if (this.idea && this.page < this.totalPages - 1) {
      this.loadComments(this.idea.id, this.page + 1, true);
    }
  }

  toggleCommentLike(commentId: number, index: number) {
    const currentVote = this.commentVotes[commentId] ?? 0;
    const comment = this.comments[index];
    
    if (currentVote === 1) {
      // Отменяем лайк
      this.http.put(`http://localhost:8080/api/comments/${commentId}/likes?like=0`, {}, { withCredentials: true }).subscribe(() => {
        comment.likes = Math.max(0, comment.likes - 1);
        this.commentVotes[commentId] = 0;
        this.cdr.detectChanges();
      });
    } else {
      // Ставим лайк (и убираем дизлайк если он был)
      this.http.put(`http://localhost:8080/api/comments/${commentId}/likes?like=1`, {}, { withCredentials: true }).subscribe(() => {
        if (currentVote === -1) {
          comment.disLikes = Math.max(0, comment.disLikes - 1);
          comment.likes = comment.likes + 1;
        } else {
          comment.likes = comment.likes + 1;
        }
        this.commentVotes[commentId] = 1;
        this.cdr.detectChanges();
      });
    }
  }

  toggleCommentDislike(commentId: number, index: number) {
    const currentVote = this.commentVotes[commentId] ?? 0;
    const comment = this.comments[index];
    
    if (currentVote === -1) {
      // Отменяем дизлайк
      this.http.put(`http://localhost:8080/api/comments/${commentId}/likes?like=0`, {}, { withCredentials: true }).subscribe(() => {
        comment.disLikes = Math.max(0, comment.disLikes - 1);
        this.commentVotes[commentId] = 0;
        this.cdr.detectChanges();
      });
    } else {
      // Ставим дизлайк (и убираем лайк если он был)
      this.http.put(`http://localhost:8080/api/comments/${commentId}/likes?like=-1`, {}, { withCredentials: true }).subscribe(() => {
        if (currentVote === 1) {
          comment.likes = Math.max(0, comment.likes - 1);
          comment.disLikes = comment.disLikes + 1;
        } else {
          comment.disLikes = comment.disLikes + 1;
        }
        this.commentVotes[commentId] = -1;
        this.cdr.detectChanges();
      });
    }
  }

  getCommentAvatarUrl(key?: string) {
    if (!key) return '/assets/icons/default-avatar.png';
    return this.commentAvatarUrls[key] || '/assets/icons/default-avatar.png';
  }

  loadCommentAvatar(key: string) {
    const url = this.ideaService.getFileViewUrl(key);
    this.http.get(url, { responseType: 'blob', withCredentials: true }).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        const safeUrl = this.sanitizer.bypassSecurityTrustUrl(blobUrl);
        this.commentAvatarUrls[key] = safeUrl;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка загрузки аватара комментария', key, err);
        this.cdr.detectChanges();
      }
    });
  }

  getCommentAuthorAvatar(userId: number) {
    return '/assets/icons/default-avatar.png';
  }

  formatCommentDate(date: string) {
    return new Date(date).toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  }

  submitComment() {
    if (!this.idea || !this.commentText.trim()) return;

    this.submittingComment = true;
    const payload = {
      text: this.commentText.trim(),
      ideaId: this.idea.id
    };

    this.http.post<any>('http://localhost:8080/api/comments/new', payload, { withCredentials: true }).subscribe({
      next: (res) => {
        this.showCommentToast('Комментарий успешно создан!', 'success');
        this.commentText = '';
        this.submittingComment = false;
        // Reload comments from first page
        this.loadComments(this.idea.id, 0);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка создания комментария', err);
        this.showCommentToast('Ошибка при создании комментария', 'error');
        this.submittingComment = false;
        this.cdr.detectChanges();
      }
    });
  }

  clearCommentForm() {
    this.commentText = '';
  }

  showCommentToast(message: string, type: 'success' | 'error') {
    this.commentToastMessage = message;
    this.commentToastType = type;
    this.commentToastVisible = true;
    setTimeout(() => {
      this.commentToastVisible = false;
      this.cdr.detectChanges();
    }, 2500);
  }

  openImageModal(imageUrl: SafeUrl) {
    this.selectedImageUrl = imageUrl;
    this.isImageModalOpen = true;
  }

  closeImageModal() {
    this.isImageModalOpen = false;
    this.selectedImageUrl = null;
  }

  canDeleteIdea(): boolean {
    return this.userRole === UserRole.OWNER || this.userRole === UserRole.MANAGER;
  }

  updateIdeaStatus(newStatus: string) {
    if (!this.idea || this.isUpdatingStatus) return;

    if (this.idea.status === newStatus) return; // Same status, no need to update

    this.isUpdatingStatus = true;
    const payload = { status: newStatus };

    this.http.patch(
      `http://localhost:8080/api/ideas/${this.idea.id}/status`,
      payload,
      { withCredentials: true }
    ).subscribe({
      next: () => {
        this.idea.status = newStatus;
        this.isUpdatingStatus = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при изменении статуса идеи:', err);
        this.isUpdatingStatus = false;
        this.cdr.detectChanges();
      }
    });
  }

  openDeleteConfirm() {
    this.showDeleteConfirm = true;
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
  }

  deleteIdea() {
    if (!this.idea) return;
    
    this.isDeleting = true;
    this.ideaService.deleteIdea(this.idea.id).subscribe({
      next: () => {
        this.showCommentToast('Идея удалена', 'success');
        setTimeout(() => {
          this.router.navigate(['/main/ideas', this.projectTitle], { queryParams: { page: this.previousPage } });
        }, 500);
      },
      error: (err) => {
        console.error('Ошибка при удалении идеи', err);
        this.showCommentToast('Ошибка при удалении идеи', 'error');
        this.isDeleting = false;
        this.cdr.detectChanges();
      }
    });
  }

  openDeleteCommentConfirm(commentId: number) {
    this.deleteCommentId = commentId;
    this.showDeleteCommentConfirm = true;
  }

  closeDeleteCommentConfirm() {
    this.showDeleteCommentConfirm = false;
    this.deleteCommentId = null;
  }

  deleteComment() {
    if (!this.idea || !this.deleteCommentId) return;

    this.isDeletingComment = true;
    const commentIdToDelete = this.deleteCommentId;

    this.ideaService.deleteComment(this.idea.id, commentIdToDelete).subscribe({
      next: () => {
        // Удаляем комментарий из списка
        this.comments = this.comments.filter(item => item.comment.id !== commentIdToDelete);
        this.totalElements--;
        
        // Если на странице нет больше комментариев, переходим на предыдущую
        if (this.comments.length === 0 && this.page > 0) {
          this.page--;
          this.loadComments(this.idea.id, this.page, true);
        }

        this.showCommentToast('Комментарий удален', 'success');
        this.closeDeleteCommentConfirm();
        this.isDeletingComment = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Ошибка при удалении комментария', err);
        this.showCommentToast('Ошибка при удалении комментария', 'error');
        this.isDeletingComment = false;
        this.cdr.detectChanges();
      }
    });
  }

  openChat(nickname: string, event: Event) {
    event.stopPropagation();
    this.router.navigate(['/main/messages'], { queryParams: { chat: nickname } });
  }
}
