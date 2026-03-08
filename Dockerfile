# Usar imagen base de Java 17
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY mvn ./mvn

# Descargar dependencias
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar
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
