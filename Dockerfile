# syntax=docker/dockerfile:1

FROM eclipse-temurin:21 AS builder

# create a cache
ENV HOME="/build" MAVEN_USER_HOME="/build/.m2" MAVEN_ARGS="-Dmaven.repo.local=/var/lib/maven"
WORKDIR /build
COPY pom.xml mvnw /build/
COPY .mvn /build/.mvn
ARG APO_WEBSERVICES_CLIENT_VERSION=6.50.73
ARG APO_WEBSERVICES_CLIENT_URL="https://github.com/EsupPortail/esup-siscol/raw/refs/tags/2.1.2/jars/apo-webservices-client-65073.jar"
ADD "$APO_WEBSERVICES_CLIENT_URL" /tmp/apo-webservices-client.jar
RUN mkdir -p /tmp/META-INF/maven \
    && (cd /tmp ; jar xf /tmp/apo-webservices-client.jar META-INF/maven/) \
    && awk '/<parent>/,/<\/parent>/ {next}1' /tmp/META-INF/maven/gouv.education.apogee/apo-webservices-client/pom.xml > /tmp/META-INF/maven/orphan-pom.xml \
    && ./mvnw install:install-file --batch-mode \
      -Dfile=/tmp/apo-webservices-client.jar \
      -DpomFile=/tmp/META-INF/maven/orphan-pom.xml \
      -Dversion="$APO_WEBSERVICES_CLIENT_VERSION" \
      -DgeneratePom=false \
    && rm -rf /tmp/apo-webservices-client.jar /tmp/META-INF \
    && ./mvnw clean dependency:resolve dependency:resolve-plugins --fail-never

# package
COPY . /build/
RUN ./mvnw package -Dmaven.test.skip \
    && mkdir -p /app \
    && mv -v "target/$(./mvnw help:evaluate -q -DforceStdout -Dexpression=project.build.finalName).war" /app/apo-webservices-stub-server.war \
    && ./mvnw clean

# final image
FROM eclipse-temurin:21

# DevSecOps: don't run as root
RUN addgroup --system --gid 123 app \
    && useradd --system --no-log-init \
    --shell /usr/sbin/nologin \
    --no-create-home --home-dir /app \
    --gid 123 --uid 1234 \
    app \
    && mkdir -p /app /config \
    && echo 'dataset: { files: file:/srv/**/*.yml }' > /config/application-default.yml

COPY --from=builder /app /app
COPY src/test/resources/testdata/personas.yml /srv/personas.yml

USER 1234:123
WORKDIR /
EXPOSE 8080/tcp
EXPOSE 389/tcp

HEALTHCHECK \
    --interval=30s \
    --timeout=3s \
    --retries=1 \
    CMD curl -sfL http://localhost:8080/actuator/health/ || exit 1

ENTRYPOINT [ "java", "-jar", "/app/apo-webservices-stub-server.war" ]
