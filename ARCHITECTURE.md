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

## Adaptive overlay renderer (v2.5.0)

`OverlayEffectsPanel` now maintains a rendering profile and an adaptive scale. In AUTOMATIC mode,
each paint records elapsed render time into an exponential moving average. Thresholds adjust both
ambient effect density and Swing Timer delay. Confetti and fireworks are priority effects; ambient
seasonal particles are the load-shedding tier.

Static frost edge gradients are cached by viewport size/intensity. Fog simulation can update less
frequently than the paint loop. The panel also avoids requesting idle repaints when no overlay
visual is active.

## Sports schedule subsystem (v2.6.0)

`TheSportsDbService.fetchUpcoming()` normalizes the provider's `eventsnext` response, filters out
finished/past events, sorts future events chronologically, and returns only the requested dashboard
limit. `DashboardFrame` stores a list of future `SportsGame` records per configured sports selection.

`SportsSchedulePanel` renders the first event prominently and up to two additional future events in
compact rows. Existing `SPORTS_<index>` widget identifiers are intentionally preserved for backward
configuration compatibility.

## Employee of the Month recognition (v2.7.0)

`CelebrationConfig` now stores an Employee of the Month year/month assignment in the same employee
record as birthday and anniversary preferences. `SettingsDialog` presents this assignment as a
single-select checkbox column and stamps the selected row with the current `YearMonth` during save.

`MainShowcasePanel` treats monthly recognition as a separate generated card from date-specific
birthday/anniversary cards. `EmployeeOfMonthSlidePanel` owns the dedicated trophy/photo layout.
The existing celebration callback and overlay system are reused for optional confetti.

## Operations Calendar subsystem (v2.8.0)

`OperationEvent` is the persistent site calendar record and supports Full Closure, Limited Service,
and Modified Hours. `AppConfig` stores the normal operating schedule, default announcement lead,
and event collection.

`OperationsCalendarService` sorts enabled events, groups overlapping/adjacent dates, determines
whether each group is inside its announcement window, and calculates the next normal operating
day while respecting normal weekdays and other operations events.

`OperationsAnnouncementSlidePanel` renders groups directly from calendar data. These cards are
ephemeral Main Showcase components; no media file is created or deleted. `MainShowcasePanel`
rebuilds date-driven content during its existing date-boundary refresh path, which makes notices
appear, update, and disappear automatically on long-running 24/7 installations.
