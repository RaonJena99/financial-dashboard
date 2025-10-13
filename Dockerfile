# -------- Build stage --------
FROM gradle:8.10.1-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar -x test

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT:-8080}/actuator/health/readiness" || exit 1

EXPOSE 8080
CMD sh -c "java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT:-8080}"