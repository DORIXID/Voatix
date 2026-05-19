import { IdeaStatus } from "../enums/idea-status.enum";

export interface Idea {
  id: number;
  title: string;
  description: string;
  dateTime: Date;
  status: IdeaStatus;
  fileKeys?: string[];
  fileIds?: number[];
  projectId?: number;
  nickname?: string;
  userId?: number;        // Author's user ID
  avatarKey?: string;
  avatarId?: number;  // Numeric avatar ID (new format)
  fileId?: number;
}

