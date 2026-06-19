<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import http, { mensajeError } from '../api/http';

const router = useRouter();
const lista = ref([]);
const error = ref('');
const cargando = ref(true);

function urgencia(dias) {
  if (dias <= 15) return 'ABIERTO';   // rojo
  if (dias <= 45) return 'PROCESO';   // ámbar
  return 'ACTIVO';                    // verde
}

onMounted(async () => {
  try {
    lista.value = (await http.get('/equipos/garantias-por-vencer')).data;
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
});
</script>

<template>
  <div class="card">
    <div class="titulo-seccion">
      <h3>Garantías por vencer · próximos 90 días</h3>
    </div>

    <div v-if="error" class="alerta error">{{ error }}</div>

    <table>
      <thead>
        <tr><th>Equipo</th><th>Activo</th><th>Edificio</th><th>Provincia</th><th>Vence</th><th>Días restantes</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="g in lista" :key="g.idEquipo">
          <td>{{ g.nombreEquipo }}</td>
          <td><span class="mono">{{ g.numActivo || '—' }}</span></td>
          <td><span class="mono">{{ g.cuentaPj }}</span> · {{ g.edificio }}</td>
          <td>{{ g.provincia }}</td>
          <td><span class="mono">{{ g.vencGarantia }}</span></td>
          <td><span class="badge" :class="urgencia(g.diasRestantes)">{{ g.diasRestantes }} días</span></td>
          <td style="text-align: right">
            <button class="secundario pequeno" @click="router.push(`/equipos/${g.idEquipo}`)">Ver</button>
          </td>
        </tr>
        <tr v-if="!cargando && !lista.length">
          <td colspan="7" class="vacio">No hay garantías por vencer en los próximos 90 días. ✓</td>
        </tr>
        <tr v-if="cargando"><td colspan="7" class="vacio">Cargando…</td></tr>
      </tbody>
    </table>
  </div>
</template>
