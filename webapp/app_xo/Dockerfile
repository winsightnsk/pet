FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY src/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]