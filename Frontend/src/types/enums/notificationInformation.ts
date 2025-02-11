export enum NotificationType {
    CAMERA_ERROR = "카메라/통신 오류",
    QR_SCAN_ERROR = "QR 오류",
    LOST_BOOK = "도서 분실",
    BOOK_ARRANGEMENT = "도서 정리",
    BOOK_OVERDUE = "도서 연체",
    QNA_CREATED = "문의사항 등록",
    RETURN_DEADLINE = "반납 기한",
    OVERDUE_NOTICE = "연체 알림",
    FAVORITE_BOOK_AVAILABLE = "관심 도서 대출 가능",
    QNA_ANSWERED = "문의사항 답변 등록"
  }
  
  export interface NotificationInformation {
    type: NotificationType;
    description: string;
  }
  
  export const NotificationDescriptions: Record<NotificationType, string> = {
    [NotificationType.CAMERA_ERROR]: "카메라 혹은 통신 오류가 발생하였습니다. 카메라 설치 환경을 확인 해주세요.",
    [NotificationType.QR_SCAN_ERROR]: "에 해당하는 QR 코드 인식에 실패하였습니다. QR 코드 상태를 확인해주세요.",
    [NotificationType.LOST_BOOK]: "제목의 도서가 분실되었습니다. 도서 상태를 확인해주세요.",
    [NotificationType.BOOK_ARRANGEMENT]: "번 책장의 도서 정리가 필요합니다. 책장의 상태를 확인해주세요.",
    [NotificationType.BOOK_OVERDUE]: "제목의 도서가 연체 되었습니다. 연체 내역을 확인해주세요.",
    [NotificationType.QNA_CREATED]: "제목의 문의사항이 등록되었습니다. 문의 내용을 확인해주세요.",
    [NotificationType.RETURN_DEADLINE]: "제목의 대출하신 도서 반납 기한이 다가옵니다. 반납일을 확인해주세요.",
    [NotificationType.OVERDUE_NOTICE]: "제목의 대출하신 도서가 연체되었습니다. 즉시 반납해주세요.",
    [NotificationType.FAVORITE_BOOK_AVAILABLE]: "제목의 관심 도서가 대출 가능한 상태가 되었습니다.",
    [NotificationType.QNA_ANSWERED]: "제목의 문의하신 내용에 답변이 등록되었습니다. 답변을 확인해주세요."
  };
  