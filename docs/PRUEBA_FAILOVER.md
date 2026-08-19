# Prueba reproducible de failover

## Objetivo

Comprobar que el clúster PostgreSQL/Patroni recupera las escrituras sin intervención
manual y que una transacción confirmada antes del fallo no se pierde.

- Criterio RTO: menos de 60 segundos.
- Criterio RPO: 0 transacciones confirmadas perdidas.
- Fallo ensayado: terminación abrupta (`SIGKILL`) del líder.

## Topología probada

- etcd: 3 miembros; conserva quórum con la pérdida de 1 miembro.
- PostgreSQL 16 + Patroni: 3 nodos; 1 líder, 1 réplica síncrona y 1 réplica adicional.
- HAProxy: primario en `localhost:5000` y réplicas en `localhost:5001`.
- Spring Boot: 2 instancias balanceadas por HAProxy en `https://localhost:8443`.
- pgBackRest: archivo continuo de WAL y respaldos cifrados con AES-256-CBC.

## Ejecución

```powershell
docker compose up -d --build
powershell -ExecutionPolicy Bypass -File .\scripts\test-failover.ps1
```

El script primero exige que los tres nodos Patroni estén activos; de esta forma evita
confundir una prueba válida de caída del líder con la pérdida simultánea de dos nodos.
Luego:

1. aplica al DCS los tiempos admitidos por Patroni (`ttl=20`, `loop_wait=2`, `retry_timeout=3`);
2. descubre el líder y selecciona una réplica superviviente para ejecutar las sondas;
3. confirma un marcador por el puerto de escritura de HAProxy;
4. envía `SIGKILL` al contenedor líder;
5. intenta escrituras cada 250 ms hasta que HAProxy acepta una;
6. comprueba que existen el marcador anterior y el posterior al fallo;
7. comprueba también que la API vuelva a responder HTTP 200;
8. arranca de nuevo el nodo caído, espera que se reincorpore y limpia la tabla de prueba.

Al terminar muestra por separado el RTO de escritura y el RTO observado por la API.
El bloque `finally` restaura al líder anterior incluso si la prueba falla o se interrumpe
por una excepción.

## Resultado de referencia

Corridas locales del 19-08-2026:

| Prueba | Líder anterior | Líder promovido | RTO escritura | RTO API | RPO |
|---:|---|---|---:|---:|---:|
| 1 | `patroni-2` | `patroni-1` | **24,327 s** | — | **0** |
| 2 | `patroni-3` | `patroni-2` | **25,558 s** | — | **0** |
| 3 | `patroni-3` | `patroni-2` | **24,657 s** | — | **0** |
| 4 | `patroni-2` | `patroni-3` | **25,359 s** | **27,475 s** | **0** |

En las cuatro pruebas se conservaron los dos marcadores confirmados. El RTO promedio
de escritura observado fue de **24,975 s**.

Patroni 4.1.5 exige un `ttl` mínimo de 20 segundos y ajusta automáticamente cualquier
valor inferior. Por ello, un RTO de 12–15 segundos no es alcanzable con la elección
automática nativa de esta versión. Lograrlo requeriría un supervisor externo con
fencing confiable; eliminar el lease de etcd sin fencing aumenta el riesgo de
*split brain* y no forma parte de esta topología local.

Durante la ventana de promoción, el frontend conserva el último resumen válido,
muestra `Reconectando con el servidor…` y reintenta automáticamente las respuestas
502/503. Si los reintentos se agotan, mantiene los datos y ofrece el botón `Reintentar`.

Después de cada recuperación, el líder anterior volvió como réplica `streaming`, con lag 0.
También se detuvo `etcd-1`: los otros dos miembros mantuvieron quórum, la consulta
de escritura devolvió `1` y el login de la API respondió HTTP 200.

## Verificación de respaldos

Ejecutar sobre el líder actual, sustituyendo el nombre del servicio si cambió:

```powershell
docker compose exec -T --user postgres patroni-1 `
  pgbackrest --stanza=sigecpj check

docker compose exec -T --user postgres patroni-1 `
  pgbackrest --stanza=sigecpj info
```

La corrida de referencia archivó correctamente el WAL y creó un respaldo completo
cifrado de 30,8 MB (4 MB comprimidos en el repositorio).

## Alcance

Esta prueba valida la topología local de Docker Compose. Para producción, cada
miembro de etcd y PostgreSQL debe residir en un host o zona de fallo independiente;
un único equipo Docker sigue siendo un punto único de fallo físico.
