import { Idea } from "./idea.interface";

export interface IdeaWithStats {
  idea: Idea;
  likes: number;
  disLikes: number;
  userVote: number;
  commentsCount: number;
}