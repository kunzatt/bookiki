<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import HeaderMobile from '@/components/common/HeaderMobile.vue';
import HeaderDesktop from '@/components/common/HeaderDesktop.vue';
import Sidebar from '@/components/common/Sidebar.vue';
import BottomNav from '@/components/common/BottomNav.vue';
import BasicSelect from '@/components/ui/Select/BasicSelect.vue';
import { useAuthStore } from '@/stores/auth';
import { getCurrentPolicy, updateMaxBooks, updateLoanPeriod, updatePolicy } from '@/api/loanPolicy';
import type { SelectOption } from '@/types/common/select';
import type { LoanPolicyResponse } from '@/types/api/loanpolicy';
import type { ShelfResponse, ShelfCreateRequest, ShelfUpdateRequest } from '@/types/api/shelf';
import { Category, CategoryNames } from '@/types/enums/category';
import { selectAllShelf, createShelf, updateShelf, deleteShelf } from '@/api/shelf';

const router = useRouter();
const authStore = useAuthStore();

// 현재 정책 상태
const currentPolicy = ref<LoanPolicyResponse>({
  maxBooks: 5,
  loanPeriod: 14,
});

// 선택 옵션
const maxBooksOptions = ref<SelectOption[]>([
  { value: 3, label: '3권' },
  { value: 5, label: '5권' },
  { value: 7, label: '7권' },
  { value: 10, label: '10권' },
]);

const loanPeriodOptions = ref<SelectOption[]>([
  { value: 7, label: '7일' },
  { value: 14, label: '14일' },
  { value: 21, label: '21일' },
  { value: 30, label: '30일' },
]);

// 선택된 값
const selectedMaxBooks = ref<number>(5);
const selectedLoanPeriod = ref<number>(14);

// 수정 상태
const isEditing = ref<boolean>(false);
const isSaving = ref<boolean>(false);

// 현재 정책 조회
const fetchCurrentPolicy = async () => {
  try {
    const response = await getCurrentPolicy();
    currentPolicy.value = response;
    selectedMaxBooks.value = response.maxBooks;
    selectedLoanPeriod.value = response.loanPeriod;
  } catch (error) {
    console.error('정책 조회 실패:', error);
    // TODO: 에러 처리
  }
};

// 정책 저장
const savePolicy = async () => {
  if (!isEditing.value) return;

  try {
    isSaving.value = true;
    await updatePolicy({
      maxBooks: selectedMaxBooks.value,
      loanPeriod: selectedLoanPeriod.value,
    });

    await fetchCurrentPolicy();
    isEditing.value = false;
  } catch (error) {
    console.error('정책 수정 실패:', error);
    // TODO: 에러 처리
  } finally {
    isSaving.value = false;
  }
};

// 수정 취소
const cancelEdit = () => {
  selectedMaxBooks.value = currentPolicy.value.maxBooks;
  selectedLoanPeriod.value = currentPolicy.value.loanPeriod;
  isEditing.value = false;
};

// 책장 관리 상태
const shelves = ref<ShelfResponse[]>([]);
const selectedShelf = ref<ShelfResponse | null>(null);
const isShelfModalOpen = ref<boolean>(false);
const isCreateMode = ref<boolean>(true);

const newShelf = ref<ShelfCreateRequest>({
  shelfNumber: 1,
  lineNumber: 1,
  category: Category.LITERATURE,
});

const categoryOptions = ref<SelectOption[]>([
  { value: Category.COMPUTER_SCIENCE, label: '컴퓨터과학' },
  { value: Category.PHILOSOPHY_PSYCHOLOGY, label: '철학/심리학' },
  { value: Category.RELIGION, label: '종교' },
  { value: Category.SOCIAL_SCIENCE, label: '사회과학' },
  { value: Category.LANGUAGE, label: '언어' },
  { value: Category.SCIENCE, label: '과학' },
  { value: Category.TECHNOLOGY, label: '기술' },
  { value: Category.ART, label: '예술' },
  { value: Category.LITERATURE, label: '문학' },
  { value: Category.HISTORY_GEOGRAPHY, label: '역사/지리' },
]);

// 책장 목록 조회
const fetchShelves = async () => {
  try {
    const response = await selectAllShelf();
    shelves.value = response.map((shelf) => ({
      ...shelf,
      category: Category[shelf.category as keyof typeof Category],
    }));
  } catch (error) {
    console.error('책장 목록 조회 실패:', error);
  }
};

// 책장 추가 모달 열기
const openCreateModal = () => {
  isCreateMode.value = true;
  selectedShelf.value = null;
  isShelfModalOpen.value = true;
};

// 책장 수정 모달 열기
const openEditModal = (shelf: ShelfResponse) => {
  isCreateMode.value = false;
  selectedShelf.value = { ...shelf };
  isShelfModalOpen.value = true;
};

// 책장 저장 (추가/수정)
const saveShelf = async () => {
  try {
    if (isCreateMode.value) {
      // newShelf.value로 접근
      await createShelf(newShelf.value);
    } else if (selectedShelf.value) {
      await updateShelf({
        id: selectedShelf.value.id,
        shelfNumber: selectedShelf.value.shelfNumber,
        lineNumber: selectedShelf.value.lineNumber,
        category: selectedShelf.value.category,
      });
    }
    await fetchShelves();
    isShelfModalOpen.value = false;
  } catch (error) {
    console.error('책장 저장 실패:', error);
  }
};

// 책장 삭제
const deleteShelfItem = async (id: number) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;

  try {
    await deleteShelf(id);
    await fetchShelves();
  } catch (error) {
    console.error('책장 삭제 실패:', error);
  }
};

onMounted(() => {
  fetchCurrentPolicy();
  fetchShelves();
});
</script>

<template>
  <div class="min-h-screen flex">
    <Sidebar class="hidden md:block fixed h-full" />
    <div class="flex-1 flex flex-col md:ml-64">
      <HeaderMobile
        class="md:hidden fixed top-0 left-0 right-0 z-10"
        title="도서관 관리"
        type="main"
      />
      <HeaderDesktop class="hidden md:block fixed top-0 right-0 left-64 z-10" title="도서관 관리" />
      <div class="flex-1 overflow-y-auto pt-16">
        <div class="h-full p-4 md:p-8">
          <!-- 대출 정책 관리 섹션 -->
          <div class="max-w-4xl mx-auto bg-white rounded-lg shadow p-6">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold text-gray-800">대출 정책 관리</h2>
              <div class="space-x-2">
                <button
                  v-if="!isEditing"
                  @click="isEditing = true"
                  class="px-4 py-2 bg-[#698469] text-white rounded hover:bg-[#4F6349] transition-colors"
                >
                  수정
                </button>
                <template v-else>
                  <button
                    @click="cancelEdit"
                    class="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400 transition-colors"
                    :disabled="isSaving"
                  >
                    취소
                  </button>
                  <button
                    @click="savePolicy"
                    class="px-4 py-2 bg-[#698469] text-white rounded hover:bg-[#4F6349] transition-colors"
                    :disabled="isSaving"
                  >
                    저장
                  </button>
                </template>
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-6">
              <!-- 최대 대출 가능 도서 수 -->
              <div class="space-y-2">
                <div class="flex justify-between">
                  <label class="block text-sm font-medium text-gray-700"> 대출 가능 도서 수 </label>
                  <span class="text-gray-600">현재: {{ currentPolicy.maxBooks }}권</span>
                </div>
                <div class="flex items-center space-x-2">
                  <BasicSelect
                    :options="maxBooksOptions"
                    v-model="selectedMaxBooks"
                    :disabled="!isEditing"
                    size="L"
                  />
                </div>
              </div>

              <!-- 대출 기간 -->
              <div class="space-y-2">
                <div class="flex justify-between">
                  <label class="block text-sm font-medium text-gray-700"> 대출 기간 </label>
                  <span class="text-gray-600">현재: {{ currentPolicy.loanPeriod }}일</span>
                </div>
                <div class="flex items-center space-x-2">
                  <BasicSelect
                    :options="loanPeriodOptions"
                    v-model="selectedLoanPeriod"
                    :disabled="!isEditing"
                    size="L"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 책장 관리 섹션 -->
          <div class="max-w-4xl mx-auto bg-white rounded-lg shadow p-6 mt-8">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold text-gray-800">책장 관리</h2>
              <button
                @click="openCreateModal"
                class="px-4 py-2 bg-[#698469] text-white rounded hover:bg-[#4F6349] transition-colors"
              >
                책장 추가
              </button>
            </div>

            <!-- 책장 목록 테이블 -->
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      책장 번호
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      행 번호
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      카테고리
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      관리
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="shelf in shelves" :key="shelf.id">
                    <td class="px-6 py-4 whitespace-nowrap">{{ shelf.shelfNumber }}</td>
                    <td class="px-6 py-4 whitespace-nowrap">{{ shelf.lineNumber }}</td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      {{ CategoryNames[shelf.category] }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <button
                        @click="openEditModal(shelf)"
                        class="text-[#698469] hover:text-[#4F6349] mr-2"
                      >
                        수정
                      </button>
                      <button
                        @click="deleteShelfItem(shelf.id)"
                        class="text-red-600 hover:text-red-800"
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 책장 추가/수정 모달 -->
            <div
              v-if="isShelfModalOpen"
              class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
            >
              <div class="bg-white rounded-lg p-6 w-full max-w-md">
                <h3 class="text-lg font-medium mb-4">
                  {{ isCreateMode ? '책장 추가' : '책장 수정' }}
                </h3>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1"> 책장 번호 </label>
                    <input
                      type="number"
                      :value="isCreateMode ? newShelf.shelfNumber : selectedShelf?.shelfNumber"
                      @input="
                        (e) => {
                          if (isCreateMode) {
                            newShelf.shelfNumber = Number(e.target.value);
                          } else if (selectedShelf) {
                            selectedShelf.shelfNumber = Number(e.target.value);
                          }
                        }
                      "
                      class="w-full border rounded-md px-3 py-2"
                      min="1"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1"> 행 번호 </label>
                    <input
                      type="number"
                      :value="isCreateMode ? newShelf.lineNumber : selectedShelf?.lineNumber"
                      @input="
                        (e) => {
                          if (isCreateMode) {
                            newShelf.lineNumber = Number(e.target.value);
                          } else if (selectedShelf) {
                            selectedShelf.lineNumber = Number(e.target.value);
                          }
                        }
                      "
                      class="w-full border rounded-md px-3 py-2"
                      min="1"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1"> 카테고리 </label>
                    <BasicSelect
                      :options="categoryOptions"
                      :modelValue="isCreateMode ? newShelf.category : selectedShelf?.category"
                      @update:modelValue="
                        (value) => {
                          if (isCreateMode) {
                            newShelf.category = value;
                          } else if (selectedShelf) {
                            selectedShelf.category = value;
                          }
                        }
                      "
                      size="L"
                    />
                  </div>
                </div>
                <div class="mt-6 flex justify-end space-x-2">
                  <button
                    @click="isShelfModalOpen = false"
                    class="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400 transition-colors"
                  >
                    취소
                  </button>
                  <button
                    @click="saveShelf"
                    class="px-4 py-2 bg-[#698469] text-white rounded hover:bg-[#4F6349] transition-colors"
                  >
                    저장
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="md:hidden fixed bottom-0 left-0 right-0">
        <BottomNav />
      </div>
    </div>
  </div>
</template>
