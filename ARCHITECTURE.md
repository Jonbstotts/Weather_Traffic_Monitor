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

## Celebration and overlay subsystem (v2.3.0)

`CelebrationConfig` stores local recognition metadata. `MainShowcasePanel` checks the current local
date and generates `CelebrationSlidePanel` cards only when birthdays or hire-date anniversaries
match. It also rechecks date-driven content while the display remains running across midnight.

`OverlayEffectsPanel` is installed as the dashboard glass pane and renders lightweight, non-interactive
holiday/celebration particles. Severe-weather priority suppresses this layer immediately.

## Automatic holiday theme resolution (v2.4.0)

`HolidayThemeService` resolves the runtime `AppTheme` from the current local date when
`automaticHolidayThemes` is enabled. The configured `themeId` remains the manual fallback and is
never overwritten by automatic switching. `DashboardFrame` checks the effective theme during its
existing media/date refresh path so long-running installations can transition at a date boundary
without restarting.

`OverlayEffectsPanel` continues to be the non-interactive glass-pane rendering layer, but holiday
effects now use dedicated animation/painting logic rather than a shared particle presentation.
