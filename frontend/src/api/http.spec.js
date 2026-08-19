import { describe, it, expect } from 'vitest';
import { esErrorTransitorio, esperaReintentoMs, mensajeError } from './http';

describe('mensajeError — extracción de mensaje de error de Axios', () => {
  it('prioriza response.data.message', () => {
    const err = { response: { data: { message: 'Regla violada' } }, message: 'Request failed' };
    expect(mensajeError(err)).toBe('Regla violada');
  });

  it('usa response.data.error si no hay message', () => {
    const err = { response: { data: { error: 'Not Found' } } };
    expect(mensajeError(err)).toBe('Not Found');
  });

  it('cae a error.message cuando no hay response', () => {
    expect(mensajeError({ message: 'Network Error' })).toBe('Network Error');
  });

  it('usa el valor por defecto cuando no hay nada', () => {
    expect(mensajeError({}, 'Falló')).toBe('Falló');
  });
});

describe('reintentos de disponibilidad', () => {
  it.each([502, 503])('reintenta GET cuando recibe %s', (status) => {
    expect(esErrorTransitorio({ response: { status }, config: { method: 'get' } })).toBe(true);
  });

  it('no reintenta escrituras para evitar operaciones duplicadas', () => {
    expect(esErrorTransitorio({ response: { status: 503 }, config: { method: 'post' } })).toBe(false);
  });

  it('limita el retroceso exponencial a cuatro segundos', () => {
    expect([1, 2, 3, 4, 5].map((intento) => esperaReintentoMs(intento))).toEqual([
      1000, 2000, 4000, 4000, 4000,
    ]);
  });
});
