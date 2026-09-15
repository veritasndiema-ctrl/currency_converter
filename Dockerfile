FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/currency-converter.jar /app/currency-converter.jar

USER app
ENTRYPOINT ["java", "-jar", "/app/currency-converter.jar"]