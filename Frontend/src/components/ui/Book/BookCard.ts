// types/book.ts
export interface BookCardProps {
    coverImage: string;
    title: string;
    variant: 'returnDueDate' | 'author' | 'borrowCount' | 'expectedReturnDate';
    // 각 variant에 따른 추가 정보
    returnDueDate?: string;
    author?: string;
    borrowCount?: number;
    expectedReturnDate?: string;
  }