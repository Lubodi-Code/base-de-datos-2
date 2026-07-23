<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import http, { mensajeError } from '../api/http';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);

const equipo = ref(null);
const credenciales = ref([]);
const error = ref('');
const ok = ref('');
const cargando = ref(true);

const tiposCred = ['SO', 'VMS', 'TOOLBOX'];

// Formularios
const formIfaz = ref({ direccionIp: '', mac: '', mascara: '', gateway: '', dns: '', puerto: '' });
const formCred = ref({ tipo: 'SO', usuario: '', secreto: '' });
const guardando = ref(false);

// Modal de secreto revelado
const revelado = ref({ abierto: false, tipo: '', usuario: '', secreto: '' });

async function cargar() {
  cargando.value = true;
  error.value = '';
  try {
    equipo.value = (await http.get(`/equipos/${id}`)).data;
    if (auth.puedeEscribir) {
      credenciales.value = (await http.get(`/equipos/${id}/credenciales`)).data;
    }
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    cargando.value = false;
  }
}

async function agregarInterfaz() {
  guardando.value = true; error.value = '';
  try {
    const payload = { ...formIfaz.value };
    ['mac', 'mascara', 'gateway', 'dns'].forEach((k) => { if (!payload[k]) payload[k] = null; });
    payload.puerto = payload.puerto === '' ? null : Number(payload.puerto);
    await http.post(`/equipos/${id}/interfaces`, payload);
    formIfaz.value = { direccionIp: '', mac: '', mascara: '', gateway: '', dns: '', puerto: '' };
    ok.value = 'Interfaz agregada.';
    await cargar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}

async function guardarCredencial() {
  guardando.value = true; error.value = '';
  try {
    await http.post(`/equipos/${id}/credenciales`, { ...formCred.value });
    formCred.value = { tipo: 'SO', usuario: '', secreto: '' };
    ok.value = 'Credencial guardada (cifrada AES-256-GCM).';
    await cargar();
  } catch (e) {
    error.value = mensajeError(e);
  } finally {
    guardando.value = false;
  }
}

async function revelar(idCred) {
  error.value = '';
  try {
    const { data } = await http.get(`/equipos/credenciales/${idCred}/revelar`);
    revelado.value = { abierto: true, tipo: data.tipo, usuario: data.usuario, secreto: data.secreto };
  } catch (e) {
    error.value = mensajeError(e, 'No fue posible revelar la credencial.');
  }
}

onMounted(cargar);
</script>

<template>
  <button class="secundario pequeno" style="margin-bottom: 16px" @click="router.push('/equipos')">‹ Volver al inventario</button>

  <div v-if="error" class="alerta error">{{ error }}</div>
  <div v-if="ok" class="alerta ok">{{ ok }}</div>
  <p v-if="cargando" class="vacio">Cargando…</p>

  <template v-else-if="equipo">
    <!-- Ficha del equipo -->
    <div class="card" style="margin-bottom: 18px">
      <div class="titulo-seccion">
        <h3>{{ equipo.nombreEquipo }}</h3>
        <span class="badge" :class="equipo.estado">{{ equipo.estado }}</span>
      </div>
      <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px">
        <div><label>N.º de activo</label><div class="mono">{{ equipo.numActivo || '—' }}</div></div>
        <div><label>Serie</label><div class="mono">{{ equipo.serie || '—' }}</div></div>
        <div><label>Modelo</label><div>{{ equipo.modelo }} ({{ equipo.tipo }})</div></div>
        <div><label>Ubicación</label><div>{{ equipo.ubicacion }}</div></div>
        <div><label>Edificio</label><div>{{ equipo.edificio }}</div></div>
        <div><label>Provincia</label><div>{{ equipo.provincia }}</div></div>
        <div><label>Plataforma VMS</label><div>{{ equipo.plataforma || '—' }}</div></div>
        <div><label>Contratación</label><div class="mono">{{ equipo.contratacion || '—' }}</div></div>
        <div><label>Instalación</label><div class="mono">{{ equipo.fechaInstalacion || '—' }}</div></div>
        <div><label>Vence garantía</label><div class="mono">{{ equipo.vencGarantia || '—' }}</div></div>
      </div>
    </div>

    <div class="grid cols-2">
      <!-- Interfaces de red -->
      <div class="card">
        <div class="titulo-seccion"><h3>Interfaces de red</h3></div>
        <table>
          <thead><tr><th>IP</th><th>MAC</th><th>Gateway</th><th>Puerto</th></tr></thead>
          <tbody>
            <tr v-for="i in equipo.interfaces" :key="i.id">
              <td><span class="mono">{{ i.direccionIp }}</span></td>
              <td><span class="mono">{{ i.mac || '—' }}</span></td>
              <td><span class="mono">{{ i.gateway || '—' }}</span></td>
              <td><span class="mono">{{ i.puerto || '—' }}</span></td>
            </tr>
            <tr v-if="!equipo.interfaces.length"><td colspan="4" class="vacio">Sin interfaces.</td></tr>
          </tbody>
        </table>

        <form v-if="auth.puedeEscribir" style="margin-top: 16px" @submit.prevent="agregarInterfaz">
          <p class="nav-rotulo" style="padding: 0 0 8px; color: var(--muted)">Agregar interfaz</p>
          <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 12px">
            <div class="campo"><label>IP *</label><input v-model="formIfaz.direccionIp" placeholder="172.24.x.x" required /></div>
            <div class="campo"><label>MAC</label><input v-model="formIfaz.mac" placeholder="a4:bb:6d:.." /></div>
            <div class="campo"><label>Máscara</label><input v-model="formIfaz.mascara" placeholder="255.255.255.0" /></div>
            <div class="campo"><label>Gateway</label><input v-model="formIfaz.gateway" /></div>
            <div class="campo"><label>DNS</label><input v-model="formIfaz.dns" /></div>
            <div class="campo"><label>Puerto</label><input v-model="formIfaz.puerto" type="number" min="1" max="65535" /></div>
          </div>
          <button type="submit" class="pequeno" :disabled="guardando">Agregar interfaz</button>
        </form>
      </div>

      <!-- Credenciales -->
      <div class="card">
        <div class="titulo-seccion"><h3>Credenciales cifradas</h3></div>
        <p v-if="!auth.puedeEscribir" class="vacio">Requiere rol ADMIN o TÉCNICO.</p>
        <template v-else>
          <table>
            <thead><tr><th>Tipo</th><th>Usuario</th><th></th></tr></thead>
            <tbody>
              <tr v-for="c in credenciales" :key="c.id">
                <td><span class="badge ACTIVO">{{ c.tipo }}</span></td>
                <td><span class="mono">{{ c.usuario || '—' }}</span></td>
                <td style="text-align: right">
                  <button v-if="auth.esAdmin" class="secundario pequeno" @click="revelar(c.id)">Revelar</button>
                  <span v-else class="mono" style="color: var(--faint)">••••••</span>
                </td>
              </tr>
              <tr v-if="!credenciales.length"><td colspan="3" class="vacio">Sin credenciales.</td></tr>
            </tbody>
          </table>

          <form style="margin-top: 16px" @submit.prevent="guardarCredencial">
            <p class="nav-rotulo" style="padding: 0 0 8px; color: var(--muted)">Agregar / reemplazar</p>
            <div class="grid" style="grid-template-columns: 1fr 1fr; gap: 0 12px">
              <div class="campo">
                <label>Tipo *</label>
                <select v-model="formCred.tipo">
                  <option v-for="t in tiposCred" :key="t" :value="t">{{ t }}</option>
                </select>
              </div>
              <div class="campo"><label>Usuario</label><input v-model="formCred.usuario" maxlength="60" /></div>
            </div>
            <div class="campo"><label>Secreto *</label><input v-model="formCred.secreto" type="password" required /></div>
            <button type="submit" class="pequeno" :disabled="guardando">Guardar credencial</button>
          </form>
        </template>
      </div>
    </div>
  </template>

  <!-- Modal secreto revelado -->
  <div v-if="revelado.abierto" class="modal-fondo" @click.self="revelado.abierto = false">
    <div class="modal" style="max-width: 440px">
      <h3>Credencial {{ revelado.tipo }}</h3>
      <div class="alerta error" style="background: var(--amber-soft); color: var(--amber); border-color: #ebd5a6">
        Este acceso queda registrado en la bitácora de auditoría.
      </div>
      <div class="campo"><label>Usuario</label><div class="mono">{{ revelado.usuario || '—' }}</div></div>
      <div class="campo"><label>Secreto</label>
        <div class="mono" style="background: var(--paper); padding: 10px 12px; border-radius: var(--radius-sm); word-break: break-all">{{ revelado.secreto }}</div>
      </div>
      <div class="acciones">
        <button class="secundario" @click="revelado.abierto = false">Cerrar</button>
      </div>
    </div>
  </div>
</template>
