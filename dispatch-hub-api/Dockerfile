# Multi-stage build for reviewers / full Compose.
# BuildKit cache: DOCKER_BUILDKIT=1 (default on Docker Desktop).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src src
# Single Maven invocation + persistent ~/.m2 (avoid a separate resolve that doubles download time).
RUN --mount=type=cache,target=/root/.m2 \
	./mvnw -B -T 1C -DskipTests package \
	&& JAR=$(ls target/dispatch-hub-api-*.jar | grep -v '\.original$' | head -n1) \
	&& cp "$JAR" /workspace/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
	CMD curl -fsS http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
