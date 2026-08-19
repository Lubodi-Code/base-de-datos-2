<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { mensajeError } from '../api/http';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const username = ref('admin');
const password = ref('admin123');
const error = ref('');
const cargando = ref(false);

async function ingresar() {
  error.value = '';
  cargando.value = true;
  try {
    await auth.login(username.value, password.value);
    router.push(route.query.redirect?.toString() || '/dashboard');
  } catch (e) {
    error.value = mensajeError(e, 'Usuario o contraseña incorrectos.');
  } finally {
    cargando.value = false;
  }
}
</script>

<template>
  <div class="login-pantalla">
    <!-- Panel de marca institucional -->
    <section class="login-marca tex-grid">
      <span class="emblema" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="8.5" />
          <circle cx="12" cy="12" r="3.2" />
          <path d="M12 3.5 12 8 M20.5 12 16 12 M12 20.5 12 16 M3.5 12 8 12" />
        </svg>
      </span>

      <div class="lema">
        <h1>Vigilancia bajo control.</h1>
        <p>
          Inventario, incidentes y trazabilidad de los equipos de videovigilancia
          del Poder Judicial — centralizados, cifrados y siempre disponibles.
        </p>
      </div>

      <p class="firma">
        SISTEMA DE GESTIÓN DE EQUIPOS CCTV<br />
        <b>Poder Judicial de Costa Rica</b>
      </p>
    </section>

    <!-- Panel de acceso -->
    <section class="login-acceso">
      <form class="login-caja" @submit.prevent="ingresar">
        <p class="rotulo-acceso">Acceso restringido</p>
        <h2>Iniciar sesión</h2>

        <div v-if="error" class="alerta error" role="alert" aria-live="polite">{{ error }}</div>

        <div class="campo">
          <label for="login-username">Usuario</label>
          <input
            id="login-username"
            v-model="username"
            name="username"
            type="text"
            autocomplete="username"
            required
          />
        </div>
        <div class="campo">
          <label for="login-password">Contraseña</label>
          <input
            id="login-password"
            v-model="password"
            name="password"
            type="password"
            autocomplete="current-password"
            required
          />
        </div>

        <button type="submit" :disabled="cargando">
          {{ cargando ? 'Verificando…' : 'Ingresar al sistema' }}
        </button>

        <p class="pista">demo · admin / admin123</p>
      </form>
    </section>
  </div>
</template>
