import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from './auth';

describe('store auth — getters de rol y sesión', () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it('sin token no está autenticado', () => {
    const auth = useAuthStore();
    expect(auth.autenticado).toBe(false);
    expect(auth.rol).toBe(null);
    expect(auth.puedeEscribir).toBe(false);
    expect(auth.esAdmin).toBe(false);
  });

  it('ADMIN puede escribir y es admin', () => {
    const auth = useAuthStore();
    auth.token = 'jwt';
    auth.user = { username: 'admin', rol: 'ADMIN' };
    expect(auth.autenticado).toBe(true);
    expect(auth.esAdmin).toBe(true);
    expect(auth.puedeEscribir).toBe(true);
  });

  it('TECNICO puede escribir pero no es admin', () => {
    const auth = useAuthStore();
    auth.user = { username: 't', rol: 'TECNICO' };
    expect(auth.puedeEscribir).toBe(true);
    expect(auth.esAdmin).toBe(false);
  });

  it('CONSULTA no puede escribir ni es admin', () => {
    const auth = useAuthStore();
    auth.user = { username: 'c', rol: 'CONSULTA' };
    expect(auth.puedeEscribir).toBe(false);
    expect(auth.esAdmin).toBe(false);
  });

  it('logout limpia token, usuario y localStorage', () => {
    const auth = useAuthStore();
    auth.token = 'jwt';
    auth.user = { username: 'admin', rol: 'ADMIN' };
    localStorage.setItem('sigec_token', 'jwt');
    auth.logout();
    expect(auth.token).toBe(null);
    expect(auth.user).toBe(null);
    expect(localStorage.getItem('sigec_token')).toBe(null);
  });
});
