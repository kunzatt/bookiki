import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  assetsInclude: ['**/*.PNG'], 
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        secure: false,
      ws: true,
      timeout: 5000,
        //rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/auth': {  // auth 경로에 대한 프록시 추가
      target: 'http://localhost:8088',
      changeOrigin: true,
    },
    },
  },
})
