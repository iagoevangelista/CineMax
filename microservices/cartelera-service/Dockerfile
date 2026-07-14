FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -N install
COPY common-library/pom.xml common-library/
COPY common-library/src common-library/src
WORKDIR /workspace/common-library
RUN mvn install -DskipTests
COPY cartelera-service/pom.xml /workspace/cartelera-service/
COPY cartelera-service/src /workspace/cartelera-service/src
WORKDIR /workspace/cartelera-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/cartelera-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx300m -Xss512k -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
