# ── Etapa 1: build ──────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copia wrapper y pom primero (aprovecha caché de Docker)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia fuentes y compila
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Etapa 2: runtime ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root
RUN addgroup -S crm && adduser -S crm -G crm
USER crm

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
