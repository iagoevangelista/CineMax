# ==========================================
# 1. Etapa de compilación (Build)
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos el wrapper de Maven y las configuraciones base
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Aseguramos que el wrapper tenga permisos de ejecución
RUN chmod +x ./mvnw

# Descargamos las dependencias de Spring Boot
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y compilamos ignorando los tests
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================================
# 2. Etapa de ejecución (Run)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Traemos únicamente el archivo .jar generado en el paso anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto dinámico
EXPOSE 8080

# Encendemos el servidor
ENTRYPOINT ["java", "-jar", "app.jar"]