// src/api/axios.ts
import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:8088',  // 또는 환경변수 사용
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  }
});

export default instance;