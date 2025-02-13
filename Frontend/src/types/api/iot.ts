import { IotMessageType } from '@/types/enums/iotMessageType';

export interface ShelfInfo {
    id: number;
    shelfNumber: number;
    lineNumber: number;
    category: number;
}

export interface IotMessage {
    type: IotMessageType;
    bookId: string;
    shelf?: ShelfInfo;
    timestamp: string;
}