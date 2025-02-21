export enum FeedbackStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
}

export const FeedbackStatusDescriptions: Record<FeedbackStatus, string> = {
  [FeedbackStatus.PENDING]: '처리 대기',
  [FeedbackStatus.IN_PROGRESS]: '처리 중',
  [FeedbackStatus.COMPLETED]: '처리 완료',
};
