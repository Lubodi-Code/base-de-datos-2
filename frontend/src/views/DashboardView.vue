<script setup>
import { computed, ref, onMounted } from 'vue';
import http, { mensajeError } from '../api/http';

const CACHE_KEY = 'sigec_dashboard_resumen';

function leerUltimoResumen() {
  try {
    return JSON.parse(sessionStorage.getItem(CACHE_KEY)) || null;
  } catch {
    sessionStorage.removeItem(CACHE_KEY);
    return null;
  }
}

const resumen = ref(leerUltimoResumen());
const error = ref('');
const solicitando = ref(false);
const reconectando = ref(false);

const maxProvincia = computed(() =>
  Math.max(1, ...(resumen.value?.equiposPorProvincia || []).map((p) => p.total))
);

async function cargarResumen() {
  if (solicitando.value) return;

  solicitando.value = true;
  reconectando.value = false;
  error.value = '';

  try {
    const { data } = await http.get('/dashboard/resumen', {
      onReintentoTransitorio: () => {
        reconectando.value = true;
      },
    });
    resumen.value = data;
    sessionStorage.setItem(CACHE_KEY, JSON.stringify(data));
  } catch (e) {
    if ([502, 503].includes(e.response?.status)) {
      error.value = resumen.value
        ? 'No se pudo actualizar el tablero. Los últimos datos permanecen visibles.'
        : 'No se pudo restablecer la conexión con el servidor.';
    } else {
      error.value = mensajeError(e);
    }
  } finally {
    solicitando.value = false;
    reconectando.value = false;
  }
}

onMounted(cargarResumen);

function dias(n) {
  return n == null ? '—' : `${Number(n).toFixed(1)} días`;
}
</script>

<template>
  <section class="dashboard" :aria-busy="solicitando">
    <div
      v-if="reconectando || error"
      class="alerta alerta-con-accion"
      :class="reconectando ? 'reconexion' : 'error'"
      :role="reconectando ? 'status' : 'alert'"
      aria-live="polite"
    >
      <span>{{ reconectando ? 'Reconectando con el servidor…' : error }}</span>
      <button
        type="button"
        class="secundario pequeno"
        :disabled="solicitando"
        @click="cargarResumen"
      >
        {{ solicitando ? 'Reintentando…' : 'Reintentar' }}
      </button>
    </div>

    <p v-if="solicitando && !resumen && !reconectando" class="vacio">Cargando indicadores…</p>

    <template v-if="resumen">
    <div class="grid kpis" style="margin-bottom: 18px">
      <div class="card kpi">
        <div class="valor">{{ resumen.totalEquipos }}</div>
        <div class="rotulo">Equipos registrados</div>
      </div>
      <div class="card kpi">
        <div class="valor">{{ resumen.incidentesAbiertos }}</div>
        <div class="rotulo">Incidentes abiertos</div>
      </div>
      <div class="card kpi">
        <div class="valor">{{ resumen.garantiasPorVencer }}</div>
        <div class="rotulo">Garantías por vencer (90 d)</div>
      </div>
      <div class="card kpi">
        <div class="valor">{{ dias(resumen.tiempoMedioReparacionDias) }}</div>
        <div class="rotulo">Tiempo medio de reparación</div>
      </div>
    </div>

    <div class="grid cols-2">
      <div class="card">
        <div class="titulo-seccion"><h3>Equipos por provincia</h3></div>
        <p v-if="!resumen.equiposPorProvincia.length" class="vacio">Sin datos.</p>
        <div v-for="p in resumen.equiposPorProvincia" :key="p.etiqueta" class="barra-prov">
          <div class="fila"><span>{{ p.etiqueta }}</span><b>{{ p.total }}</b></div>
          <div class="pista">
            <div class="relleno" :style="{ width: (p.total / maxProvincia) * 100 + '%' }" />
          </div>
        </div>
      </div>

      <div class="card">
        <div class="titulo-seccion"><h3>Equipos por estado</h3></div>
        <table>
          <tbody>
            <tr v-for="e in resumen.equiposPorEstado" :key="e.etiqueta">
              <td><span class="badge" :class="e.etiqueta">{{ e.etiqueta }}</span></td>
              <td style="text-align: right"><b>{{ e.total }}</b></td>
            </tr>
          </tbody>
        </table>

        <div class="titulo-seccion" style="margin-top: 18px"><h3>Incidentes por estado</h3></div>
        <table>
          <tbody>
            <tr v-for="i in resumen.incidentesPorEstado" :key="i.etiqueta">
              <td><span class="badge" :class="i.etiqueta">{{ i.etiqueta }}</span></td>
              <td style="text-align: right"><b>{{ i.total }}</b></td>
            </tr>
            <tr v-if="!resumen.incidentesPorEstado.length"><td class="vacio">Sin incidentes.</td></tr>
          </tbody>
        </table>
      </div>
    </div>
    </template>
  </section>
</template>
