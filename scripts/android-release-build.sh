#!/bin/bash
set -euo pipefail

item="$(rbw get "${ANDROID_SIGNING_ITEM:-ANDROID_APP_SIGNING}" --raw)"

field() {
  jq -er --arg name "$1" '.fields // [] | .[] | select(.name == $name) | .value' <<<"$item"
}

tmp="${XDG_RUNTIME_DIR:-/tmp}/android-signing-$$"
mkdir -p "$tmp"
trap 'rm -rf "$tmp"' EXIT

ks="$tmp/release.p12"

field KEYSTORE_B64 | base64 -d > "$ks"
chmod 600 "$ks"

alias="$(jq -r '.data.username // empty' <<<"$item")"
[ -n "$alias" ] || alias="release"

pass="$(jq -r '.data.password' <<<"$item")"

keypass="$(field KEY_PASSWORD 2>/dev/null || true)"
[ -n "$keypass" ] || keypass="$pass"

storetype="$(field KEYSTORE_TYPE 2>/dev/null || true)"
[ -n "$storetype" ] || storetype="PKCS12"

ANDROID_KEYSTORE_FILE="$ks" \
ANDROID_KEYSTORE_PASSWORD="$pass" \
ANDROID_KEY_ALIAS="$alias" \
ANDROID_KEY_PASSWORD="$keypass" \
ANDROID_KEYSTORE_TYPE="$storetype" \
./gradlew assembleRelease
