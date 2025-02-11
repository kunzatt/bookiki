import axios from 'axios';

const API_URL = '/api' // 베이스 URL을 설정합니다.

import type { 
   LoanPolicyResponse,
   LoanBookPolicyRequest,
   LoanBookPolicyResponse,
   LoanPeriodPolicyRequest, 
   LoanPeriodPolicyResponse,
   LoanPolicyRequest
} from '@/types/api/loanpolicy';

// 현재 대출 정책 조회
export const getCurrentPolicy = async (): Promise<LoanPolicyResponse> => {
   try {
       const response = await axios.get<LoanPolicyResponse>(`${API_URL}/loan-policy`);
       return response.data;
   } catch (error) {
       console.error('대출 정책 조회 실패:', error);
       throw error;
   }
};

// 최대 대출 가능 도서 수 수정
export const updateMaxBooks = async (
   request: LoanBookPolicyRequest
): Promise<LoanBookPolicyResponse> => {
   try {
       const response = await axios.patch<LoanBookPolicyResponse>(
           `${API_URL}/admin/loan-policy/books`,
           request
       );
       return response.data;
   } catch (error) {
       console.error('최대 대출 가능 도서 수 수정 실패:', error);
       throw error;
   }
};

// 대출 기간 수정
export const updateLoanPeriod = async (
   request: LoanPeriodPolicyRequest
): Promise<LoanPeriodPolicyResponse> => {
   try {
       const response = await axios.patch<LoanPeriodPolicyResponse>(
           `${API_URL}/admin/loan-policy/period`,
           request
       );
       return response.data;
   } catch (error) {
       console.error('대출 기간 수정 실패:', error);
       throw error;
   }
};

// 대출 정책 전체 수정
export const updatePolicy = async (
   request: LoanPolicyRequest
): Promise<LoanPolicyResponse> => {
   try {
       const response = await axios.put<LoanPolicyResponse>(
           `${API_URL}/admin/loan-policy`,
           request
       );
       return response.data;
   } catch (error) {
       console.error('대출 정책 전체 수정 실패:', error);
       throw error;
   }
};