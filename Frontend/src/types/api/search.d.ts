export type SearchType = 'TITLE' | 'AUTHOR' | 'PUBLISHER';

export interface BookSearchResponse {
  content: BookItem[];
  totalElements: number;
  totalPages: number;
  pageable: {
    page: number;
    size: number;
    sort: {
      sorted: boolean;
      direction: string;
    };
  };
}

export interface BookItem {
  id: number;
  image: string;
  title: string;
  author: string;
}
