#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_PROPERTIES="$ROOT_DIR/local.properties"

if [[ -z "${ANDROID_HOME:-}" ]]; then
  if [[ -f "$LOCAL_PROPERTIES" ]]; then
    ANDROID_HOME="$(sed -n 's/^sdk\.dir=//p' "$LOCAL_PROPERTIES" | tail -1)"
  else
    ANDROID_HOME="$HOME/android-sdk"
  fi
fi

if [[ -z "$ANDROID_HOME" || ! -d "$ANDROID_HOME" ]]; then
  echo "Android SDK not found. Set ANDROID_HOME or add sdk.dir to local.properties."
  exit 1
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb not found at $ANDROID_HOME/platform-tools."
  exit 1
fi

cd "$ROOT_DIR"

echo "Using Android SDK: $ANDROID_HOME"
adb devices

./gradlew app:installDebug

adb shell am start -n com.jtripppiie.mooserush/.MainActivity
