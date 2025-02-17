// /types/enums/qnaStatus.ts
export enum QnaStatus {
  WAITING = 'WAITING',
  COMPLETED = 'COMPLETED',
}

export const QnaStatusDescriptions: Record<QnaStatus, string> = {
  [QnaStatus.WAITING]: '답변대기',
  [QnaStatus.COMPLETED]: '답변완료',
};

export const QnaStatusTypes: Record<QnaStatus, string> = {
  [QnaStatus.WAITING]: 'warning', // 답변대기 - 주황색
  [QnaStatus.COMPLETED]: 'success', // 답변완료 - 초록색
};
