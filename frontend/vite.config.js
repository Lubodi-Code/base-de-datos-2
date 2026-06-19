import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// El frontend habla con la API por rutas /api/* ; en desarrollo se redirigen
// al backend Spring Boot en :8080 para evitar problemas de CORS.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
