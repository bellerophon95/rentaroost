FROM openjdk:21-jdk
WORKDIR /app
COPY target/payments-service-0.0.1-SNAPSHOT.jar /app/payments-service.jar
EXPOSE 4242
EXPOSE 9195
ENTRYPOINT ["java", "-jar", "payments-service.jar"]
