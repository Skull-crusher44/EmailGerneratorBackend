<<<<<<< HEAD
FROM openjdk:17-jdk-slim AS build
VOLUME /tmp
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build target/Email-Response-Generator-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
=======
FROM openjdk:19-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

FROM openjdk:19-jdk
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
>>>>>>> a1aa71f2d179612ef5eb8487ea2774e0327a0f57
