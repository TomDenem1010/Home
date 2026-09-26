#!/bin/bash
set -Eeuo pipefail
: "${APP_USER:?APP_USER is required}"
: "${APP_USER_PASSWORD:?APP_USER_PASSWORD is required}"
: "${HOME_AUTH_INITIALADMIN_PASSWORD:?HOME_AUTH_INITIALADMIN_PASSWORD is required}"
: "${VNC_PASSWORD:?VNC_PASSWORD is required (exactly 8 characters)}"
if ((${#VNC_PASSWORD} != 8)); then
    echo "VNC_PASSWORD must contain exactly 8 characters." >&2
    exit 1
fi
if [[ "$(id -u)" == 0 ]]; then
    /opt/home/prepare-oracle.sh
    # setpriv changes credentials but preserves root's HOME by default.
    export HOME="$(getent passwd oracle | cut -d: -f6)"
    export USER=oracle LOGNAME=oracle
    exec setpriv --reuid=oracle --regid=oinstall --init-groups "$0" "$@"
fi
if [[ ! -d "${ORACLE_BASE}/oradata/dbconfig/${ORACLE_SID}" ]]; then
    : "${ORACLE_PASSWORD:?ORACLE_PASSWORD is required on first startup}"
fi
export DB_URL="jdbc:oracle:thin:@//localhost:1521/FREEPDB1"
export DB_USERNAME="${APP_USER}"
export DB_PASSWORD="${APP_USER_PASSWORD}"
database_pid=""
application_pid=""
desktop_pid=""
viewer_pid=""
# Invoked indirectly by the EXIT trap.
# shellcheck disable=SC2329
shutdown() {
    local status=$?
    trap - EXIT INT TERM
    if [[ -n "${application_pid}" ]]; then
        kill -TERM "${application_pid}" 2>/dev/null || true
        wait "${application_pid}" 2>/dev/null || true
    fi
    # Flush cookies and profile data before shutting down the display.
    pkill -TERM -u "$(id -u)" -o -x chrome 2>/dev/null || true
    for ((attempt=0; attempt<20; attempt++)); do
        if ! pgrep -u "$(id -u)" -x chrome >/dev/null; then break; fi
        sleep 0.5
    done
    for pid in "${viewer_pid}" "${desktop_pid}" "${database_pid}"; do
        if [[ -n "${pid}" ]]; then
            kill -TERM "${pid}" 2>/dev/null || true
            wait "${pid}" 2>/dev/null || true
        fi
    done
    exit "${status}"
}
trap shutdown EXIT
trap 'exit 0' INT TERM

umask 077
printf '%s\n' "${VNC_PASSWORD}" | vncpasswd -f > /tmp/home-vnc-password
unset VNC_PASSWORD
Xvnc :99 -geometry 1400x900 -depth 24 -localhost yes -SecurityTypes VncAuth -PasswordFile /tmp/home-vnc-password &
desktop_pid=$!
websockify --web /opt/novnc 6080 localhost:5999 &
viewer_pid=$!

echo "Starting Oracle before the application..."
container-entrypoint.sh &
database_pid=$!
deadline=$((SECONDS + ${ORACLE_STARTUP_TIMEOUT_SECONDS:-600}))
until /opt/oracle/healthcheck.sh >/dev/null 2>&1; do
    for pid in "${database_pid}" "${desktop_pid}" "${viewer_pid}"; do
        if ! kill -0 "${pid}" 2>/dev/null; then
            echo "A required service exited during startup." >&2
            exit 1
        fi
    done
    if ((SECONDS >= deadline)); then
        echo "Oracle startup timed out." >&2
        exit 1
    fi
    sleep 2 &
    wait $!
done
echo "Oracle is ready; starting Home (Flyway applies the schema migrations)."
"${JAVA_HOME}/bin/java" -jar /opt/home/home.jar "$@" &
application_pid=$!
# Any required service exiting stops the container and shuts down its peers.
set +e
wait -n "${database_pid}" "${application_pid}" "${desktop_pid}" "${viewer_pid}"
status=$?
set -e
if ((status == 0)); then status=1; fi
exit "${status}"
