# Build stage
FROM amazoncorretto:25-al2023-jdk AS builder
RUN yum update -y && yum install -y findutils
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Final stage
FROM amazoncorretto:25-al2023-headless
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]