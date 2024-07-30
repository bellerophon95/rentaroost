FROM openjdk:21-jdk
WORKDIR /app
COPY build/libs/dynamic-pricing-service-0.0.1-SNAPSHOT.jar /app/dynamic-pricing-service.jar
EXPOSE 8281
ENTRYPOINT ["java", "-jar", "dynamic-pricing-service.jar"]