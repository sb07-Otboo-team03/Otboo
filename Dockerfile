# Stage 1: Build Stage
FROM amazoncorretto:17-al2-jdk AS build
WORKDIR /app

# Gradle wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 멀티 모듈 소스 복사
COPY api api
COPY common common
COPY batch batch

# 라이브러리 다운로드
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# API 모듈만 빌드
RUN ./gradlew :api:bootJar -x test --no-daemon

# Stage 2: Runtime Stage
FROM amazoncorretto:17-al2-jdk AS runtime
WORKDIR /app

# 4. 파일명 복사 (와일드카드 사용 권장)
# 변수(${PROJECT_NAME}) 주입이 번거롭다면 아래처럼 와일드카드를 쓰는 것이 가장 확실합니다.
# api 모듈의 bootJar만 복사
COPY --from=build /app/api/build/libs/*.jar app.jar

EXPOSE 80
ENV JVM_OPTS="-Xms512m -Xmx512m"

# 5. 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar --spring.profiles.active=prod"]