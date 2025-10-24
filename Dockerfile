# ---- Build stage ----
FROM gradle:8.10-jdk17-alpine AS builder
WORKDIR /workspace

# Gradle 캐시 최적화 (선택) — 없다면 그대로 넘어가도 됩니다.
COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 bootJar 빌드(테스트 제외)
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre-alpine AS runtime
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
WORKDIR /app
RUN adduser -D appuser \
 && mkdir -p /app/logs \
 && chown -R appuser:appuser /app
USER appuser

# Spring Boot fat-jar 경로 (단일 모듈 기준)
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# Actuator 헬스체크 사용 시 유효
HEALTHCHECK --interval=30s --timeout=3s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]