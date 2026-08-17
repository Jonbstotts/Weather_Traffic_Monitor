#!/bin/bash
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir out
javac --release 21 -encoding UTF-8 -d out $(find src -name '*.java')
jar --create --file WeatherTrafficMonitor.jar --manifest MANIFEST.MF -C out .
echo "Built WeatherTrafficMonitor.jar"
