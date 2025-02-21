// src/utils/errorHandler.ts
export class APIError extends Error {
    constructor(message: string, public code?: string) {
      super(message);
      this.name = 'APIError';
    }
  }
  
  export const handleAPIError = (error: unknown) => {
    if (error instanceof APIError) {
      return error.message;
    }
    return '알 수 없는 오류가 발생했습니다.';
  };