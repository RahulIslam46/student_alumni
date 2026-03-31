#!/usr/bin/env zsh
set -euo pipefail

cd "$(dirname "$0")"

# Load optional local overrides.
if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

# Defaults for local dev if .env is missing.
: ${ALUMNAI_DB_URL:='jdbc:mysql://127.0.0.1:3306/alumnai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'}
: ${ALUMNAI_DB_USER:='alumnai_app'}
: ${ALUMNAI_DB_PASSWORD:='StrongPass123!'}

export ALUMNAI_DB_URL ALUMNAI_DB_USER ALUMNAI_DB_PASSWORD

javac -cp '.:lib/mysql-connector-j-9.5.0.jar' *.java
java -cp '.:lib/mysql-connector-j-9.5.0.jar' LoginFrame
