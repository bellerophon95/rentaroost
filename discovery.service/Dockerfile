FROM openjdk:21-jdk
WORKDIR /app
COPY target/discovery-service-0.0.1-SNAPSHOT.jar /app/discovery-service.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]
