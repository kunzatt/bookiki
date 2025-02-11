// 페이지네이션
export interface Pageable {
    pageNumber: number;
    pageSize: number;
    sort: {
        sorted: boolean;
        direction: string;
    };
}

export interface PageResponse<T> {
    content: T[];
    pageable: Pageable;
    totalElements: number;
}