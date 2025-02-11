import axios from 'axios'

const API_URL = '/api/qna' // 베이스 URL을 설정합니다.

import type {
    QnaRequest,
    QnaListResponse,
    QnaDetailResponse,
    QnaUpdate, 
} from '@/types/api/qna'