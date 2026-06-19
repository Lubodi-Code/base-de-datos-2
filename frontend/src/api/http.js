import axios from 'axios';

/**
 * Cliente HTTP central. Adjunta el JWT en cada petición y, ante un 401,
 * limpia la sesión y redirige al login.
 */
const http = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('sigec_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('sigec_token');
      localStorage.removeItem('sigec_user');
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);

/** Extrae un mensaje legible del error de Axios. */
export function mensajeError(error, porDefecto = 'Ocurrió un error inesperado.') {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    porDefecto
  );
}

export default http;
