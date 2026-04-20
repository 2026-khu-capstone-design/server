FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
## 컨테이너 안에서 작업할 폴더 설정 모두 /app
COPY build/libs/*.jar app.jar
# Actions 서버의 build/libs/*.jar 파일을
# 컨테이너 안의 /app/app.jar 로 복사
# gradle bootJar로 빌드한 jar 파일을 컨테이너 안에 넣는 것
ENTRYPOINT ["java", "-jar", "app.jar"]
# 컨테이너 실행될 때 자동으로 실행할 명령어
# = java -jar app.jar
# 우리가 서버에서 직접 java -jar 치던 것을 자동화