export interface Message {
  type: 'user' | 'bot' | 'system';
  content: string;
  showFeedback?: boolean;
  showReportButton?: boolean; // 신고하기 버튼 표시 여부
}

export interface ChatbotState {
  isOpen: boolean;
  messages: Message[];
  showFeedback: boolean;
}
