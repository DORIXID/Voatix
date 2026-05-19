import { UserRole } from "../enums/user-role.enum";

export interface Project {
  // projectId removed — use `title` as identifier for deep links
  title: string;
  active: boolean;
  roleOfUser: UserRole;
  key: string; //это ключ для URL аватарки проекта. Например: http://localhost:8080/api/files/КЛЮЧ/view
  id?: number;
  avatarKey?: string;
  avatarId?: number;  // Numeric avatar ID (new format)
  fileId?: number;
}
