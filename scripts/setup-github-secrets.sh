#!/bin/bash
set -euo pipefail

item="$(rbw get "${ANDROID_SIGNING_ITEM:-ANDROID_APP_SIGNING}" --raw)"

field() {
  jq -er --arg name "$1" '.fields // [] | .[] | select(.name == $name) | .value' <<<"$item"
}

alias="$(jq -r '.data.username // empty' <<<"$item")"
[ -n "$alias" ] || alias="release"

pass="$(jq -r '.data.password' <<<"$item")"

keypass="$(field KEY_PASSWORD 2>/dev/null || true)"
[ -n "$keypass" ] || keypass="$pass"

storetype="$(field KEYSTORE_TYPE 2>/dev/null || true)"
[ -n "$storetype" ] || storetype="PKCS12"

field KEYSTORE_B64 | gh secret set ANDROID_RELEASE_KEYSTORE_B64
printf '%s' "$pass" | gh secret set ANDROID_RELEASE_KEYSTORE_PASSWORD
printf '%s' "$alias" | gh secret set ANDROID_RELEASE_KEY_ALIAS
printf '%s' "$keypass" | gh secret set ANDROID_RELEASE_KEY_PASSWORD
printf '%s' "$storetype" | gh secret set ANDROID_RELEASE_KEYSTORE_TYPE
