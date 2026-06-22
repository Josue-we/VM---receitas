#!/bin/bash
# Cria uma base de dados para cada nome listado em POSTGRES_MULTIPLE_DATABASES.
# Executado automaticamente pelo container do Postgres no primeiro start
# (via /docker-entrypoint-initdb.d).
set -e
set -u

function create_database() {
    local database=$1
    echo "Criando base '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
EOSQL
}

if [ -n "${POSTGRES_MULTIPLE_DATABASES:-}" ]; then
    echo "Criando múltiplas bases: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_database "$db"
    done
fi
