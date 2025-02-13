// src/constants/messages.ts
export const AUTH_MESSAGES = {
    SUCCESS: {
      SIGNUP: '회원가입이 완료되었습니다.',
      EMAIL_VERIFIED: '이메일이 인증되었습니다.',
      EMAIL_SENT: '인증 코드가 발송되었습니다.',
      COMPANY_ID_AVAILABLE: '사용 가능한 사번입니다.'
    },
    ERROR: {
      EMAIL_DUPLICATE: '이미 사용중인 이메일입니다.',
      COMPANY_ID_DUPLICATE: '이미 사용중인 사번입니다.',
      INVALID_EMAIL: '유효한 이메일을 입력해주세요.',
      INVALID_CODE: '인증 코드가 올바르지 않습니다.',
      PASSWORD_MISMATCH: '비밀번호가 일치하지 않습니다.',
      SIGNUP_FAILED: '회원가입에 실패했습니다.',
      FORM_INVALID: '모든 필드를 올바르게 입력해주세요.'
    }
  };