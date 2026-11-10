import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // /api で始まるリクエストは、Spring Boot（8080）に取り次ぐ（開発中のみ）
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
