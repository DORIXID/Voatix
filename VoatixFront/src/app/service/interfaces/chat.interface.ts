export interface Chat {
  userNickname: string;
  userId?: number;
  avatarId?: number;
  fileId?: number;
  avatarKey?: string;
  // Optional fields from backend response
  senderNickname?: string;
  text?: string;
  dateTime?: Date | string;
  unreadCount?: number;
}
