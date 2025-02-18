import { defineStore } from 'pinia';
import type { BookRankingResponse } from '@/types/api/bookHistory';
import type { BookItemListResponse, BookInformationResponse } from '@/types/api/bookItem';

interface CacheData {
  data: any;
  timestamp: number;
}

interface MainPageState {
  monthlyBooksCache: CacheData | null;
  recommendedBooksCache: CacheData | null;
}

export const useMainPageStore = defineStore('mainPage', {
  state: (): MainPageState => ({
    monthlyBooksCache: null,
    recommendedBooksCache: null,
  }),

  getters: {
    isMonthlyBooksCacheValid(): boolean {
      if (!this.monthlyBooksCache) return false;
      const now = Date.now();
      return now - this.monthlyBooksCache.timestamp < 3600000; // 1시간 (60 * 60 * 1000 ms)
    },
    isRecommendedBooksCacheValid(): boolean {
      if (!this.recommendedBooksCache) return false;
      const now = Date.now();
      return now - this.recommendedBooksCache.timestamp < 3600000;
    },
  },

  actions: {
    setMonthlyBooksCache(data: BookRankingResponse[]) {
      this.monthlyBooksCache = {
        data,
        timestamp: Date.now(),
      };
    },
    setRecommendedBooksCache(data: (BookItemListResponse & { bookItemId: number; bookInfo?: BookInformationResponse })[]) {
      this.recommendedBooksCache = {
        data,
        timestamp: Date.now(),
      };
    },
    clearCache() {
      this.monthlyBooksCache = null;
      this.recommendedBooksCache = null;
    },
  },
});