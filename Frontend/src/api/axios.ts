// src/api/axios.ts
import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:8088',  // 또는 환경변수 사용
  withCredentials: true,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  }
});

export default instance;

export const getUserProfile = () => {
  return axios.get('/api/user/me');
};