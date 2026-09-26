# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp package -DskipTests

FROM eclipse-temurin:25-jre-jammy AS java

FROM gvenzl/oracle-free:23.26.3-slim
USER root
RUN microdnf install -y dnf \
    && dnf install -y curl fontconfig freetype libstdc++ tigervnc-server-minimal tigervnc python3.11 python3.11-pip \
    && curl -fsSL https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm -o /tmp/chrome.rpm \
    && dnf install -y /tmp/chrome.rpm \
    && python3.11 -m pip install --no-cache-dir websockify==0.13.0 \
    && mkdir -p /opt/novnc \
    && curl -fsSL https://github.com/novnc/noVNC/archive/refs/tags/v1.6.0.tar.gz | tar -xz --strip-components=1 -C /opt/novnc \
    && rm /tmp/chrome.rpm && dnf clean all
COPY --from=java /opt/java/openjdk /opt/java/openjdk
COPY --from=build /build/target/home-0.0.1-SNAPSHOT.jar /opt/home/home.jar
COPY --chmod=755 run_docker/entrypoint.sh run_docker/healthcheck.sh run_docker/prepare-oracle.sh /opt/home/
RUN mkdir -p /opt/home/chrome-profile /opt/home/oracle-bootstrap /media /tcg \
    && cp "${ORACLE_BASE_CONFIG}/dbs/spfileFREE.ora" "${ORACLE_BASE_CONFIG}/dbs/orapwFREE" \
          "${ORACLE_BASE_HOME}/network/admin/listener.ora" "${ORACLE_BASE_HOME}/network/admin/tnsnames.ora" \
          "${ORACLE_BASE_HOME}/network/admin/sqlnet.ora" /opt/home/oracle-bootstrap/ \
    && ln "${ORACLE_BASE}/FREE.7z" /opt/home/oracle-bootstrap/FREE.7z \
    && chown -R oracle:oinstall /opt/home/chrome-profile /media /tcg
ENV JAVA_HOME=/opt/java/openjdk \
    DISPLAY=:99 \
    APP_USER=home \
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
    TCG_DECK_RESOURCE_PATTERN=file:/tcg/*.csv \
    CHROME_EXECUTABLE_PATH=/usr/bin/google-chrome \
    CHROME_USER_DATA_DIRECTORY=/opt/home/chrome-profile \
    CHROME_ARGUMENTS=--no-sandbox,--disable-dev-shm-usage,--password-store=basic,--no-first-run,--no-default-browser-check,--restore-last-session,--start-maximized \
    CHROME_DESKTOP_URL=http://localhost:6080/vnc.html?autoconnect=1\&resize=scale \
    HOME_AUTH_INITIALADMIN_USERNAME=admin
WORKDIR /opt/home
VOLUME ["/opt/oracle/oradata", "/opt/home/chrome-profile", "/media", "/tcg"]
EXPOSE 5050 6080
STOPSIGNAL SIGTERM
HEALTHCHECK --interval=30s --timeout=15s --start-period=10m --retries=3 CMD ["/opt/home/healthcheck.sh"]
ENTRYPOINT ["/opt/home/entrypoint.sh"]
