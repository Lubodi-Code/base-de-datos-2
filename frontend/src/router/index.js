import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

import LoginView from '../views/LoginView.vue';
import DashboardView from '../views/DashboardView.vue';
import EquiposView from '../views/EquiposView.vue';
import EquipoDetalleView from '../views/EquipoDetalleView.vue';
import IncidentesView from '../views/IncidentesView.vue';
import MovimientosView from '../views/MovimientosView.vue';
import GarantiasView from '../views/GarantiasView.vue';
import UsuariosView from '../views/UsuariosView.vue';

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { publico: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { titulo: 'Tablero' } },
  { path: '/equipos', name: 'equipos', component: EquiposView, meta: { titulo: 'Inventario' } },
  { path: '/equipos/:id', name: 'equipo-detalle', component: EquipoDetalleView, meta: { titulo: 'Detalle del equipo' } },
  { path: '/incidentes', name: 'incidentes', component: IncidentesView, meta: { titulo: 'Incidentes' } },
  { path: '/movimientos', name: 'movimientos', component: MovimientosView, meta: { titulo: 'Movimientos' } },
  { path: '/garantias', name: 'garantias', component: GarantiasView, meta: { titulo: 'Garantías por vencer' } },
  { path: '/usuarios', name: 'usuarios', component: UsuariosView, meta: { titulo: 'Usuarios', soloAdmin: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (!to.meta.publico && !auth.autenticado) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  if (to.name === 'login' && auth.autenticado) {
    return { name: 'dashboard' };
  }
  if (to.meta.soloAdmin && !auth.esAdmin) {
    return { name: 'dashboard' };
  }
});

export default router;
