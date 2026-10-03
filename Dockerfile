# ---- Etapa 1: compilar y probar ----
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /fuente
# Primero solo el pom: las dependencias quedan en cache mientras no cambie.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Las pruebas unitarias corren aqui: si fallan, no se construye la imagen.
RUN mvn -q -B package

# ---- Etapa 2: imagen liviana para ejecutar ----
FROM eclipse-temurin:17-jre
ENV TZ=America/Bogota
RUN useradd --system --create-home --shell /usr/sbin/nologin eden
WORKDIR /app
COPY --from=compilacion /fuente/target/eden-*.jar /app/eden.jar
USER eden
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/eden.jar"]
