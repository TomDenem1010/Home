#!/bin/bash
set -Eeuo pipefail

data_directory="${ORACLE_BASE}/oradata"
config_directory="${data_directory}/dbconfig/${ORACLE_SID}"
bootstrap_directory=/opt/home/oracle-bootstrap
profile_directory="${CHROME_USER_DATA_DIRECTORY:-/opt/home/chrome-profile}"

mkdir -p "${data_directory}" "${profile_directory}"
chown oracle:oinstall "${data_directory}" "${profile_directory}"
chmod u+rwx "${data_directory}" "${profile_directory}"
if [[ -d "${data_directory}/dbconfig" ]]; then
    chown -R oracle:oinstall "${data_directory}/dbconfig"
fi
if ! setpriv --reuid=oracle --regid=oinstall --init-groups test -w "${data_directory}"; then
    echo "Oracle data mount is not writable. Mount /opt/oracle/oradata read-write." >&2
    exit 1
fi
install -d -m 1777 -o root -g root /tmp/.X11-unix

# Database files take precedence over initialization markers. Never replace them.
database_file=$(find "${data_directory}" -type f \( -name '*.dbf' -o -name '*.ctl' -o -name 'redo*.log' \) -print -quit)
if [[ -n "${database_file}" ]]; then
    if [[ ! -s "${config_directory}/spfile${ORACLE_SID}.ora" ]]; then
        echo "Existing Oracle database files have no readable parameter file in ${config_directory}. Restore the database configuration from backup; no database files were replaced." >&2
        exit 1
    fi
    echo "Existing Oracle database found; preserving its data and configuration."
    exit 0
fi

: "${ORACLE_PASSWORD:?ORACLE_PASSWORD is required to initialize an empty database}"
if [[ -e "${config_directory}" || -L "${config_directory}" ]]; then
    saved_directory="${config_directory}.incomplete-$(date +%s%N)"
    mv "${config_directory}" "${saved_directory}"
    echo "Saved incomplete Oracle configuration at ${saved_directory}."
fi

# The upstream entrypoint moves configuration files and removes FREE.7z on its
# first run. Restore pristine installation files when retrying an empty mount
# in the same container, rather than moving its old symlinks into the mount.
for file in "spfile${ORACLE_SID}.ora" "orapw${ORACLE_SID}"; do
    cp --remove-destination "${bootstrap_directory}/${file}" "${ORACLE_BASE_CONFIG}/dbs/${file}"
    chown oracle:oinstall "${ORACLE_BASE_CONFIG}/dbs/${file}"
done
for file in listener.ora tnsnames.ora sqlnet.ora; do
    cp --remove-destination "${bootstrap_directory}/${file}" "${ORACLE_BASE_HOME}/network/admin/${file}"
    chown oracle:oinstall "${ORACLE_BASE_HOME}/network/admin/${file}"
done
if [[ ! -f "${ORACLE_BASE}/${ORACLE_SID}.7z" ]]; then
    cp --reflink=auto "${bootstrap_directory}/${ORACLE_SID}.7z" "${ORACLE_BASE}/${ORACLE_SID}.7z"
    chown oracle:oinstall "${ORACLE_BASE}/${ORACLE_SID}.7z"
fi
echo "Empty Oracle data mount prepared for automatic initialization."
