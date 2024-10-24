#################
# Build the JAVA APP #
#################

FROM openjdk:17-jdk-alpine
VOLUME /tmp

# Copy the JAR file to the image
ADD target/kafka-producer-0.0.1-SNAPSHOT.jar kafka-producer.jar

# Expose the port that your Spring Boot application uses
EXPOSE 8165

# Set the default JAVA_OPTS to allow for custom Java options during runtime
ENV JAVA_OPTS=""

# Entry point to run the jar file
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /kafka-producer.jar"]
