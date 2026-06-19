import { defineStore } from 'pinia';
import http from '../api/http';

/** Estado de autenticación: token JWT + datos básicos del usuario. */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('sigec_token') || null,
    user: JSON.parse(localStorage.getItem('sigec_user') || 'null'),
  }),
  getters: {
    autenticado: (s) => !!s.token,
    rol: (s) => s.user?.rol || null,
    puedeEscribir: (s) => ['ADMIN', 'TECNICO'].includes(s.user?.rol),
    esAdmin: (s) => s.user?.rol === 'ADMIN',
  },
  actions: {
    async login(username, password) {
      const { data } = await http.post('/auth/login', { username, password });
      this.token = data.token;
      this.user = { username: data.username, rol: data.rol };
      localStorage.setItem('sigec_token', this.token);
      localStorage.setItem('sigec_user', JSON.stringify(this.user));
    },
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('sigec_token');
      localStorage.removeItem('sigec_user');
    },
  },
});
