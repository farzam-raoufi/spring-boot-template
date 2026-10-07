#   Stage 1 (build)   : Maven + JDK -> builds the fat jar and splits it into layers
#   Stage 2 (runtime) : JRE only    -> small image, non-root user, healthcheck

ARG JAVA_VERSION=21


# ============================== Stage 1: build ==============================
FROM maven:3.9-eclipse-temurin-${JAVA_VERSION} AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline || true

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests \
    && cp target/*.jar application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted


# ============================= Stage 2: runtime =============================
FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime

# curl is only needed by HEALTHCHECK; create an unprivileged user.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 app \
    && useradd  --system --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /application

# Least-changing layers first -> best cache reuse.
COPY --from=build --chown=app:app /workspace/extracted/dependencies/          ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/    ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/           ./

USER app:app

# Container-aware JVM settings; override at runtime with -e JAVA_TOOL_OPTIONS=...
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# Requires spring-boot-starter-actuator (already configured in application.properties).
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# Exec form: java runs as PID 1 and receives SIGTERM -> graceful shutdown.
ENTRYPOINT ["java", "-jar", "application.jar"]