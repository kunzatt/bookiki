import { Role } from '../enums/role';

// 인증된 사용자
export interface AuthUser {
  id: number;
  email: string;
  name: string;
  role: Role;
}

// 구글 인증 요청
export interface GoogleOAuth2Request extends OAuth2Request {
  email: string;
  name: string;
}

// 로그인 요청
export interface LoginRequest {
  email: string;
  password: string;
}

// 로그인 응답
export interface LoginResponse {
  id: number;
  username: string;
  role: Role;
}

// 네이버 인증 요청
export interface NaverOAuth2Request extends OAuth2Request {
  email: string;
  name: string;
}

// OAuth 요청
export interface OAuth2Request {
  email: string;
  name: string;
}

// OAuth 회원가입 요청
export interface OAuth2SignUpRequest {
  companyId: string;
  userName: string;
}

// 비밀번호 재설정 이메일 요청
export interface PasswordResetEmailRequest {
  email: string;
  userName: string;
}

// 비밀번호 재설정 요청
export interface PasswordResetRequest {
  newPassword: string;
  newPasswordConfirm: string;
}

// 비밀번호 변경 요청
export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
  newPasswordConfirm: string;
}

// 프로필 응답
export interface ProfileResponse {
  profileImageUrl: string;
}

// 메일 전송 요청
export interface SendEmailRequest {
  email: string;
}

// 테스트 계정 요청
export interface TestAccountRequest {
  email: string;
  password: string;
  userName: string;
  companyId: string;
}

// 관리자용 사용자정보 요청
export interface UserInformationForAdminRequest {
  activeAt: string;
}

// 관리자용 사용자 정보 응답
export interface UserInformationForAdminResponse {
  id: number;
  email: string;
  userName: string;
  companyId: string;
  role: string;
  createdAt: string;
  updatedAt: string;
  activeAt: string | null;
  profileImage: string;
  provider: string;
  currentBorrowCount: number; // 추가: 현재 대출 중인 도서 수
  hasOverdueBooks: boolean; // 추가: 연체 도서 존재 여부
}

// 회원가입 요청
export interface UserSignUpRequest {
  email: string;
  password: string;
  userName: string;
  companyId: string;
}

// 인증코드 요청
export interface VerifyCodeRequest {
  code: string;
}

// 현재 사용자 정보 응답
export interface UserInformationResponse {
  userName: string;
  companyId: string;
  loanStatus: string;
}
