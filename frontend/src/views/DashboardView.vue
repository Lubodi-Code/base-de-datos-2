<script setup>
import { ref, onMounted } from 'vue';
import http, { mensajeError } from '../api/http';

const resumen = ref(null);
const error = ref('');
const cargando = ref(true);

const maxProvincia = ref(1);

onMounted(async () => {
  try {
    const { data } = await http.get('/dashboard/resumen');
    resumen.value = data;
    maxProvincia.value = Math.max(1, ...(data.equiposPorProvincia || []).map((p) => p.total));
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
});

function dias(n) {
  return n == null ? '—' : `${Number(n).toFixed(1)} días`;
}
</script>

<template>
  <div v-if="error" class="alerta error">{{ error }}</div>
  <p v-if="cargando" class="vacio">Cargando indicadores…</p>

  <template v-else-if="resumen">
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
</template>
