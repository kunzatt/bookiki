// 백엔드로 보내줄 페이지 정보
export interface Pageable {
    pageNumber: number;  // 현재 페이지 번호 (0부터 시작)
    pageSize: number;  // 페이지당 데이터 수
    sort: string[];   // 정렬 조건 배열 (예: ["createdAt,DESC"])
}

// 백엔드에서 받을 페이지 정보
export interface PageResponse<T> {
    content: T[];  // 실제 데이터 배열
    totalElements: number;  // 전체 데이터 수
    totalPages: number;     // 전체 페이지 수
}