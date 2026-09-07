# syntax=docker/dockerfile:1

# ---- Build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Dependency layer — cached until pom.xml changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

# App
COPY src/ src/
RUN ./mvnw -B -ntp clean package -DskipTests \
 && cp target/gym-api-*.jar app.jar

# ---- Runtime ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /app/app.jar app.jar
# stock temurin runs as root; drop to a non-root uid (k8s runAsNonRoot)
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
