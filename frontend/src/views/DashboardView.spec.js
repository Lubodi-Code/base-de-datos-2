import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import DashboardView from './DashboardView.vue';
import http from '../api/http';

vi.mock('../api/http', () => ({
  default: { get: vi.fn() },
  mensajeError: (error) => error?.message || 'Ocurrió un error inesperado.',
}));

const resumenAnterior = {
  totalEquipos: 29,
  incidentesAbiertos: 2,
  garantiasPorVencer: 1,
  tiempoMedioReparacionDias: 2,
  equiposPorProvincia: [{ etiqueta: 'San José', total: 13 }],
  equiposPorEstado: [{ etiqueta: 'ACTIVO', total: 27 }],
  incidentesPorEstado: [{ etiqueta: 'ABIERTO', total: 2 }],
};

const resumenActualizado = { ...resumenAnterior, totalEquipos: 30 };

describe('DashboardView — recuperación ante failover', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('mantiene los últimos datos mientras reconecta y anuncia el estado', async () => {
    sessionStorage.setItem('sigec_dashboard_resumen', JSON.stringify(resumenAnterior));

    let completar;
    http.get.mockImplementation((_url, config) => {
      config.onReintentoTransitorio();
      return new Promise((resolve) => {
        completar = resolve;
      });
    });

    const wrapper = mount(DashboardView);
    await flushPromises();

    expect(wrapper.text()).toContain('Reconectando con el servidor…');
    expect(wrapper.text()).toContain('29');
    expect(wrapper.get('[role="status"]').attributes('aria-live')).toBe('polite');
    expect(wrapper.get('button').attributes()).toHaveProperty('disabled');

    completar({ data: resumenActualizado });
    await flushPromises();

    expect(wrapper.text()).not.toContain('Reconectando con el servidor…');
    expect(wrapper.text()).toContain('30');
    expect(JSON.parse(sessionStorage.getItem('sigec_dashboard_resumen')).totalEquipos).toBe(30);
  });

  it('permite reintentar manualmente sin borrar los datos conservados', async () => {
    sessionStorage.setItem('sigec_dashboard_resumen', JSON.stringify(resumenAnterior));
    http.get
      .mockRejectedValueOnce({ response: { status: 503 } })
      .mockResolvedValueOnce({ data: resumenActualizado });

    const wrapper = mount(DashboardView);
    await flushPromises();

    expect(wrapper.text()).toContain('Los últimos datos permanecen visibles.');
    expect(wrapper.text()).toContain('29');

    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(http.get).toHaveBeenCalledTimes(2);
    expect(wrapper.text()).toContain('30');
    expect(wrapper.find('[role="alert"]').exists()).toBe(false);
  });
});
