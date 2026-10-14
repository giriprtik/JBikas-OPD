# Stage 1: Build the Java source into binary artifacts using multi-arch Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies to optimize build execution speeds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy code and package the production JAR file
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal multi-arch JRE runtime container footprint
FROM eclipse-temurin:17-jre
WORKDIR /app

# Pull compiled application layers forward
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
