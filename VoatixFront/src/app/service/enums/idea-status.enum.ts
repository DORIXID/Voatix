export enum IdeaStatus {
  DONE = 'DONE',
  IN_WORK = 'IN_WORK',
  CREATED = 'CREATED',
  CANCELLED = 'CANCELLED'
}

export const IdeaStatusRu: Record<IdeaStatus, string> = {
  [IdeaStatus.DONE]: 'Готово',
  [IdeaStatus.IN_WORK]: 'В работе',
  [IdeaStatus.CREATED]: 'Создан',
  [IdeaStatus.CANCELLED]: 'Отклонено'
};
