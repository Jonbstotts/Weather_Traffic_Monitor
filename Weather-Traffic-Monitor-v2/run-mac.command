#!/bin/bash
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  osascript -e 'display dialog "Java 21 is required. Install Temurin/OpenJDK 21, then run this file again." buttons {"OK"} default button "OK"'
  exit 1
fi
java -version 2>&1 | head -n 1
echo "Starting Weather & Traffic Monitor..."
exec java -jar WeatherTrafficMonitor.jar
