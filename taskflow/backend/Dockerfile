# ======================
# BUILD STAGE
# ======================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Dependency cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Source
COPY src ./src

# Build
RUN mvn clean package -DskipTests

# ======================
# RUNTIME STAGE
# ======================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","app.jar"]
