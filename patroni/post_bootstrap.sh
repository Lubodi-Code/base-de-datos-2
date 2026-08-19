#!/bin/sh
# ponytail: crea rol/BD de app (el entrypoint oficial de postgres nunca corre, lo reemplaza Patroni)
# y carga DDL+seed una sola vez, al bootstrap del primario. Patroni pasa el connstring como $1.
set -e
psql "$1" -v ON_ERROR_STOP=1 -c "CREATE ROLE ${POSTGRES_USER} LOGIN PASSWORD '${POSTGRES_PASSWORD}'"
psql "$1" -v ON_ERROR_STOP=1 -c "CREATE DATABASE sigecpj OWNER ${POSTGRES_USER}"
psql "$1 dbname=sigecpj" -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/01_ddl.sql
if [ -f /docker-entrypoint-initdb.d/02_seed.sql ]; then
  psql "$1 dbname=sigecpj" -v ON_ERROR_STOP=1 -f /docker-entrypoint-initdb.d/02_seed.sql
fi
psql "$1 dbname=sigecpj" -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${POSTGRES_USER}"
psql "$1 dbname=sigecpj" -v ON_ERROR_STOP=1 -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${POSTGRES_USER}"
pgbackrest --stanza=sigecpj stanza-create
