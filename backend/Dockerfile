# Stage 1: Build the Java application using standard multi-arch Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package the JAR file (skipping tests during image creation)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Universal multi-arch JRE runtime container (No Alpine)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the compiled JAR file from Stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
