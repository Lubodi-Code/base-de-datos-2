<script setup>
import { ref, onMounted } from 'vue';
import http, { mensajeError } from '../api/http';
import catalogos from '../api/catalogos';

const usuarios = ref([]);
const tecnicos = ref([]);
const error = ref('');
const ok = ref('');
const cargando = ref(true);

const roles = ['ADMIN', 'TECNICO', 'CONSULTA'];
const modal = ref(false);
const guardando = ref(false);
const form = ref(formVacio());

function formVacio() {
  return { username: '', password: '', nombreCompleto: '', rol: 'CONSULTA', idTecnico: '' };
}

async function cargar() {
  cargando.value = true; error.value = '';
  try {
    usuarios.value = (await http.get('/usuarios')).data;
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
}

function abrir() { form.value = formVacio(); ok.value = ''; modal.value = true; }

async function guardar() {
  guardando.value = true; error.value = '';
  try {
    const payload = { ...form.value };
    if (!payload.idTecnico) payload.idTecnico = null;
    await http.post('/usuarios', payload);
    modal.value = false;
    ok.value = `Usuario "${payload.username}" creado.`;
    cargar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}

onMounted(async () => {
  try { tecnicos.value = await catalogos.tecnicos(); } catch (e) { /* opcional */ }
  cargar();
});
</script>

<template>
  <div class="card">
    <div class="barra-acciones" style="justify-content: space-between">
      <h3 style="margin: 0">Usuarios del sistema</h3>
      <button @click="abrir">+ Nuevo usuario</button>
    </div>

    <div v-if="ok" class="alerta ok">{{ ok }}</div>
    <div v-if="error" class="alerta error">{{ error }}</div>

    <table>
      <thead>
        <tr><th>#</th><th>Usuario</th><th>Nombre completo</th><th>Rol</th><th>Estado</th></tr>
      </thead>
      <tbody>
        <tr v-for="u in usuarios" :key="u.id">
          <td>{{ u.id }}</td>
          <td><span class="mono">{{ u.username }}</span></td>
          <td>{{ u.nombreCompleto || '—' }}</td>
          <td><span class="badge" :class="u.rol === 'ADMIN' ? 'PROCESO' : (u.rol === 'TECNICO' ? 'TRASLADO' : 'RETIRADO')">{{ u.rol }}</span></td>
          <td><span class="badge" :class="u.activo ? 'ACTIVO' : 'BAJA'">{{ u.activo ? 'Activo' : 'Inactivo' }}</span></td>
        </tr>
        <tr v-if="!cargando && !usuarios.length"><td colspan="5" class="vacio">No hay usuarios.</td></tr>
        <tr v-if="cargando"><td colspan="5" class="vacio">Cargando…</td></tr>
      </tbody>
    </table>
  </div>

  <div v-if="modal" class="modal-fondo" @click.self="modal = false">
    <form class="modal" @submit.prevent="guardar">
      <h3>Nuevo usuario</h3>
      <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 16px">
        <div class="campo"><label>Usuario *</label><input v-model="form.username" required maxlength="40" /></div>
        <div class="campo"><label>Contraseña * (mín. 8)</label><input v-model="form.password" type="password" required minlength="8" maxlength="72" /></div>
      </div>
      <div class="campo"><label>Nombre completo</label><input v-model="form.nombreCompleto" maxlength="120" /></div>
      <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 16px">
        <div class="campo">
          <label>Rol *</label>
          <select v-model="form.rol" required>
            <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
          </select>
        </div>
        <div class="campo">
          <label>Técnico vinculado</label>
          <select v-model="form.idTecnico">
            <option value="">Ninguno</option>
            <option v-for="t in tecnicos" :key="t.id" :value="t.id">{{ t.etiqueta }}</option>
          </select>
        </div>
      </div>
      <div class="acciones">
        <button type="button" class="secundario" @click="modal = false">Cancelar</button>
        <button type="submit" :disabled="guardando">{{ guardando ? 'Creando…' : 'Crear usuario' }}</button>
      </div>
    </form>
  </div>
</template>
