FROM openjdk:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "app.jar"]