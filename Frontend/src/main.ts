import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// VeeValidate 설정
import { configure } from 'vee-validate'

// Material Icons 스타일 import
import '@material-design-icons/font'
import '@material-design-icons/font/index.css'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// VeeValidate 기본 설정
configure({
  generateMessage: ({ field }) => `${field} 필드가 유효하지 않습니다.`,
})

const app = createApp(App)

// Element Plus 아이콘 등록
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.use(ElementPlus)

app.mount('#app')
