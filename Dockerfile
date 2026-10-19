
FROM maven:3.9-eclipse-temurin-17 AS constructor

WORKDIR /build

# Se copia primero solo el pom.xml y se descargan las dependencias.

COPY pom.xml .

# Ahora si el codigo fuente
COPY src ./src

# Se genera el JAR ejecutable (se omiten las pruebas para acelerar)
RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine

LABEL autor="Arnulfo Calderon Corredor"

LABEL descripcion="Servidor web Spring Boot + Hibernate "

WORKDIR /app

# Buena practica de seguridad: no ejecutar como root dentro del contenedor
RUN addgroup -S spring && adduser -S spring -G spring

# El nombre del JAR viene de <finalName>gestorPedidos</finalName> del pom
COPY --from=constructor /build/target/gestorPedidos.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring

EXPOSE 8080

# Docker consulta este endpoint para saber si la app esta sana.
# start-period da 40 s de gracia mientras Spring Boot arranca.
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]