import http from './http';

/**
 * Funciones de carga de catálogos (todas devuelven listas de OpcionDto {id, etiqueta}).
 * Centralizadas para reutilizar entre el formulario de equipos, los filtros y las
 * vistas de incidentes/movimientos/usuarios.
 */
export const catalogos = {
  provincias: () => http.get('/catalogos/provincias').then((r) => r.data),
  cantones: (idProvincia) => http.get(`/catalogos/provincias/${idProvincia}/cantones`).then((r) => r.data),
  edificios: (idCanton) => http.get(`/catalogos/cantones/${idCanton}/edificios`).then((r) => r.data),
  ubicaciones: (idEdificio) => http.get(`/catalogos/edificios/${idEdificio}/ubicaciones`).then((r) => r.data),
  modelos: () => http.get('/catalogos/modelos').then((r) => r.data),
  plataformas: () => http.get('/catalogos/plataformas').then((r) => r.data),
  contrataciones: () => http.get('/catalogos/contrataciones').then((r) => r.data),
  tecnicos: () => http.get('/catalogos/tecnicos').then((r) => r.data),
};

export default catalogos;
