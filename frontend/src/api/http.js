import axios from 'axios';

/**
 * Cliente HTTP central. Adjunta el JWT en cada petición y, ante un 401,
 * limpia la sesión y redirige al login.
 */
const http = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

const ESTADOS_TRANSITORIOS = new Set([502, 503]);
const METODOS_SEGUROS = new Set(['get', 'head', 'options']);
const REINTENTOS_PREDETERMINADOS = 6;
const ESPERA_BASE_MS = 1000;
const ESPERA_MAXIMA_MS = 4000;

/** Indica si una respuesta se puede repetir sin duplicar escrituras. */
export function esErrorTransitorio(error) {
  const estado = error?.response?.status;
  const metodo = error?.config?.method?.toLowerCase();
  return ESTADOS_TRANSITORIOS.has(estado) && METODOS_SEGUROS.has(metodo);
}

/** Retroceso exponencial acotado: 1 s, 2 s y luego intervalos de 4 s. */
export function esperaReintentoMs(intento, baseMs = ESPERA_BASE_MS) {
  return Math.min(baseMs * 2 ** Math.max(0, intento - 1), ESPERA_MAXIMA_MS);
}

function esperar(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('sigec_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('sigec_token');
      localStorage.removeItem('sigec_user');
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }

    const config = error.config;
    if (config && esErrorTransitorio(error)) {
      const maximo = config.reintentosTransitorios ?? REINTENTOS_PREDETERMINADOS;
      const realizados = config.__reintentosTransitorios ?? 0;

      if (realizados < maximo) {
        const intento = realizados + 1;
        const esperaMs = esperaReintentoMs(intento, config.esperaReintentoBaseMs);
        config.__reintentosTransitorios = intento;
        config.onReintentoTransitorio?.({ intento, maximo, esperaMs });
        await esperar(esperaMs);
        return http(config);
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
