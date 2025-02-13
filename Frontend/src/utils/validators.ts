// src/utils/validators.ts
export const validators = {
    email: (value: string): boolean => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(value);
    },
    
    password: (value: string): boolean => {
      // 최소 8자, 영문, 숫자, 특수문자 포함
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
      return passwordRegex.test(value);
    },
  
    companyId: (value: string): boolean => {
      return value.length > 0;
    }
  };