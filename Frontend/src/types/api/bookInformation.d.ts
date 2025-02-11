// 책 정보 응답
export interface BookInformationResponse {
    id: number;
    title: string;
    author: string;
    publisher: string;
    isbn: string;
    publishedAt: string;
    image: string;
    description: string;
    category: number;
}