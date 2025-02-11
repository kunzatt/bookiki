import { NotificationStatus } from "../enums/notificationStatus";

// 알림 응답
export interface NotificationResponse {
    id: number;
    userId: number;
    content: string;
    notificationId: number;
    notificationType: string;
    notificationStatus: NotificationStatus;
    createdAt: string;
}