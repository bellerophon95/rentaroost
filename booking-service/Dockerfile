FROM openjdk:21-jdk
WORKDIR /app
COPY target/booking-service-0.0.1-SNAPSHOT.jar /app/booking-service.jar
EXPOSE 8380
ENTRYPOINT ["java", "-jar", "/app/booking-service.jar"]
