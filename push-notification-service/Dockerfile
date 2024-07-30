FROM openjdk:21-jdk
WORKDIR /app
COPY target/app-0.0.1-SNAPSHOT.jar /app/app.jar
COPY /path/to/local/google/push-notifications-example-firebase-adminsdk.json /google/push-notifications-example-firebase-adminsdk.json
EXPOSE 6700
ENTRYPOINT ["java", "-jar", "app.jar"]