<script setup>
import { ref, computed, onMounted } from 'vue';
import http, { mensajeError } from '../api/http';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const pagina = ref({ content: [], number: 0, totalPages: 0, totalElements: 0 });
const page = ref(0);
const error = ref('');
const ok = ref('');
const cargando = ref(false);

const equipos = ref([]);
const tecnicos = ref([]);

const modal = ref(false);
const guardando = ref(false);
const form = ref(crearForm());

const esReemplazo = computed(() => form.value.tipo === 'REEMPLAZO');

function crearForm() {
  return { idEquipo: '', tipo: 'BAJA', idTecnico: '', motivo: '', idEquipoSustituto: '' };
}

async function cargar() {
  cargando.value = true;
  error.value = '';
  try {
    pagina.value = (await http.get('/movimientos', { params: { page: page.value, size: 10 } })).data;
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
}

function irA(n) { page.value = n; cargar(); }

function abrirNuevo() {
  form.value = crearForm();
  ok.value = '';
  modal.value = true;
}

async function guardar() {
  guardando.value = true;
  error.value = '';
  try {
    const payload = { idEquipo: form.value.idEquipo, tipo: form.value.tipo, motivo: form.value.motivo };
    if (form.value.idTecnico) payload.idTecnico = form.value.idTecnico;
    if (esReemplazo.value && form.value.idEquipoSustituto) payload.idEquipoSustituto = form.value.idEquipoSustituto;
    await http.post('/movimientos', payload);
    modal.value = false;
    ok.value = 'Movimiento registrado. El estado del equipo se actualizó según el tipo.';
    page.value = 0;
    cargar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}

function fmtFecha(iso) {
  return iso ? new Date(iso).toLocaleString('es-CR', { dateStyle: 'short', timeStyle: 'short' }) : '—';
}

onMounted(async () => {
  try {
    equipos.value = (await http.get('/equipos', { params: { size: 200 } })).data.content;
    tecnicos.value = (await http.get('/catalogos/tecnicos')).data;
  } catch (e) { /* selectores opcionales */ }
  cargar();
});
</script>

<template>
  <div class="card">
    <div class="barra-acciones" style="justify-content: space-between">
      <h3 style="margin: 0">Bajas, reemplazos y traslados</h3>
      <button v-if="auth.puedeEscribir" @click="abrirNuevo">+ Nuevo movimiento</button>
    </div>

    <div v-if="ok" class="alerta ok">{{ ok }}</div>
    <div v-if="error" class="alerta error">{{ error }}</div>

    <table>
      <thead>
        <tr><th>#</th><th>Equipo</th><th>Tipo</th><th>Fecha</th><th>Técnico</th></tr>
      </thead>
      <tbody>
        <tr v-for="m in pagina.content" :key="m.id">
          <td>{{ m.id }}</td>
          <td>{{ m.equipo }}</td>
          <td><span class="badge" :class="m.tipo">{{ m.tipo }}</span></td>
          <td>{{ fmtFecha(m.fecha) }}</td>
          <td>{{ m.tecnico || '—' }}</td>
        </tr>
        <tr v-if="!cargando && !pagina.content.length"><td colspan="5" class="vacio">No hay movimientos.</td></tr>
        <tr v-if="cargando"><td colspan="5" class="vacio">Cargando…</td></tr>
      </tbody>
    </table>

    <div class="paginacion">
      <span class="total">{{ pagina.totalElements }} resultados</span>
      <button class="secundario pequeno" :disabled="page === 0" @click="irA(page - 1)">‹ Anterior</button>
      <span>Página {{ pagina.number + 1 }} de {{ pagina.totalPages || 1 }}</span>
      <button class="secundario pequeno" :disabled="page + 1 >= pagina.totalPages" @click="irA(page + 1)">Siguiente ›</button>
    </div>
  </div>

  <div v-if="modal" class="modal-fondo" @click.self="modal = false">
    <form class="modal" @submit.prevent="guardar">
      <h3>Nuevo movimiento</h3>
      <div class="campo">
        <label>Equipo *</label>
        <select v-model="form.idEquipo" required>
          <option value="" disabled>Seleccione…</option>
          <option v-for="e in equipos" :key="e.id" :value="e.id">{{ e.nombreEquipo }} ({{ e.numActivo || e.id }})</option>
        </select>
      </div>
      <div class="campo">
        <label>Tipo *</label>
        <select v-model="form.tipo" required>
          <option value="BAJA">BAJA — el equipo pasa a RETIRADO</option>
          <option value="REEMPLAZO">REEMPLAZO — el equipo pasa a REEMPLAZADO</option>
          <option value="TRASLADO">TRASLADO — sin cambio de estado</option>
        </select>
      </div>
      <div v-if="esReemplazo" class="campo">
        <label>Equipo sustituto *</label>
        <select v-model="form.idEquipoSustituto" :required="esReemplazo">
          <option value="" disabled>Seleccione…</option>
          <option v-for="e in equipos.filter((x) => x.id !== form.idEquipo)" :key="e.id" :value="e.id">
            {{ e.nombreEquipo }} ({{ e.numActivo || e.id }})
          </option>
        </select>
      </div>
      <div class="campo">
        <label>Técnico</label>
        <select v-model="form.idTecnico">
          <option value="">Sin asignar</option>
          <option v-for="t in tecnicos" :key="t.id" :value="t.id">{{ t.etiqueta }}</option>
        </select>
      </div>
      <div class="campo">
        <label>Motivo</label>
        <textarea v-model="form.motivo" rows="2" />
      </div>
      <div class="acciones">
        <button type="button" class="secundario" @click="modal = false">Cancelar</button>
        <button type="submit" :disabled="guardando">{{ guardando ? 'Guardando…' : 'Registrar' }}</button>
      </div>
    </form>
  </div>
</template>
