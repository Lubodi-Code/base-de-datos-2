<script setup>
import { ref, watch } from 'vue';
import http, { mensajeError } from '../api/http';
import catalogos from '../api/catalogos';

const props = defineProps({
  abierto: { type: Boolean, default: false },
  equipoId: { type: Number, default: null }, // null => alta
});
const emit = defineEmits(['cerrar', 'guardado']);

const estados = ['ACTIVO', 'DANADO', 'RETIRADO', 'REEMPLAZADO'];

const form = ref(formVacio());
const error = ref('');
const guardando = ref(false);

// Catálogos
const provincias = ref([]);
const cantones = ref([]);
const edificios = ref([]);
const ubicaciones = ref([]);
const modelos = ref([]);
const plataformas = ref([]);
const contrataciones = ref([]);

// Cascada (no viaja al backend, solo guía la selección de ubicación)
const sel = ref({ idProvincia: '', idCanton: '', idEdificio: '' });
const ubicacionActual = ref(null); // {id, etiqueta} al editar

function formVacio() {
  return {
    idModelo: '', idUbicacion: '', idPlataforma: '', idContratacion: '',
    nombreEquipo: '', serie: '', numActivo: '', estado: 'ACTIVO',
    verWindows: '', licenciaWin: '', fechaInstalacion: '', vencGarantia: '',
  };
}

async function abrir() {
  error.value = '';
  sel.value = { idProvincia: '', idCanton: '', idEdificio: '' };
  cantones.value = []; edificios.value = []; ubicaciones.value = [];
  ubicacionActual.value = null;
  form.value = formVacio();

  // catálogos base en paralelo
  [provincias.value, modelos.value, plataformas.value, contrataciones.value] = await Promise.all([
    catalogos.provincias(), catalogos.modelos(), catalogos.plataformas(), catalogos.contrataciones(),
  ]);

  if (props.equipoId != null) {
    const { data } = await http.get(`/equipos/${props.equipoId}`);
    form.value = {
      idModelo: data.idModelo ?? '',
      idUbicacion: data.idUbicacion ?? '',
      idPlataforma: data.idPlataforma ?? '',
      idContratacion: data.idContratacion ?? '',
      nombreEquipo: data.nombreEquipo ?? '',
      serie: data.serie ?? '',
      numActivo: data.numActivo ?? '',
      estado: data.estado ?? 'ACTIVO',
      verWindows: data.verWindows ?? '',
      licenciaWin: data.licenciaWin ?? '',
      fechaInstalacion: data.fechaInstalacion ?? '',
      vencGarantia: data.vencGarantia ?? '',
    };
    // Preserva la ubicación actual como opción seleccionable.
    ubicacionActual.value = { id: data.idUbicacion, etiqueta: `${data.ubicacion} · ${data.edificio}` };
    ubicaciones.value = [ubicacionActual.value];
  }
}

watch(() => props.abierto, (v) => { if (v) abrir(); });

async function onProvincia() {
  sel.value.idCanton = ''; sel.value.idEdificio = '';
  edificios.value = []; ubicaciones.value = ubicacionActual.value ? [ubicacionActual.value] : [];
  cantones.value = sel.value.idProvincia ? await catalogos.cantones(sel.value.idProvincia) : [];
}
async function onCanton() {
  sel.value.idEdificio = '';
  ubicaciones.value = ubicacionActual.value ? [ubicacionActual.value] : [];
  edificios.value = sel.value.idCanton ? await catalogos.edificios(sel.value.idCanton) : [];
}
async function onEdificio() {
  ubicaciones.value = sel.value.idEdificio ? await catalogos.ubicaciones(sel.value.idEdificio) : [];
}

async function guardar() {
  guardando.value = true;
  error.value = '';
  try {
    const payload = { ...form.value };
    // Normaliza opcionales vacíos -> null
    ['idPlataforma', 'idContratacion', 'serie', 'numActivo', 'verWindows', 'licenciaWin',
     'fechaInstalacion', 'vencGarantia'].forEach((k) => { if (payload[k] === '') payload[k] = null; });
    if (props.equipoId != null) {
      await http.put(`/equipos/${props.equipoId}`, payload);
    } else {
      await http.post('/equipos', payload);
    }
    emit('guardado');
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}
</script>

<template>
  <div v-if="abierto" class="modal-fondo" @click.self="emit('cerrar')">
    <form class="modal" style="max-width: 660px" @submit.prevent="guardar">
      <h3>{{ equipoId != null ? 'Editar equipo' : 'Nuevo equipo' }}</h3>

      <div v-if="error" class="alerta error">{{ error }}</div>

      <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 16px">
        <div class="campo">
          <label>Nombre del equipo *</label>
          <input v-model="form.nombreEquipo" required maxlength="80" />
        </div>
        <div class="campo">
          <label>Modelo *</label>
          <select v-model="form.idModelo" required>
            <option value="" disabled>Seleccione…</option>
            <option v-for="m in modelos" :key="m.id" :value="m.id">{{ m.etiqueta }}</option>
          </select>
        </div>
      </div>

      <p class="nav-rotulo" style="padding: 4px 0 8px; color: var(--muted)">Ubicación física</p>
      <div class="grid" style="grid-template-columns: 1fr 1fr 1fr; gap: 0 12px">
        <div class="campo">
          <label>Provincia</label>
          <select v-model="sel.idProvincia" @change="onProvincia">
            <option value="">—</option>
            <option v-for="p in provincias" :key="p.id" :value="p.id">{{ p.etiqueta }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Cantón</label>
          <select v-model="sel.idCanton" :disabled="!cantones.length" @change="onCanton">
            <option value="">—</option>
            <option v-for="c in cantones" :key="c.id" :value="c.id">{{ c.etiqueta }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Edificio</label>
          <select v-model="sel.idEdificio" :disabled="!edificios.length" @change="onEdificio">
            <option value="">—</option>
            <option v-for="ed in edificios" :key="ed.id" :value="ed.id">{{ ed.etiqueta }}</option>
          </select>
        </div>
      </div>
      <div class="campo">
        <label>Ubicación *</label>
        <select v-model="form.idUbicacion" required>
          <option value="" disabled>Seleccione provincia → cantón → edificio…</option>
          <option v-for="u in ubicaciones" :key="u.id" :value="u.id">{{ u.etiqueta }}</option>
        </select>
      </div>

      <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 16px">
        <div class="campo">
          <label>Plataforma VMS</label>
          <select v-model="form.idPlataforma">
            <option value="">Sin plataforma</option>
            <option v-for="p in plataformas" :key="p.id" :value="p.id">{{ p.etiqueta }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Contratación</label>
          <select v-model="form.idContratacion">
            <option value="">Sin contratación</option>
            <option v-for="c in contrataciones" :key="c.id" :value="c.id">{{ c.etiqueta }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Serie</label>
          <input v-model="form.serie" maxlength="60" />
        </div>
        <div class="campo">
          <label>N.º de activo</label>
          <input v-model="form.numActivo" maxlength="20" />
        </div>
        <div class="campo">
          <label>Estado</label>
          <select v-model="form.estado">
            <option v-for="e in estados" :key="e" :value="e">{{ e }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Versión Windows</label>
          <input v-model="form.verWindows" maxlength="40" />
        </div>
        <div class="campo">
          <label>Fecha de instalación</label>
          <input v-model="form.fechaInstalacion" type="date" />
        </div>
        <div class="campo">
          <label>Vence garantía</label>
          <input v-model="form.vencGarantia" type="date" />
        </div>
      </div>

      <div class="acciones">
        <button type="button" class="secundario" @click="emit('cerrar')">Cancelar</button>
        <button type="submit" :disabled="guardando">{{ guardando ? 'Guardando…' : 'Guardar' }}</button>
      </div>
    </form>
  </div>
</template>
