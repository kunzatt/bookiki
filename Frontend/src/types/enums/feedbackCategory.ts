// types/enums/feedbackCategory.ts
export enum FeedbackCategory {
  QR_ERROR = 'QR_ERROR',
  LED_ERROR = 'LED_ERROR',
  CAMERA_ERROR = 'CAMERA_ERROR',
  BOOK_LOCATION = 'BOOK_LOCATION',
  SYSTEM_ERROR = 'SYSTEM_ERROR',
  CHATBOT_ERROR = 'CHATBOT_ERROR',
  OTHER = 'OTHER',
}

export const FeedbackCategoryDescriptions: Record<FeedbackCategory, string> = {
  [FeedbackCategory.QR_ERROR]: 'QR 코드 오류',
  [FeedbackCategory.LED_ERROR]: 'LED 위치 표시 오류',
  [FeedbackCategory.CAMERA_ERROR]: '카메라 인식 오류',
  [FeedbackCategory.BOOK_LOCATION]: '도서 위치 오류',
  [FeedbackCategory.SYSTEM_ERROR]: '시스템 오류',
  [FeedbackCategory.CHATBOT_ERROR]: '챗봇 응답 오류',
  [FeedbackCategory.OTHER]: '기타',
};
