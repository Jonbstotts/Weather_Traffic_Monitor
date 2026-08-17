# Architecture

The code is separated by responsibility so site settings and API providers can be changed without rewriting the display.

- `com.wtm.app` - application entry point
- `com.wtm.config` - site configuration and persistence
- `com.wtm.model` - immutable data records
- `com.wtm.net` - centralized HTTP client
- `com.wtm.weather` - Open-Meteo provider
- `com.wtm.alerts` - NWS alert provider and GeoJSON parsing
- `com.wtm.radar` - RainViewer radar timeline provider
- `com.wtm.traffic` - TomTom traffic-aware routing provider
- `com.wtm.map` - cached Web Mercator tile renderer and overlays
- `com.wtm.ui` - dashboard, settings, theme and reusable surfaces
- `com.wtm.util` - dependency-free JSON and map-coordinate helpers

The UI never directly performs network calls. Scheduled worker threads call provider services and publish results back to Swing on the Event Dispatch Thread. This keeps the display responsive during slow or unavailable network conditions.

Site-specific locations, routes, title text, ticker text, visibility options and block assignments are loaded from `config.properties`; they are not hard-coded into dashboard rendering logic.
