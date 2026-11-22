#Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -B

#Extractor stage
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

#Prod stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=extractor --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/extracted/application/ ./

USER appuser

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]