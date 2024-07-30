FROM openjdk:21-jdk
WORKDIR /app
COPY target/graphql-0.0.1-SNAPSHOT.jar /app/graphql.jar
EXPOSE 8185
EXPOSE 9190
ENTRYPOINT ["java", "-jar", "graphql.jar"]