import { Idea } from "./idea.interface";
import { SafeUrl } from "@angular/platform-browser";

export interface IdeaWithStats {
  idea: Idea;
  likes: number;
  disLikes: number;
  userVote: number;
  commentsCount: number;
  imageUrls?: Record<string, SafeUrl>;
}