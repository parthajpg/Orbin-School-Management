# =====================================================================
# Orbin School — Backend Production Multi-Stage Dockerfile
# =====================================================================

# Stage 1: Build JAR with Maven & OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace/app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production JRE Alpine Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add unprivileged user for container security
RUN addgroup -S orbin && adduser -S orbin -G orbin
USER orbin:orbin

# Copy built artifact from builder stage
COPY --from=builder --chown=orbin:orbin /workspace/app/target/school-*.jar app.jar

# Expose default HTTP port
EXPOSE 8080

# Production JVM optimizations
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
