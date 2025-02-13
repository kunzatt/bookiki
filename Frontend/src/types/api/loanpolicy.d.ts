// 대출권수 요청
export interface LoanBookPolicyRequest {
    maxBooks: number;
}

// 대출권수 응답
export interface LoanBookPolicyResponse {
    maxBooks: number;
}

// 대출기간 요청
export interface LoanPeriodPolicyRequest {
    loanPeriod: number;
}

// 대출기간 응답
export interface LoanPeriodPolicyResponse {
    loanPeriod: number;
}

// 대출정책 요청
export interface LoanPolicyRequest {
    maxBooks: number;
    loanPeriod: number;
}

// 대출정책 응답
export interface LoanPolicyResponse {
    maxBooks: number;
    loanPeriod: number;
}