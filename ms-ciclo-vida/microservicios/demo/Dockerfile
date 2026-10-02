FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY ms-ciclo-vida/microservicios/demo/pom.xml .
COPY ms-ciclo-vida/microservicios/demo/src ./src
COPY contracts /contracts
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
