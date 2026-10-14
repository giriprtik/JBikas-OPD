# Stage 1: Compile and package the Java binary source
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies to optimize build execution speeds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy code and build production JAR artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy lightweight JRE runtime wrapper
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
