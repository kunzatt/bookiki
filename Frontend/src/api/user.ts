import axios from './axios';
import instance from '@/api/axios';

const API_URL = '/api'; // 베이스 URL을 설정합니다.

import type {
  SendEmailRequest,
  VerifyCodeRequest,
  LoginRequest,
  LoginResponse,
  OAuth2SignUpRequest,
  ProfileResponse,
  UserInformationForAdminResponse,
  UserInformationForAdminRequest,
  UserSignUpRequest,
  PasswordResetEmailRequest,
  PasswordResetRequest,
  PasswordUpdateRequest,
} from '@/types/api/user';

// 이메일 회원가입
export const registerWithEmail = async (request: UserSignUpRequest): Promise<string> => {
  try {
    const response = await axios.post<string>(`${API_URL}/user/signup`, request);
    return response.data;
  } catch (error) {
    console.error('회원가입 실패:', error);
    throw error;
  }
};

// 이메일 중복 확인
export const checkEmailDuplicate = async (email: string): Promise<string> => {
  try {
    const response = await axios.get<string>(`${API_URL}/user/signup/email/check`, {
      params: { email },
    });
    return response.data;
  } catch (error) {
    console.error('이메일 중복 확인 실패:', error);
    throw error;
  }
};

// 사번 중복 확인
export const checkCompanyId = async (companyId: string): Promise<string> => {
  try {
    const response = await axios.get<string>(`${API_URL}/user/signup/company-id/check`, {
      params: { companyId },
    });
    return response.data;
  } catch (error) {
    console.error('사번 중복 확인 실패:', error);
    throw error;
  }
};

// 이메일 인증 코드 발송
export const sendVerificationCode = async (request: SendEmailRequest): Promise<void> => {
  try {
    await axios.post(`${API_URL}/auth/email/verify`, request);
  } catch (error) {
    console.error('인증 코드 발송 실패:', error);
    throw error;
  }
};

// 이메일 인증 코드 확인
export const verifyCode = async (email: string, request: VerifyCodeRequest): Promise<void> => {
  try {
    await axios.post(`${API_URL}/auth/email/verify/${email}`, request);
  } catch (error) {
    console.error('인증 코드 확인 실패:', error);
    throw error;
  }
};

// 이메일 로그인
export const login = async (request: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await axios.post<LoginResponse>(`${API_URL}/auth/login`, request);
    return response.data;
  } catch (error) {
    console.error('로그인 실패:', error);
    throw error;
  }
};

// 로그아웃
export const logout = async (): Promise<void> => {
  try {
    await instance.post(`/api/auth/logout`);

    // 수동으로 쿠키 삭제
    document.cookie = 'access_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  } catch (error) {
    console.error('로그아웃 실패:', error);
    throw error;
  }
};

// OAuth2 회원가입
export const completeOAuth2SignUp = async (
  provider: string,
  request: OAuth2SignUpRequest,
  temporaryToken: string,
): Promise<string> => {
  try {
    const token = temporaryToken.startsWith('Bearer ')
      ? temporaryToken
      : `Bearer ${temporaryToken}`;

    const response = await axios.post<string>(`${API_URL}/auth/oauthsignup`, request, {
      headers: {
        Authorization: `Bearer ${temporaryToken}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error('OAuth2 회원가입 완료 실패:', error);
    throw error;
  }
};

// 프로필 사진 변경
export const updateProfileImage = async (userId: number, file: File): Promise<void> => {
  try {
    const formData = new FormData();
    formData.append('file', file);

    await axios.put(`${API_URL}/users/${userId}/profile-image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  } catch (error) {
    console.error('프로필 사진 변경 실패:', error);
    throw error;
  }
};

// 프로필 사진 삭제
export const deleteProfileImage = async (userId: number): Promise<void> => {
  try {
    await axios.delete(`${API_URL}/users/${userId}/profile-image`);
  } catch (error) {
    console.error('프로필 사진 삭제 실패:', error);
    throw error;
  }
};

// 프로필 사진 조회
export const getProfileImage = async (userId: number): Promise<ProfileResponse> => {
  try {
    const response = await axios.get<ProfileResponse>(`${API_URL}/users/${userId}/profile-image`);
    return response.data;
  } catch (error) {
    console.error('프로필 사진 조회 실패:', error);
    throw error;
  }
};
// 전체 사용자 정보 조회
export const getUserDetails = async (): Promise<UserInformationForAdminResponse[]> => {
  try {
    const response = await axios.get<UserInformationForAdminResponse[]>(`${API_URL}/admin/users`);
    return response.data;
  } catch (error) {
    console.error('전체 사용자 조회 실패:', error);
    throw error;
  }
};

// 사용자 상세 정보 조회
export const getUserDetailsById = async (
  userId: number,
): Promise<UserInformationForAdminResponse> => {
  try {
    const response = await axios.get<UserInformationForAdminResponse>(
      `${API_URL}/admin/users/${userId}`,
    );
    return response.data;
  } catch (error) {
    console.error('사용자 상세 조회 실패:', error);
    throw error;
  }
};

// 사용자 I활성 시간 수정
export const updateUserActiveAt = async (
  userId: number,
  request: UserInformationForAdminRequest,
): Promise<UserInformationForAdminResponse> => {
  try {
    const response = await axios.put<UserInformationForAdminResponse>(
      `${API_URL}/admin/users/${userId}`,
      request,
    );
    return response.data;
  } catch (error) {
    console.error('사용자 활성 시간 수정 실패:', error);
    throw error;
  }
};

// 사용자 삭제
export const deleteUser = async (userId: number): Promise<UserInformationForAdminResponse> => {
  try {
    const response = await axios.delete<UserInformationForAdminResponse>(
      `${API_URL}/admin/users/${userId}`,
    );
    return response.data;
  } catch (error) {
    console.error('사용자 삭제 실패:', error);
    throw error;
  }
};

// 비밀번호 재설정 이메일 발송
export const sendPasswordResetEmail = async (request: PasswordResetEmailRequest): Promise<void> => {
  try {
    await axios.post(`${API_URL}/password/reset-email`, request);
  } catch (error) {
    console.error('비밀번호 재설정 이메일 발송 실패:', error);
    throw error;
  }
};

// 비밀번호 재설정
export const resetPassword = async (
  token: string,
  request: PasswordResetRequest,
): Promise<void> => {
  try {
    await axios.put(`${API_URL}/password/reset/${token}`, request);
  } catch (error) {
    console.error('비밀번호 재설정 실패:', error);
    throw error;
  }
};

// 비밀번호 변경
export const updatePassword = async (request: PasswordUpdateRequest): Promise<void> => {
  try {
    await axios.put(`${API_URL}/password/update`, request);
  } catch (error) {
    console.error('비밀번호 변경 실패:', error);
    throw error;
  }
};

export const reissueToken = async (): Promise<void> => {
  try {
    await axios.post(`${API_URL}/auth/reissue`);
  } catch (error) {
    console.error('토큰 재발급 실패:', error);
    throw error;
  }
};

export const setToken = async (temporaryToken: string): Promise<LoginResponse> => {
  try {
    const response = await axios.post<LoginResponse>(`${API_URL}/auth/token`, { temporaryToken });
    return response.data;
  } catch (error) {
    console.error('토큰 설정 실패:', error);
    throw error;
  }
};
