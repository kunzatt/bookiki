export enum PeriodType {
    LAST_WEEK = "LAST_WEEK",
    LAST_MONTH = "LAST_MONTH",
    LAST_THREE_MONTHS = "LAST_THREE_MONTHS",
    LAST_SIX_MONTHS = "LAST_SIX_MONTHS",
    LAST_YEAR = "LAST_YEAR",
    CUSTOM = "CUSTOM"
  }
  
  export const PeriodTypeDescriptions: Record<PeriodType, string> = {
    [PeriodType.LAST_WEEK]: "최근 1주일",
    [PeriodType.LAST_MONTH]: "최근 1개월",
    [PeriodType.LAST_THREE_MONTHS]: "최근 3개월",
    [PeriodType.LAST_SIX_MONTHS]: "최근 6개월",
    [PeriodType.LAST_YEAR]: "최근 1년",
    [PeriodType.CUSTOM]: "사용자 지정"
  };
  