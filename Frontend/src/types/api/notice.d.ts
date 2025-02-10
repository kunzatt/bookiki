// 공지사항 생성 요청 타입
export interface NoticeRequest {
  title: string
  content: string
}

// 공지사항 응답 타입
export interface NoticeResponse {
  id: number
  title: string
  content: string
  createdAt: string // LocalDateTime은 문자열로 전달됨
  updatedAt: string
  viewCount: number
}

// 공지사항 수정 요청 타입
export interface NoticeUpdate {
  id: number
  title: string
  content: string
}
