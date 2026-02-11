import { UserRole } from "../enums/user-role.enum";

export interface Project {
  projectId: number;
  title: string;
  active: boolean;
  roleOfUser: UserRole;
  key: string;
}
