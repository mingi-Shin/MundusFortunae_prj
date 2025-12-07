FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8

CMD ["java", "-jar", "app.jar"]
