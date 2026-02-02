# ============================================================
# DOCKERFILE FOR JAVA WEB APPLICATION
# ============================================================
# This creates a container image from your WAR file
# Used by Harness CI to build deployable images
#
# BUILD COMMAND:
#   docker build -t my-webapp:latest .
#
# RUN COMMAND:
#   docker run -p 8080:8080 my-webapp:latest
# ============================================================

# ------------------------------------------------------------
# STAGE 1: Build (Multi-stage build)
# ------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first (for Docker layer caching)
# If pom.xml hasn't changed, Maven won't re-download dependencies
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B
EXPOSE 8088
EXPOSE 8080


# Copy source code
COPY src ./src

# Build the WAR file
RUN mvn package -DskipTests -B

# ------------------------------------------------------------
# STAGE 2: Runtime (smaller final image)
# ------------------------------------------------------------
FROM tomcat:10.1-jdk17-temurin-jammy

LABEL maintainer="your-team@example.com"
LABEL description="My Web Application"
LABEL version="1.0.0-SNAPSHOT"

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR from builder stage
COPY --from=builder /app/target/my-webapp.war /usr/local/tomcat/webapps/my-webapp.war
# Expose Tomcat port


# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/my-webapp/health || exit 1



# Start Tomcat
CMD ["catalina.sh", "run"]
