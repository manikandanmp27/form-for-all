# Multi-stage Dockerfile for Spring Boot Backend deployment on Render / Railway
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven
COPY . .
RUN if [ -d "backend" ]; then cd backend; fi && \
    mvn clean package -DskipTests && \
    cp target/*.jar /app/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
