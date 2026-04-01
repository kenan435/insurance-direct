FROM eclipse-temurin:21-jre-slim

WORKDIR /app

# Download the OpenTelemetry Java agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY build/libs/kotlin-service.jar /app/kotlin-service.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"

EXPOSE 8080

CMD ["java", "-jar", "/app/kotlin-service.jar"]
