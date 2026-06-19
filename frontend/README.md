# SIGEC-PJ — Frontend (SPA)

Aplicación de página única en **Vue 3 + Vite** que consume la API REST del backend.
Cubre login JWT, tablero de indicadores, inventario, incidentes y movimientos.

## Tecnologías

- **Vue 3** (Composition API, `<script setup>`)
- **Vite** (servidor de desarrollo + build)
- **Vue Router** (rutas protegidas por sesión)
- **Pinia** (estado de autenticación)
- **Axios** (cliente HTTP con interceptor de JWT)

## Estructura

```
src/
├── api/http.js        # Axios: baseURL /api/v1, JWT en cada request, manejo de 401
├── stores/auth.js     # Token + usuario (rol), persistidos en localStorage
├── router/index.js    # Rutas y guard de autenticación
├── App.vue            # Layout (barra lateral + topbar) cuando hay sesión
├── style.css          # Estilos institucionales
└── views/
    ├── LoginView.vue
    ├── DashboardView.vue
    ├── EquiposView.vue        # listado/búsqueda paginada con filtros
    ├── IncidentesView.vue     # listar, crear y cerrar incidentes
    └── MovimientosView.vue    # listar y registrar bajas/reemplazos/traslados
```

## Requisitos

- Node 18+ (probado con Node 22).
- El backend corriendo en `http://localhost:8080` (ver [`../backend`](../backend)).

## Ejecutar

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
```

Vite redirige `/api/*` al backend `:8080` (ver `vite.config.js`), por lo que no hay
problemas de CORS en desarrollo. Credenciales de demostración: `admin / admin123`.

## Build de producción

```bash
npm run build    # genera dist/
npm run preview  # sirve el build localmente
```

> En producción, las llamadas `/api/*` deben enrutarse al backend mediante el
> servidor web / balanceador (HAProxy o NGINX, ver la propuesta de alta disponibilidad).
