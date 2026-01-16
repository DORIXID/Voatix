import { IdeaStatus } from "../enums/idea-status.enum";

export interface Idea {
  id: number;
  title: string;
  description: string;
  dateTime: Date;
  status: IdeaStatus;
}

