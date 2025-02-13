import { Role } from "../enums/role";

// 사용자 상태
export interface UserStatus {
    availableLoans: number;
    isOverdue: boolean;
    isSuspended: boolean;
  }