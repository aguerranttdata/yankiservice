FROM maven:3.8.5-openjdk-8-slim
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn package -DskipTests
RUN ls
EXPOSE 8083
ENTRYPOINT ["java","-jar","target/yankiservice-0.0.1-SNAPSHOT.jar"]
