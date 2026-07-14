FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY service-discovery/pom.xml service-discovery/
COPY service-discovery/src service-discovery/src
WORKDIR /workspace/service-discovery
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/service-discovery/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx300m -Xss512k -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
