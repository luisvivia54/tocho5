

# ===== Build =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

# ===== Run =====
FROM eclipse-temurin:17-jre
WORKDIR /app
# copia el jar construido
COPY --from=build /app/target/*.jar app.jar
# memoria modesta para plan free
ENV JAVA_OPTS="-Xms256m -Xmx512m"
# Render te pasa $PORT; exponlo al arranque
CMD ["sh","-c","java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
