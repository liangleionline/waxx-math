#!/usr/bin/env bash
# waxx-math 手动构建脚本（无需 Gradle）
# 依赖：JDK + Android SDK commandline-tools / platforms;android-33 / build-tools;34.0.0
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP="$ROOT/app"
BUILD="$ROOT/build"
GEN="$BUILD/gen"
CLASSES="$BUILD/classes"
DEX="$BUILD/dex"

SDK_ROOT="${ANDROID_HOME:-$HOME/Android/Sdk}"
BT="$SDK_ROOT/build-tools/34.0.0"
ANDROID_JAR="$SDK_ROOT/platforms/android-33/android.jar"

AAPT="$BT/aapt"
D8="$BT/d8"
ZIPALIGN="$BT/zipalign"
APKSIGNER="$BT/apksigner"

KEYSTORE="$BUILD/keystore.jks"
KS_PASS="waxx2026"
ALIAS="waxx"

echo "==> clean"
rm -rf "$BUILD"; mkdir -p "$GEN" "$CLASSES" "$DEX"

echo "==> aapt: package resources"
"$AAPT" package -f -m \
  -J "$GEN" \
  -S "$APP/src/main/res" \
  -M "$APP/src/main/AndroidManifest.xml" \
  -I "$ANDROID_JAR" \
  -F "$BUILD/app.unsigned.apk"

echo "==> javac"
SRCS=$(find "$GEN" "$APP/src/main/java" -name '*.java')
javac -source 8 -target 8 \
  -classpath "$ANDROID_JAR" \
  -d "$CLASSES" \
  $SRCS 2>/dev/null || {
    # 回退：显式编译便于查看错误
    javac -source 8 -target 8 -classpath "$ANDROID_JAR" -d "$CLASSES" $SRCS
    exit 1
  }

echo "==> d8"
"$D8" --lib "$ANDROID_JAR" --min-api 23 \
  --output "$DEX" $(find "$CLASSES" -name '*.class')

echo "==> add classes.dex"
cp "$DEX/classes.dex" "$BUILD/classes.dex"
"$AAPT" add "$BUILD/app.unsigned.apk" "$BUILD/classes.dex"

echo "==> zipalign"
"$ZIPALIGN" -f 4 "$BUILD/app.unsigned.apk" "$BUILD/app.aligned.apk"

echo "==> keytool: generate release keystore"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -v \
    -keystore "$KEYSTORE" -alias "$ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$KS_PASS" -keypass "$KS_PASS" \
    -dname "CN=Waxx Math, OU=Dev, O=Lianglei, L=Beijing, ST=Beijing, C=CN" >/dev/null 2>&1
fi

echo "==> apksigner"
"$APKSIGNER" sign \
  --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" \
  --out "$ROOT/waxx-math-release.apk" \
  "$BUILD/app.aligned.apk"

echo "==> verify"
"$APKSIGNER" verify --print-certs "$ROOT/waxx-math-release.apk" | head -5

echo "==> DONE: $ROOT/waxx-math-release.apk"
ls -la "$ROOT/waxx-math-release.apk"
