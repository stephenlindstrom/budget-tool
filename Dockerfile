# --------- Build Stage ---------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY backend ./backend
RUN ./mvnw -f backend/pom.xml clean package -DskipTests

# --------- Run Stage ---------
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]