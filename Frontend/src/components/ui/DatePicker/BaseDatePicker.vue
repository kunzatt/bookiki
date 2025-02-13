<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ref } from 'vue'
import dayjs from 'dayjs'
import type { DateRange } from '@/types/common/datePicker'

interface DateProps {
  modelValue?: DateRange | null
  disabled?: boolean
  format?: string
}

const dateProps = withDefaults(defineProps<DateProps>(), {
  disabled: false,
  format: 'YYYY.MM.DD'
})

const emit = defineEmits(['update:modelValue', 'change'])

const isModalOpen = ref(false)
const activeTab = ref('custom')
const startDate: Ref<Date | null> = ref(null)
const endDate: Ref<Date | null> = ref(null)
const currentDate = ref(dayjs())
const selectedType = ref<'start' | 'end'>('start')

// 기간 선택 탭 옵션
const tabOptions = [
  {
    key: 'month1',
    label: '최근 1개월',
    value: () => {
      const end = dayjs()
      const start = end.subtract(1, 'month')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    key: 'month3',
    label: '최근 3개월',
    value: () => {
      const end = dayjs()
      const start = end.subtract(3, 'month')
      return [start.toDate(), end.toDate()]
    }
  },
  {
    key: 'custom',
    label: '사용자 지정',
    value: () => [startDate.value, endDate.value]
  }
]

const formatDate = (date: Date | null) => {
  if (!date) return ''
  return dayjs(date).format(dateProps.format)
}

const handleTabChange = (tab: string) => {
  activeTab.value = tab
  const selectedOption = tabOptions.find(option => option.key === tab)
  if (selectedOption && tab !== 'custom') {
    const [start, end] = selectedOption.value()
    startDate.value = start
    endDate.value = end
    emitDateChange()
  }
}

const weekDays = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']

const daysInMonth = computed(() => {
  const firstDay = currentDate.value.startOf('month')
  const daysInMonth = currentDate.value.daysInMonth()
  const startOfWeek = firstDay.day()
  
  const days = []
  // 이전 달의 마지막 날짜들
  for (let i = 0; i < startOfWeek; i++) {
    days.push({
      date: firstDay.subtract(startOfWeek - i, 'day'),
      isCurrentMonth: false
    })
  }
  
  // 현재 달의 날짜들
  for (let i = 1; i <= daysInMonth; i++) {
    days.push({
      date: firstDay.add(i - 1, 'day'),
      isCurrentMonth: true
    })
  }
  
  // 다음 달의 시작 날짜들
  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i++) {
    days.push({
      date: firstDay.add(daysInMonth - 1 + i, 'day'),
      isCurrentMonth: false
    })
  }
  
  return days
})

const prevMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month')
}

const nextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month')
}

const selectDate = (date: dayjs.Dayjs) => {
  if (selectedType.value === 'start') {
    startDate.value = date.toDate()
    selectedType.value = 'end'
  } else {
    if (startDate.value && date.isBefore(startDate.value)) {
      endDate.value = startDate.value
      startDate.value = date.toDate()
    } else {
      endDate.value = date.toDate()
    }
    selectedType.value = 'start'
  }
  
  if (startDate.value && endDate.value) {
    emitDateChange()
  }
}

const emitDateChange = () => {
  if (startDate.value && endDate.value) {
    emit('update:modelValue', [startDate.value, endDate.value])
    emit('change', [startDate.value, endDate.value])
  }
}

const isSelected = (date: dayjs.Dayjs) => {
  return (startDate.value && dayjs(startDate.value).isSame(date, 'day')) ||
         (endDate.value && dayjs(endDate.value).isSame(date, 'day'))
}

const isInRange = (date: dayjs.Dayjs) => {
  if (!startDate.value || !endDate.value) return false
  return date.isAfter(startDate.value) && date.isBefore(endDate.value)
}
</script>

<template>
    <!-- Date Range Input -->
    <div class="flex items-center gap-2 w-full" @click="isModalOpen = true">
      <div class="flex-1 px-4 py-2 bg-[#F5F7F5] rounded text-sm text-[#698469]">
        {{ startDate ? formatDate(startDate) : '시작일' }}
      </div>
      <span class="text-[#698469]">~</span>
      <div class="flex-1 px-4 py-2 bg-[#F5F7F5] rounded text-sm text-[#698469]">
        {{ endDate ? formatDate(endDate) : '종료일' }}
      </div>
    </div>
  
    <!-- Modal -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isModalOpen"
        class="fixed inset-0 flex items-center justify-center z-50"
      >
        <!-- Backdrop -->
        <div 
          class="absolute inset-0 bg-black bg-opacity-40"
          @click="isModalOpen = false"
        />
        
        <!-- Modal Content -->
        <div class="relative bg-white rounded-2xl p-6 max-w-md w-full mx-4 z-10">
          <!-- 기간 선택 탭 -->
          <div class="mb-6">
            <div class="flex gap-2">
              <button
                v-for="tab in tabOptions"
                :key="tab.key"
                @click="handleTabChange(tab.key)"
                class="flex-1 py-2 text-xs font-medium rounded-full"
                :class="[
                  activeTab === tab.key
                    ? 'bg-[#698469] text-white'
                    : 'bg-[#F5F7F5] text-[#698469]'
                ]"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>
  
          <!-- 달력 -->
          <div v-if="activeTab === 'custom'">
            <!-- 달력 헤더 -->
            <div class="flex items-center justify-between mb-4">
              <button @click="prevMonth" class="p-1">
                <span class="text-[#698469]">&lt;</span>
              </button>
              <span class="text-sm font-medium text-[#698469]">
                {{ currentDate.format('MM YYYY') }}
              </span>
              <button @click="nextMonth" class="p-1">
                <span class="text-[#698469]">&gt;</span>
              </button>
            </div>
  
            <!-- 요일 헤더 -->
            <div class="grid grid-cols-7 mb-2">
              <div
                v-for="day in weekDays"
                :key="day"
                class="text-center text-xs text-[#A3B8A3] py-2"
              >
                {{ day }}
              </div>
            </div>
  
            <!-- 날짜 그리드 -->
            <div class="grid grid-cols-7 gap-1">
              <button
                v-for="{ date, isCurrentMonth } in daysInMonth"
                :key="date.format('YYYY-MM-DD')"
                @click="selectDate(date)"
                class="aspect-square flex items-center justify-center text-sm rounded-full relative"
                :class="[
                  isCurrentMonth ? 'text-[#2C3E2E]' : 'text-[#CCD5CC]',
                  isSelected(date) ? 'bg-[#698469] text-white' : '',
                  isInRange(date) ? 'bg-[#E8EDE8]' : ''
                ]"
              >
                {{ date.format('D') }}
                <div 
                  v-if="isSelected(date)"
                  class="absolute -bottom-1 w-1 h-1 bg-[#698469] rounded-full"
                />
              </button>
            </div>
  
            <!-- 선택 상태 표시 -->
            <div class="mt-4 text-xs text-[#698469] text-center">
              {{ selectedType === 'start' ? '시작일' : '종료일' }} 선택
            </div>
          </div>
  
          <!-- Modal Footer -->
          <div class="flex justify-center gap-3 mt-6">
            <button 
              class="px-4 py-2 bg-[#698469] text-white rounded-lg text-sm"
              @click="isModalOpen = false"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </template>