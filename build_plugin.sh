#!/usr/bin/env bash

echo "start building plugin apk..."

./gradlew :plugin:assembleDebug
cp plugin/build/outputs/apk/debug/plugin-debug.apk app/src/main/assets/

echo "start building host apk..."

./gradlew :app:installDebug
adb shell am start -n com.sleticalboy.pluginization/.MainActivity