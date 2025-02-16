import type { QnaListResponse, QnaDetailResponse } from '../api/qna';
import { QnaTypeDescriptions } from '../enums/qnaType';

export interface QnaListResponseWithAnswered extends QnaListResponse {
  answered: boolean;
}

export const convertToQnaWithAnswered = (qna: QnaDetailResponse): QnaListResponseWithAnswered => {
  return {
    id: qna.id,
    title: qna.title,
    qnaType: qna.qnaType,
    authorName: qna.authorName,
    createdAt: qna.createdAt,
    answered: qna.comments.length > 0,
  };
};
export const hasAnswer = (qna: QnaDetailResponse): boolean => {
  return qna.comments.length > 0;
};
