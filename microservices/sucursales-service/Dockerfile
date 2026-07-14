FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -N install
COPY common-library/pom.xml common-library/
COPY common-library/src common-library/src
WORKDIR /workspace/common-library
RUN mvn install -DskipTests
COPY sucursales-service/pom.xml /workspace/sucursales-service/
COPY sucursales-service/src /workspace/sucursales-service/src
WORKDIR /workspace/sucursales-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/sucursales-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx300m -Xss512k -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
