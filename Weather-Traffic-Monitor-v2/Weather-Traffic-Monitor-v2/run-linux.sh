#!/bin/bash
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  echo "Java 21 is required. Install OpenJDK/Temurin 21 and run again."
  exit 1
fi
exec java -jar WeatherTrafficMonitor.jar
