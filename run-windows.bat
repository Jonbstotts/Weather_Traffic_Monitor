@echo off
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Java 21 is required. Install Temurin/OpenJDK 21 and run this file again.
  pause
  exit /b 1
)
java -jar WeatherTrafficMonitor.jar
