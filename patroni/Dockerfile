FROM postgres:16

ARG PATRONI_VERSION=4.1.5

RUN apt-get update \
    && apt-get install -y --no-install-recommends pgbackrest python3-pip \
    && pip install --break-system-packages --no-cache-dir \
        "patroni[etcd3]==${PATRONI_VERSION}" \
        psycopg2-binary==2.9.12 \
    && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["patroni", "/etc/patroni/patroni.yml"]
