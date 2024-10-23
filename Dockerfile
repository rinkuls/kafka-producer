#################
# Build the  JAVA APP #

#################FROM node:18.16.0-alpine as build
#################WORKDIR /app

FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD target/kafka-producer-0.0.1-SNAPSHOT.jar kafka-producer.jar
ENV JAVA_OPTS=""
EXPOSE 8165
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /kafka-producer.jar" ]