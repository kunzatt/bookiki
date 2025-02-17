<script setup lang="ts">
import type { BookHistoryResponse } from '@/types/api/bookHistory';
import { getBookInformation } from '@/api/bookInformation';
import { getBookItemById } from '@/api/bookItem';
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';

interface BookDetail extends BookHistoryResponse {
  bookInfo?: {
    title: string;
    author: string;
    image: string;
  };
}

interface BookHistoryProps {
  items: BookHistoryResponse[];
}

const props = defineProps<BookHistoryProps>();
const booksWithDetails = ref<BookDetail[]>([]);
const router = useRouter();

// 날짜 포맷팅 함수 (YYYY.MM.DD)
const formatDate = (dateString: string | number[] | null) => {
  if (!dateString) return '-';
  
  // 배열인 경우 ([2025, 1, 20, 0, 0])
  if (Array.isArray(dateString)) {
    const [year, month, day] = dateString;
    return `${year}.${String(month).padStart(2, '0')}.${String(day).padStart(2, '0')}`;
  }
  
  // 문자열인 경우 ("2025-01-20T00:00:00")
  if (typeof dateString === 'string') {
    const [date] = dateString.split('T');
    if (!date) return '-';
    const [year, month, day] = date.split('-');
    return `${year}.${month}.${day}`;
  }
  
  return '-';
};

// 도서 상세 정보 가져오기
const fetchBookDetails = async () => {
  try {
    if (!props.items || props.items.length === 0) {
      console.log('도서 목록이 비어있음');
      booksWithDetails.value = [];
      return;
    }

    console.log('도서 상세 정보 조회 시작:', props.items);
    const detailedBooks = await Promise.all(
      props.items.map(async (book) => {
        try {
          // 1. bookItemId로 도서 아이템 정보 조회
          const bookItem = await getBookItemById(book.bookItemId);
          console.log(`도서 아이템 정보 (ID: ${book.bookItemId}):`, bookItem);
          
          // 2. bookInformationId로 도서 정보 조회
          const bookInfo = await getBookInformation(bookItem.bookInformationId);
          console.log(`도서 상세 정보 (ID: ${bookItem.bookInformationId}):`, bookInfo);
          
          return {
            ...book,
            bookInfo: {
              title: bookInfo.title,
              author: bookInfo.author,
              image: bookInfo.image // 직접 bookInfo.image 사용
            }
          };
        } catch (error) {
          console.error(`도서 정보 조회 실패 (ID: ${book.bookItemId}):`, error);
          return {
            ...book,
            bookInfo: {
              title: book.bookTitle || '제목 없음',
              author: book.bookAuthor || '저자 미상',
              image: '/default-book-cover.svg'
            }
          };
        }
      })
    );
    
    console.log('도서 상세 정보 조회 완료:', detailedBooks);
    booksWithDetails.value = detailedBooks;
  } catch (error) {
    console.error('도서 상세 정보 조회 실패:', error);
    booksWithDetails.value = props.items.map(book => ({
      ...book,
      bookInfo: {
        title: book.bookTitle || '제목 없음',
        author: book.bookAuthor || '저자 미상',
        image: '/default-book-cover.svg'
      }
    }));
  }
};

const navigateToBookDetail = (bookItemId: number) => {
  router.push(`/books/${bookItemId}`);
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = '/default-book-cover.svg';
};

// items가 변경될 때마다 도서 정보 다시 가져오기
watch(() => props.items, fetchBookDetails, { immediate: true });

// 컴포넌트 마운트 시에도 도서 정보 가져오기
onMounted(() => {
  fetchBookDetails();
});
</script>

<template>
  <div class="w-full max-w-4xl mx-auto">
    <ul class="space-y-4">
      <li 
        v-for="(item, index) in booksWithDetails" 
        :key="index"
        class="bg-white p-4 rounded-lg shadow-sm cursor-pointer hover:shadow-md transition-shadow"
        @click="navigateToBookDetail(item.bookItemId)"
      >
        <div class="flex gap-4">
          <!-- 도서 이미지 -->
          <div class="w-24 h-36 flex-shrink-0">
            <img
              :src="item.bookInfo?.image || '/default-book-cover.svg'"
              :alt="item.bookInfo?.title"
              class="w-full h-full object-cover rounded-md bg-gray-100"
              @error="handleImageError"
            />
          </div>

          <!-- 도서 정보 -->
          <div class="flex-1 min-w-0">
            <div class="flex flex-col h-full justify-between">
              <!-- 상단: 제목, 저자 -->
              <div>
                <h3 class="text-lg font-medium text-gray-900 mb-1 line-clamp-2">
                  {{ item.bookInfo?.title }}
                </h3>
                <p class="text-sm text-gray-500 mb-4">
                  {{ item.bookInfo?.author }}
                </p>
              </div>

              <!-- 하단: 대출/반납 정보 -->
              <div class="flex flex-wrap gap-x-6 gap-y-2 text-sm">
                <div>
                  <span class="text-gray-500">대출일:</span>
                  <span class="ml-2 text-gray-900">{{ formatDate(item.borrowedAt) }}</span>
                </div>
                <!-- 반납일은 반납된 경우에만 표시 -->
                <div v-if="item.returnedAt">
                  <span class="text-gray-500">반납일:</span>
                  <span class="ml-2" :class="item.overdue ? 'text-red-600' : 'text-gray-900'">
                    {{ formatDate(item.returnedAt) }}
                  </span>
                </div>
                <!-- 상태 표시 -->
                <div 
                  :class="[
                    'px-2 py-0.5 rounded-full text-xs',
                    !item.returnedAt && !item.overdue ? 'bg-blue-100 text-blue-800' : // 대출중
                    !item.returnedAt && item.overdue ? 'bg-red-100 text-red-800' : // 연체중
                    item.overdue ? 'bg-red-100 text-red-800' : // 연체 반납
                    'bg-green-100 text-green-800' // 정상 반납
                  ]"
                >
                  {{ 
                    !item.returnedAt && !item.overdue ? '대출중' :
                    !item.returnedAt && item.overdue ? '연체중' :
                    item.overdue ? '연체 반납' : '정상 반납'
                  }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>