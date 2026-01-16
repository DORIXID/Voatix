import { Idea } from "./idea.interface";

export interface IdeasPage {
  content: Idea[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
