import { defineStore } from 'pinia';
import type { BookItemDisplayResponse } from '@/types/api/bookItem';
import type { Pageable } from '@/types/common/pagination';

interface CacheData {
  data: {
    content: BookItemDisplayResponse[];
    totalPages: number;
  };
  timestamp: number;
  pageInfo: Pageable;
  keyword: string;
}

interface VirtualShelfState {
  booksCache: CacheData | null;
}

export const useVirtualShelfStore = defineStore('virtualShelf', {
  state: (): VirtualShelfState => ({
    booksCache: null,
  }),

  getters: {
    isBooksCacheValid(): boolean {
      if (!this.booksCache) return false;
      const now = Date.now();
      // 1시간 캐시 유효
      return now - this.booksCache.timestamp < 3600000;
    },
  },

  actions: {
    setBooksCache(data: { content: BookItemDisplayResponse[]; totalPages: number }, pageInfo: Pageable, keyword: string) {
      this.booksCache = {
        data,
        timestamp: Date.now(),
        pageInfo,
        keyword,
      };
    },
    clearCache() {
      this.booksCache = null;
    },
  },
});
