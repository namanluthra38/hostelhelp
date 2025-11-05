FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn -DskipTests=true clean package

FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser

COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar ./app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 4005

# Add JVM options for better performance
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]