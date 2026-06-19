<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

const mostrarShell = computed(() => auth.autenticado && route.name !== 'login');
const titulo = computed(() => route.meta?.titulo || 'SIGEC-PJ');

function salir() {
  auth.logout();
  router.push({ name: 'login' });
}
</script>

<template>
  <div v-if="mostrarShell" class="app-shell">
    <aside class="sidebar tex-grid">
      <div class="marca">
        <span class="emblema" aria-hidden="true">
          <!-- Apertura/lente CCTV: vigilancia institucional -->
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
            <circle cx="12" cy="12" r="8.5" />
            <circle cx="12" cy="12" r="3.2" />
            <path d="M12 3.5 12 8 M20.5 12 16 12 M12 20.5 12 16 M3.5 12 8 12" />
          </svg>
        </span>
        <div>
          <h1>SIGEC-PJ</h1>
          <span>Equipos CCTV</span>
        </div>
      </div>

      <p class="nav-rotulo">Operación</p>
      <nav>
        <RouterLink to="/dashboard">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>
          <span>Tablero</span>
        </RouterLink>
        <RouterLink to="/equipos">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><rect x="3" y="4" width="18" height="13" rx="2"/><path d="M8 21h8 M12 17v4"/></svg>
          <span>Inventario</span>
        </RouterLink>
        <RouterLink to="/incidentes">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M12 3 2.5 19.5h19L12 3Z"/><path d="M12 10v4 M12 17.5v.01"/></svg>
          <span>Incidentes</span>
        </RouterLink>
        <RouterLink to="/movimientos">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M7 7h11l-3-3 M17 17H6l3 3"/></svg>
          <span>Movimientos</span>
        </RouterLink>
      </nav>

      <div class="pie">
        <b>Fase 2</b> · Base de Datos II<br />
        Poder Judicial · Costa Rica
      </div>
    </aside>

    <div class="contenido">
      <header class="topbar">
        <div class="titulo-pag">
          <h2>{{ titulo }}</h2>
          <span class="migaja">SIGEC-PJ</span>
        </div>
        <div class="usuario">
          <span class="chip-usuario">
            <span class="nom">{{ auth.user?.username }}</span>
            <span class="rol">{{ auth.rol }}</span>
          </span>
          <button class="secundario pequeno" @click="salir">Salir</button>
        </div>
      </header>
      <main class="main">
        <RouterView />
      </main>
    </div>
  </div>

  <RouterView v-else />
</template>
