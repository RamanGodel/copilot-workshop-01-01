#!/bin/sh
set -eu

# Optional TLS inspection / custom CA support.
# If enabled, the entrypoint will trust any PEM-encoded *.crt files mounted under:
#   /usr/local/share/ca-certificates/
# by updating OS CA store and importing into JVM cacerts.

CA_DIR="/usr/local/share/ca-certificates"
KEYSTORE="/etc/ssl/certs/java/cacerts"
PASS="changeit"
RUN_AS_USER="spring"

# Ensure tmp is writable (update-ca-certificates creates a temp bundle file)
export TMPDIR="${TMPDIR:-/tmp}"
mkdir -p "$TMPDIR" || true
chmod 1777 "$TMPDIR" || true

if [ "${CORPORATE_CA_ENABLED:-false}" = "true" ]; then
  echo "[entrypoint] CORPORATE_CA_ENABLED=true"

  # List mounted certs (debug)
  if [ -d "${CA_DIR}" ]; then
    echo "[entrypoint] Listing ${CA_DIR}/*.crt (if any)"
    ls -1 ${CA_DIR}/*.crt 2>/dev/null || echo "[entrypoint] No .crt files found in ${CA_DIR}"
  fi

  # 1) OS trust (needs root)
  update-ca-certificates

  # 2) JVM trust (needs root)
  if command -v keytool >/dev/null 2>&1 && [ -f "${KEYSTORE}" ]; then
    for crt in ${CA_DIR}/*.crt; do
      [ -f "$crt" ] || continue
      alias="custom-$(basename "$crt" | tr '.' '-' | tr '[:upper:]' '[:lower:]')"
      echo "[entrypoint] Importing $crt into JVM truststore (alias=$alias)"
      keytool -delete -alias "$alias" -keystore "$KEYSTORE" -storepass "$PASS" >/dev/null 2>&1 || true
      keytool -importcert -noprompt -trustcacerts -alias "$alias" -file "$crt" -keystore "$KEYSTORE" -storepass "$PASS" >/dev/null
    done
  else
    echo "[entrypoint] keytool or JVM truststore not available; skipping JVM import"
  fi
else
  echo "[entrypoint] CORPORATE_CA_ENABLED=false"
fi

# Drop privileges for the actual app process
if command -v su-exec >/dev/null 2>&1; then
  exec su-exec "$RUN_AS_USER" java -jar app.jar
fi

# Alpine default: use su
exec su -s /bin/sh -c "java -jar app.jar" "$RUN_AS_USER"
