#Build stage
FROM eclipse-temurin:17-jdk-jammy AS build
ENV HOME=/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml clean package -DskipTests -Pproduction

FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=/app/target/*.jar
COPY --from=build ${JAR_FILE} app.jar
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]

