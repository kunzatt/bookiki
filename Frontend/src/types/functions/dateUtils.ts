import { PeriodType } from "../enums/periodType";

export function getStartDate(period: PeriodType): Date {
    const today = new Date();
    switch (period) {
      case PeriodType.LAST_WEEK:
        return new Date(today.setDate(today.getDate() - 7));
      case PeriodType.LAST_MONTH:
        return new Date(today.setMonth(today.getMonth() - 1));
      case PeriodType.LAST_THREE_MONTHS:
        return new Date(today.setMonth(today.getMonth() - 3));
      case PeriodType.LAST_SIX_MONTHS:
        return new Date(today.setMonth(today.getMonth() - 6));
      case PeriodType.LAST_YEAR:
        return new Date(today.setFullYear(today.getFullYear() - 1));
      case PeriodType.CUSTOM:
        throw new Error("CUSTOM 타입은 직접 날짜를 입력해야 합니다.");
      default:
        throw new Error("잘못된 PeriodType 값입니다.");
    }
  }