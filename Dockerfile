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

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
