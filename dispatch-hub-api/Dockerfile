# Multi-stage build: reviewers run `docker compose up --build` with no local JDK.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src src
# No -q: first build downloads a lot; quiet mode looks "stuck" in BuildKit.
RUN ./mvnw -B -DskipTests package \
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
