#!/bin/bash
set -Eeuo pipefail
/opt/oracle/healthcheck.sh >/dev/null
curl --fail --silent --max-time 5 "http://localhost:${SERVER_PORT:-5050}/login" >/dev/null
curl --fail --silent --max-time 5 http://localhost:6080/vnc.html >/dev/null
pgrep -x Xvnc >/dev/null
