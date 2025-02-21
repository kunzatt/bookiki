// src/functions/userUtils.ts
import { Role } from '../enums/role';
import type {
  AuthUser,
  OAuth2Request,
  GoogleOAuth2Request,
  NaverOAuth2Request,
  UserInformationForAdminResponse
} from '../api/user';

// OAuth2 타입 가드
export function isGoogleOAuth2Request(request: OAuth2Request): request is GoogleOAuth2Request {
  return 'email' in request && 'name' in request;
}

export function isNaverOAuth2Request(request: OAuth2Request): request is NaverOAuth2Request {
  return 'email' in request && 'name' in request;
}

// 비밀번호 유효성 검사
export function isValidPassword(password: string): boolean {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,20}$/;
  return passwordRegex.test(password);
}

// 인증 코드 유효성 검사
export function isValidVerificationCode(code: string): boolean {
  const codeRegex = /^[0-9]{6}$/;
  return codeRegex.test(code);
}

// Role 체크 유틸리티
export function isAdmin(user: AuthUser): boolean {
  return user.role === Role.ADMIN;
}

// 날짜 포맷 유틸리티 (관리자 응답용)
export function formatAdminResponseDates(response: UserInformationForAdminResponse): UserInformationForAdminResponse {
  return {
    ...response,
    createdAt: new Date(response.createdAt).toISOString(),
    updatedAt: new Date(response.updatedAt).toISOString(),
    activeAt: response.activeAt ? new Date(response.activeAt).toISOString() : null
  };
}

// 이메일 유효성 검사
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// AuthUser 변환 유틸리티 (백엔드 응답을 프론트엔드 타입으로 변환)
export function convertToAuthUser(userData: AuthUser): AuthUser {
  return {
    id: userData.id,
    email: userData.email,
    name: userData.name || '',
    role: userData.role
  };
}