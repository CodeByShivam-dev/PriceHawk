# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project files
COPY . .

# Build application
RUN mvn clean package -DskipTests

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated jar
COPY --from=build /app/target/*.jar app.jar

# Render uses PORT environment variable
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]