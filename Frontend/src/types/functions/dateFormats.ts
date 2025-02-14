/**
 * 날짜 배열 또는 문자열을 'YYYY.MM.DD' 형식으로 포맷팅
 * @param date 날짜 데이터 ([year, month, day, hour, minute, second] 또는 문자열)
 * @returns 포맷팅된 날짜 문자열
 */
export const formatDate = (date: number[] | string): string => {
  if (Array.isArray(date)) {
    const [year, month, day] = date;
    return `${year}.${String(month).padStart(2, '0')}.${String(day).padStart(2, '0')}`;
  }

  if (typeof date === 'string') {
    const parsed = new Date(date);
    if (isNaN(parsed.getTime())) {
      return '날짜 정보 없음';
    }
    return parsed
      .toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      })
      .replace(/\. /g, '.')
      .replace('.', '');
  }

  return '날짜 정보 없음';
};

/**
 * 날짜 배열 또는 문자열을 'YYYY.MM.DD HH:mm' 형식으로 포맷팅
 * @param date 날짜 데이터 ([year, month, day, hour, minute, second] 또는 문자열)
 * @returns 포맷팅된 날짜와 시간 문자열
 */
export const formatDateTime = (date: number[] | string): string => {
  if (Array.isArray(date)) {
    const [year, month, day, hour = 0, minute = 0] = date;
    return `${year}.${String(month).padStart(2, '0')}.${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
  }

  if (typeof date === 'string') {
    const parsed = new Date(date);
    if (isNaN(parsed.getTime())) {
      return '날짜 정보 없음';
    }
    return `${parsed
      .toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      })
      .replace(/\. /g, '.')
      .replace(
        '.',
        '',
      )} ${String(parsed.getHours()).padStart(2, '0')}:${String(parsed.getMinutes()).padStart(2, '0')}`;
  }

  return '날짜 정보 없음';
};
