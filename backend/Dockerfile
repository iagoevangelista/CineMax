# ==========================================
# 1. Etapa de compilación con Maven Oficial
# ==========================================
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copiamos solo el pom.xml y el código fuente
COPY pom.xml .
COPY src ./src

# Compilamos la aplicación usando el Maven de Docker
RUN mvn clean package -DskipTests

# ==========================================
# 2. Etapa de ejecución
# ==========================================
FROM openjdk:17-jdk-slim
WORKDIR /app

# Traemos el archivo .jar compilado
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Encendemos el servidor
ENTRYPOINT ["java", "-jar", "app.jar"]