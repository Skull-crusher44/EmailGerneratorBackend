FROM openjdk:17-jdk-slim AS build
VOLUME /tmp
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build target/Email-Response-Generator-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]