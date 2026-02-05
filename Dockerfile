# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory in container
WORKDIR /app

# Copy the Spring Boot jar to container
COPY target/InventoryManagementSystem-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.forward-headers-strategy=framework", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

#ENTRYPOINT ["java", "-jar", "app.jar"]