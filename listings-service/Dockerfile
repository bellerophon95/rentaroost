FROM openjdk:21-jdk
WORKDIR /app
COPY target/listings-service-0.0.1-SNAPSHOT.jar /app/listings-service.jar
EXPOSE 8085
EXPOSE 9199
ENTRYPOINT ["java", "-jar", "listings-service.jar"]
