import { getCurrentPolicy } from '@/api/loanPolicy';

export const calculateDueDate = async (borrowedAt: number[]): Promise<string> => {
    try {
        // 현재 대출 정책 조회
        const policy = await getCurrentPolicy();
        const loanPeriod = policy.loanPeriod;

        // borrowedAt 배열에서 연,월,일을 추출하여 Date 객체 생성
        const [year, month, day] = borrowedAt;
        const borrowedDate = new Date(year, month - 1, day); // JavaScript의 월은 0부터 시작하므로 -1
        
        // loanPeriod일 만큼 더하기
        const dueDate = new Date(borrowedDate);
        dueDate.setDate(borrowedDate.getDate() + loanPeriod);

        // YYYY-MM-DD 형식으로 반환
        return dueDate.toISOString().split('T')[0];
    } catch (error) {
        console.error('반납 예정일 계산 실패:', error);
        throw error;
    }
};
