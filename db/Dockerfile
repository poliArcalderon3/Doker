# =====================================================================
#  IMAGEN PROPIA DE LA BASE DE DATOS
#  Construir:  docker build -t pedidos-mysql:1.0 ./db
# =====================================================================
FROM mysql:8.0

LABEL autor="Arnulfo Calderon Corredor"

LABEL descripcion="MySQL 8 con el esquema pedidosdb (tablas cliente y pedido)"

ENV MYSQL_ROOT_PASSWORD=rootpass
ENV MYSQL_DATABASE=pedidosdb
ENV MYSQL_USER=appuser
ENV MYSQL_PASSWORD=Pruebas123

# carpeta, la primera vez que se inicializa el contenedor.
COPY init/ /docker-entrypoint-initdb.d/

EXPOSE 3306

CMD ["mysqld", \
     "--character-set-server=utf8mb4", \
     "--collation-server=utf8mb4_unicode_ci"]