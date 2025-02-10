import { defineStore } from 'pinia';
import type { User } from '../components/common/Sidebar';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
  }),
  
  actions: {
    setUser(user: User) {
      this.user = user;
    },
    clearUser() {
      this.user = null;
    },
  },
  
  getters: {
    isAuthenticated: (state) => !!state.user,
    userRole: (state) => state.user?.role,
  },
});