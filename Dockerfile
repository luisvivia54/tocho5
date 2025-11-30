# ---- STAGE 1: build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache de dependencias
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Código y build
COPY src ./src
RUN mvn -B -DskipTests -Dmaven.resources.skip=true package

# ---- STAGE 2: runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# copia el JAR construido
COPY --from=build /build/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
