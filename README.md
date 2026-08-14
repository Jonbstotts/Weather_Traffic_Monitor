# Weather & Traffic Monitor 1.0

A fullscreen workplace dashboard for weather, radar, National Weather Service alerts, commute route times, live traffic-flow overlays, and rotating facility announcements.

## First run

### macOS
1. Make sure Java 21 is installed.
2. Double-click `run-mac.command` (you may need to allow it in System Settings > Privacy & Security the first time).
3. Or open Terminal in this folder and run: `java -jar WeatherTrafficMonitor.jar`

### Linux / Raspberry Pi / ThinkPad
1. Install Java 21 (`openjdk-21-jre` or Temurin 21).
2. Run: `./run-linux.sh`

### Windows
1. Install Java 21.
2. Double-click `run-windows.bat`.

No IDE is required to run the included JAR.

## Default site
- Primary location: Vance, Alabama
- Forecast strip: Tuscaloosa, Vance, Birmingham
- Routes: Tuscaloosa, Birmingham, Trussville
- Default theme: dark

## Data sources
- Open-Meteo: current, hourly and daily forecast data. No API key required.
- National Weather Service: active U.S. alerts and alert polygons. No API key required.
- RainViewer: radar tile timeline. No API key required.
- TomTom: live traffic-flow tiles and route travel times. A TomTom API key is required.

## Enabling live traffic
1. Create a TomTom developer account and API key.
2. Open the dashboard Settings (gear icon).
3. Open `Data & APIs`.
4. Paste the key into `TomTom API key`.
5. Save & Apply.

Without a key the application still runs normally; traffic cards explain that traffic is not configured and the weather/radar portions continue operating.

## Settings and local files
The application stores configuration, cache, and the default media folder at:

`~/.weather-traffic-monitor/`

Important files:
- `config.properties` - site settings
- `cache/` - cached map and radar tiles
- `media/` - PNG/JPG/JPEG/GIF announcements for a Media block

The application can only be exited from Settings by design. F11 toggles maximized display mode.

## Dashboard blocks
Six side-panel positions can independently display:
- Primary current weather
- Route 1 / 2 / 3 travel status
- NWS alerts
- Primary hourly outlook
- Wind and gusts
- Media announcements
- System/data status

## Building from source
No external Java libraries are required.

```bash
./build.sh
```

This compiles the complete source tree with Java 21 and rebuilds `WeatherTrafficMonitor.jar`.

## Notes for continuous display use
- Weather, radar, alerts and routes refresh on independent schedules.
- Base/radar map tiles are cached locally.
- TomTom traffic tiles are kept in memory temporarily rather than persisted because the traffic service marks them as non-cacheable.
- If an API fails, the dashboard retains the last successful data instead of clearing the display.
- Map attribution is shown directly on the map.

## UI refresh in v1.1.0
Version 1.1.0 introduces a unified operations-dashboard design with equal spacing,
rounded outlined surfaces, scalable vector weather/route icons, improved typography,
and matching light/dark themes. All icons are drawn by Java at runtime, so no external
image assets or icon libraries are required.

## Dynamic site customization in v1.2.0
Pinned locations and commute routes are no longer limited to three entries. Use Settings >
Pinned Locations to add map pins, then Settings > Routes to create any number of commute
routes. Dashboard Blocks can display current weather for individual pinned locations or
travel time for any configured route.

Large displays can use 6, 8, 10, or 12 information cards. Ten and twelve cards use a
three-column grid beside the map, while smaller configurations use two columns.

## Layout lock in v1.2.1
The main map and information grid now use a fixed responsive proportion instead of a
draggable split pane. This prevents accidental dashboard resizing on a shared TV while
still allowing the entire interface to scale automatically with the connected display.

## Fixed dashboard proportions in v1.2.2
The map and information-card areas now use a deterministic 63/37 layout. The application
can still scale to different monitor resolutions, but internal component sizes are no longer
allowed to change that proportion after startup.

## Controlled dashboard resizing in v1.3.0
Open Settings > Dashboard Blocks to choose how much horizontal space belongs to the map.
The slider supports 55% through 75% map width and provides 55/45, 63/37, and 70/30
presets. The setting is persistent and remains locked during normal dashboard operation.

## On-demand refresh controls in v1.3.1
Settings > Data & APIs now contains independent refresh controls for routes, weather,
radar, and NWS alerts. Save & Apply reschedules the running background jobs immediately.

For a production workday display, 10 minutes is a conservative default for route travel
times. Shorter intervals can be selected temporarily when more frequent commute updates
are useful.

## Live Severe Weather Mode in v1.3.2
Settings > Data & APIs includes a Live Severe Weather Mode for periods when rapidly
changing conditions need closer monitoring. While enabled, NWS alerts are checked every
minute and both radar and current weather are refreshed every two minutes. Traffic and
routing remain on their normal configured interval, so severe-weather monitoring does not
consume additional TomTom routing requests.

This is high-frequency polling rather than a streaming socket; the upstream weather
services publish discrete updates.

## Automatic Severe Weather Mode in v1.4.0
Settings > Data & APIs now includes three independent severe-weather controls:

1. Manual Live Severe Weather Mode — force rapid polling manually.
2. Automatically enable Live Severe Weather Mode — let qualifying NWS alerts activate rapid polling.
3. Automatically return to normal refresh rates — restore normal intervals after qualifying alerts clear.

Auto Return is enabled by default. System Status indicates NORMAL, MANUAL LIVE, or AUTO LIVE.

## Main Showcase in v1.5.0
Settings > Main Showcase controls the large left-side content region.

It can remain a permanent live map or cycle between the map and announcement images stored
in the configured media directory. The interval is configurable. Severe Weather Map Priority
is enabled by default; when Automatic Severe Weather Mode enters AUTO LIVE, the live map
takes over immediately and stays persistent until the qualifying NWS alert clears.

The map-priority override can be disabled for troubleshooting or deliberate media-cycle tests.

## Severe-weather ticker priority in v1.5.1
Main Showcase announcement images no longer display their filenames.

When Automatic Severe Weather Mode enters AUTO LIVE, the bottom ticker temporarily switches
from the site's normal announcement text to a severe-weather status message. The triggering
NWS event is included when available. When AUTO LIVE clears, the configured site ticker
returns automatically. Manual live mode has its own distinct ticker message.
