# =========================================================
#  Backend SIGEC-PJ — build multietapa (Java 21 + Spring Boot)
# =========================================================

# --- Etapa 1: compilar y empaquetar el jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachea dependencias: primero el pom, luego el código.
COPY pom.xml .
RUN mvn -q -B -o dependency:go-offline 2>/dev/null || mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- Etapa 2: imagen de ejecución (solo JRE) ---
FROM eclipse-temurin:21-jre AS run
WORKDIR /app
# Usuario sin privilegios.
RUN useradd --system --uid 1001 sigec
COPY --from=build /app/target/*.jar app.jar
USER sigec
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
