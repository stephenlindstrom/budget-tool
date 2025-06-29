# --------- Build Stage ---------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend backend
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -f backend/pom.xml clean package -DskipTests

# --------- Run Stage ---------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
