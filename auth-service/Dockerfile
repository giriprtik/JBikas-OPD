# Stage 1: Build the Java source into binary artifacts using multi-arch Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all project definition and source tree assets at once
COPY pom.xml .
COPY src ./src

# Download dependencies and package production JAR artifact cleanly in one step
RUN mvn clean package -DskipTests

# Stage 2: Deploy lightweight JRE runtime wrapper (Apple Silicon compatible)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
