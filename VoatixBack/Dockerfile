FROM eclipse-temurin:21-jre-jammy

WORKDIR /opt/app
EXPOSE 8080

COPY build/libs/Voatix.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]