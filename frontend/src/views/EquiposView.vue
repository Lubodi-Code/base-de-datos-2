<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import http, { mensajeError } from '../api/http';
import catalogos from '../api/catalogos';
import { useAuthStore } from '../stores/auth';
import EquipoFormModal from '../components/EquipoFormModal.vue';

const auth = useAuthStore();
const router = useRouter();

const filtros = ref({ texto: '', estado: '', idProvincia: '', ip: '' });
const provincias = ref([]);
const pagina = ref({ content: [], number: 0, totalPages: 0, totalElements: 0 });
const page = ref(0);
const error = ref('');
const ok = ref('');
const cargando = ref(false);

const estados = ['ACTIVO', 'DANADO', 'RETIRADO', 'REEMPLAZADO'];

// Modal alta/edición
const modal = ref({ abierto: false, equipoId: null });

async function cargar() {
  cargando.value = true;
  error.value = '';
  try {
    const params = { page: page.value, size: 10 };
    if (filtros.value.texto) params.texto = filtros.value.texto;
    if (filtros.value.estado) params.estado = filtros.value.estado;
    if (filtros.value.idProvincia) params.idProvincia = filtros.value.idProvincia;
    if (filtros.value.ip) params.ip = filtros.value.ip;
    pagina.value = (await http.get('/equipos', { params })).data;
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
}

function buscar() { page.value = 0; cargar(); }
function irA(n) { page.value = n; cargar(); }

function nuevo() { modal.value = { abierto: true, equipoId: null }; }
function editar(id) { modal.value = { abierto: true, equipoId: id }; }
function cerrarModal() { modal.value.abierto = false; }
function guardado() {
  modal.value.abierto = false;
  ok.value = 'Equipo guardado.';
  cargar();
}

onMounted(async () => {
  try { provincias.value = await catalogos.provincias(); } catch (e) { /* opcional */ }
  cargar();
});
</script>

<template>
  <div class="card">
    <div class="barra-acciones" style="justify-content: space-between">
      <div style="display: flex; gap: 12px; align-items: flex-end; flex-wrap: wrap">
        <div class="campo" style="min-width: 180px">
          <label>Texto (nombre, serie, activo)</label>
          <input v-model="filtros.texto" placeholder="Buscar…" @keyup.enter="buscar" />
        </div>
        <div class="campo">
          <label>Estado</label>
          <select v-model="filtros.estado">
            <option value="">Todos</option>
            <option v-for="e in estados" :key="e" :value="e">{{ e }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Provincia</label>
          <select v-model="filtros.idProvincia">
            <option value="">Todas</option>
            <option v-for="p in provincias" :key="p.id" :value="p.id">{{ p.etiqueta }}</option>
          </select>
        </div>
        <div class="campo">
          <label>IP</label>
          <input v-model="filtros.ip" placeholder="172.24.x.x" @keyup.enter="buscar" />
        </div>
        <button @click="buscar">Buscar</button>
      </div>
      <button v-if="auth.puedeEscribir" @click="nuevo">+ Nuevo equipo</button>
    </div>

    <div v-if="ok" class="alerta ok">{{ ok }}</div>
    <div v-if="error" class="alerta error">{{ error }}</div>

    <table>
      <thead>
        <tr>
          <th>#</th><th>Equipo</th><th>Activo</th><th>Modelo</th><th>Tipo</th>
          <th>Ubicación</th><th>Estado</th><th>Garantía</th><th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="e in pagina.content" :key="e.id">
          <td>{{ e.id }}</td>
          <td>{{ e.nombreEquipo }}</td>
          <td><span class="mono">{{ e.numActivo || '—' }}</span></td>
          <td>{{ e.modelo }}</td>
          <td>{{ e.tipo }}</td>
          <td>{{ e.ubicacion }}</td>
          <td><span class="badge" :class="e.estado">{{ e.estado }}</span></td>
          <td><span class="mono">{{ e.vencGarantia || '—' }}</span></td>
          <td style="text-align: right; white-space: nowrap">
            <button class="secundario pequeno" @click="router.push(`/equipos/${e.id}`)">Ver</button>
            <button v-if="auth.puedeEscribir" class="secundario pequeno" style="margin-left: 6px" @click="editar(e.id)">Editar</button>
          </td>
        </tr>
        <tr v-if="!cargando && !pagina.content.length">
          <td colspan="9" class="vacio">No se encontraron equipos.</td>
        </tr>
        <tr v-if="cargando"><td colspan="9" class="vacio">Cargando…</td></tr>
      </tbody>
    </table>

    <div class="paginacion">
      <span class="total">{{ pagina.totalElements }} resultados</span>
      <button class="secundario pequeno" :disabled="page === 0" @click="irA(page - 1)">‹ Anterior</button>
      <span>Página {{ pagina.number + 1 }} de {{ pagina.totalPages || 1 }}</span>
      <button class="secundario pequeno" :disabled="page + 1 >= pagina.totalPages" @click="irA(page + 1)">Siguiente ›</button>
    </div>
  </div>

  <EquipoFormModal
    :abierto="modal.abierto"
    :equipo-id="modal.equipoId"
    @cerrar="cerrarModal"
    @guardado="guardado"
  />
</template>
