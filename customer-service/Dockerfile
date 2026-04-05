FROM gradle:8.14.3-jdk17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle ./gradle
COPY src ./src

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]