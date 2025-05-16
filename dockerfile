FROM eclipse-temurin:21-jdk as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
VOLUME /tmp
COPY --from=build /workspace/app/target/roadvault-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]