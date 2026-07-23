<script setup>
import { ref, onMounted } from 'vue';
import http, { mensajeError } from '../api/http';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const filtroEstado = ref('');
const pagina = ref({ content: [], number: 0, totalPages: 0, totalElements: 0 });
const page = ref(0);
const error = ref('');
const ok = ref('');
const cargando = ref(false);

const equipos = ref([]);
const tecnicos = ref([]);
const estados = ['ABIERTO', 'PROCESO', 'CERRADO'];

const modal = ref(false);
const guardando = ref(false);
const form = ref(crearForm());

const cierre = ref({ abierto: false, id: null, fechaReparacion: '', trabajoRealizado: '' });

function crearForm() {
  return { idEquipo: '', idTecnico: '', fechaObservacion: new Date().toISOString().slice(0, 10), detalle: '', estado: 'ABIERTO', medidasATomar: '' };
}

async function cargar() {
  cargando.value = true;
  error.value = '';
  try {
    const params = { page: page.value, size: 10 };
    if (filtroEstado.value) params.estado = filtroEstado.value;
    pagina.value = (await http.get('/incidentes', { params })).data;
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
}

function buscar() { page.value = 0; cargar(); }
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
    const payload = { ...form.value };
    if (!payload.idTecnico) payload.idTecnico = null;
    await http.post('/incidentes', payload);
    modal.value = false;
    ok.value = 'Incidente registrado.';
    buscar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}

function abrirCierre(inc) {
  cierre.value = { abierto: true, id: inc.id, fechaReparacion: new Date().toISOString().slice(0, 10), trabajoRealizado: '' };
}

async function confirmarCierre() {
  guardando.value = true;
  error.value = '';
  try {
    await http.patch(`/incidentes/${cierre.value.id}/cerrar`, {
      fechaReparacion: cierre.value.fechaReparacion,
      trabajoRealizado: cierre.value.trabajoRealizado,
    });
    cierre.value.abierto = false;
    ok.value = 'Incidente cerrado.';
    cargar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
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
      <div class="campo">
        <label>Filtrar por estado</label>
        <select v-model="filtroEstado" @change="buscar">
          <option value="">Todos</option>
          <option v-for="e in estados" :key="e" :value="e">{{ e }}</option>
        </select>
      </div>
      <button v-if="auth.puedeEscribir" @click="abrirNuevo">+ Nuevo incidente</button>
    </div>

    <div v-if="ok" class="alerta ok">{{ ok }}</div>
    <div v-if="error" class="alerta error">{{ error }}</div>

    <table>
      <thead>
        <tr><th>#</th><th>Equipo</th><th>Técnico</th><th>Observado</th><th>Reparado</th><th>Estado</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="i in pagina.content" :key="i.id">
          <td>{{ i.id }}</td>
          <td>{{ i.equipo }}</td>
          <td>{{ i.tecnico || '—' }}</td>
          <td>{{ i.fechaObservacion }}</td>
          <td>{{ i.fechaReparacion || '—' }}</td>
          <td><span class="badge" :class="i.estado">{{ i.estado }}</span></td>
          <td style="text-align: right">
            <button v-if="auth.puedeEscribir && i.estado !== 'CERRADO'" class="secundario pequeno" @click="abrirCierre(i)">Cerrar</button>
          </td>
        </tr>
        <tr v-if="!cargando && !pagina.content.length"><td colspan="7" class="vacio">No hay incidentes.</td></tr>
        <tr v-if="cargando"><td colspan="7" class="vacio">Cargando…</td></tr>
      </tbody>
    </table>

    <div class="paginacion">
      <span class="total">{{ pagina.totalElements }} resultados</span>
      <button class="secundario pequeno" :disabled="page === 0" @click="irA(page - 1)">‹ Anterior</button>
      <span>Página {{ pagina.number + 1 }} de {{ pagina.totalPages || 1 }}</span>
      <button class="secundario pequeno" :disabled="page + 1 >= pagina.totalPages" @click="irA(page + 1)">Siguiente ›</button>
    </div>
  </div>

  <!-- Modal nuevo incidente -->
  <div v-if="modal" class="modal-fondo" @click.self="modal = false">
    <form class="modal" @submit.prevent="guardar">
      <h3>Nuevo incidente</h3>
      <div class="campo">
        <label>Equipo *</label>
        <select v-model="form.idEquipo" required>
          <option value="" disabled>Seleccione…</option>
          <option v-for="e in equipos" :key="e.id" :value="e.id">{{ e.nombreEquipo }} ({{ e.numActivo || e.id }})</option>
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
        <label>Fecha de observación *</label>
        <input v-model="form.fechaObservacion" type="date" required />
      </div>
      <div class="campo">
        <label>Estado</label>
        <select v-model="form.estado">
          <option value="ABIERTO">ABIERTO</option>
          <option value="PROCESO">PROCESO</option>
        </select>
      </div>
      <div class="campo">
        <label>Detalle</label>
        <textarea v-model="form.detalle" rows="2" />
      </div>
      <div class="campo">
        <label>Medidas a tomar</label>
        <textarea v-model="form.medidasATomar" rows="2" />
      </div>
      <div class="acciones">
        <button type="button" class="secundario" @click="modal = false">Cancelar</button>
        <button type="submit" :disabled="guardando">{{ guardando ? 'Guardando…' : 'Registrar' }}</button>
      </div>
    </form>
  </div>

  <!-- Modal cierre -->
  <div v-if="cierre.abierto" class="modal-fondo" @click.self="cierre.abierto = false">
    <form class="modal" @submit.prevent="confirmarCierre">
      <h3>Cerrar incidente #{{ cierre.id }}</h3>
      <div class="campo">
        <label>Fecha de reparación *</label>
        <input v-model="cierre.fechaReparacion" type="date" required />
      </div>
      <div class="campo">
        <label>Trabajo realizado</label>
        <textarea v-model="cierre.trabajoRealizado" rows="3" />
      </div>
      <div class="acciones">
        <button type="button" class="secundario" @click="cierre.abierto = false">Cancelar</button>
        <button type="submit" :disabled="guardando">{{ guardando ? 'Cerrando…' : 'Confirmar cierre' }}</button>
      </div>
    </form>
  </div>
</template>
