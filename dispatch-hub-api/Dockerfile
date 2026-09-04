# Multi-stage build for reviewers / full Compose.
# BuildKit cache: DOCKER_BUILDKIT=1 (default on Docker Desktop).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src src
# Avoid -T parallel package (can race spring-boot repackage). Validate ZIP before promoting.
RUN --mount=type=cache,target=/root/.m2 \
	./mvnw -B -DskipTests package \
	&& test -f target/dispatch-hub-api-0.0.1-SNAPSHOT.jar \
	&& jar tf target/dispatch-hub-api-0.0.1-SNAPSHOT.jar >/dev/null \
	&& cp target/dispatch-hub-api-0.0.1-SNAPSHOT.jar /workspace/app.jar \
	&& test -s /workspace/app.jar \
	&& jar tf /workspace/app.jar >/dev/null

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl unzip \
	&& rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app.jar app.jar
RUN test -s app.jar && unzip -t app.jar >/dev/null
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
	CMD curl -fsS http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
