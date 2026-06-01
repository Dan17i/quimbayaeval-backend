# Etapa 1: Compilar con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Segunda etapa - imagen runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar JAR de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone puerto
EXPOSE 8080

# Variables de entorno
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/quimbayaeval
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV JWT_SECRET=tu-clave-secreta-muy-larga-y-segura-cambiar-en-produccion-debe-tener-minimo-256-bits

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
