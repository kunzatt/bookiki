// 문의사항 유형
export enum QnaType {
  NORMAL = 'NORMAL',
  NEW_BOOK = 'NEW_BOOK',
  CHANGE_INFO = 'CHANGE_INFO',
}

export const QnaTypeDescriptions: Record<QnaType, string> = {
  [QnaType.NORMAL]: '일반',
  [QnaType.NEW_BOOK]: '희망도서 신청',
  [QnaType.CHANGE_INFO]: '회원정보 변경',
};
