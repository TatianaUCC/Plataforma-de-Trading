# ============================================================
# market_data_feed — Dockerfile multietapa
# ============================================================

# --- Etapa 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Descargar dependencias primero para aprovechar cache de capas
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuario no privilegiado
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/market-data-feed-1.0.0.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
