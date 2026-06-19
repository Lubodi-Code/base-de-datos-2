import { describe, it, expect } from 'vitest';
import { mensajeError } from './http';

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
