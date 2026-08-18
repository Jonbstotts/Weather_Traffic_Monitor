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

## API provider architecture in v1.6.0
Settings > API Providers centralizes provider selection and credentials. Open-Meteo Free remains
the default and requires no key. If a commercial Open-Meteo account is added later, select
Open-Meteo Customer and paste its API key; the application switches to the customer endpoint
without a code edit. TomTom credentials are managed on the same screen.

NWS and RainViewer remain keyless in the current adapter set. NWS uses a configurable User-Agent.
A completely different future vendor still requires a small Java adapter because vendors use
different endpoints and JSON schemas, but the settings/credential framework is already in place.

## Sports Score blocks in v1.7.0
Use Settings > Sports to define teams that should be available to the dashboard. Each saved
sports selection becomes a Sports Score option in Settings > Dashboard Blocks.

The first provider adapter is TheSportsDB. The free v1 key can display team artwork, upcoming
games, and recent/final results. TheSportsDB currently reserves its dedicated livescore API
for premium subscribers; enable Premium live scores under API Providers only after entering a
premium key. The application is structured so another sports provider can be added later
without redesigning the dashboard-block or Sports-settings systems.

The fresh Vance configuration includes editable Alabama Football and Tennessee Football
examples (league 4479; team IDs 136168 and 136957). They are not forced onto the dashboard;
choose either under Dashboard Blocks when desired.

## Find Team in v1.8.0
Open Settings > Sports and select Find Team. Search by a recognizable team name, select the
correct team from the results, inspect its league/sport/country and logo, then choose Use This
Team. The application fills the provider IDs and descriptive fields automatically.

The workflow is sport-independent and can be used for football, basketball, baseball, hockey,
soccer, and other team sports supported by the configured provider.

TheSportsDB currently provides unrestricted general text team search through its premium v2
API. Its free v1 team-search method is provider-restricted, so free-key users may still need
manual IDs for some teams until another searchable provider is added.

## Find Location in v1.9.0
Settings > Pinned Locations now includes Find Location and Find Primary Location. Settings >
Routes includes Find Destination. Search by city/place name, choose the appropriate result, and
the application fills WGS84 latitude/longitude automatically.

The current search provider is Open-Meteo's keyless Geocoding API backed by GeoNames. It works
best for cities, towns, municipalities, and named places. Manual coordinates remain available
when a warehouse or route needs an exact point that the place database does not contain.

## Application themes in v2.0.0
Settings > General now includes a full Theme selector instead of only a Dark Mode checkbox.

Built-in presets:
- Dark
- Light
- Graphite / Silver
- Operations Blue
- Midnight Blue
- Slate
- Emerald
- Amber / Night
- High Contrast
- Warm Neutral

Each preset keeps the same dashboard layout and safety semantics while changing the surrounding
visual palette. The map automatically selects a compatible light/dark presentation.

## API Usage monitoring in v2.1.0
Settings > API Usage shows locally tracked request counts and compares them with known provider
allowances. All requests pass through HttpService, allowing the application to account for its
own traffic automatically.

The counters represent this installation only. If an API key is shared with another computer,
the provider's own developer dashboard remains authoritative for total account/key usage.

Current reference limits built into this release:
- TomTom: 50,000 tile requests/day and 2,500 non-tile requests/day.
- Open-Meteo Free: 10,000 calls/day.
- NWS: tracked informationally because no fixed public quota is published.
- RainViewer: tracked informationally because no fixed public quota is represented.
- TheSportsDB Free: 30 requests/minute; premium uses the configured premium reference.

## API Usage theme fix in v2.1.1
The API Usage screen now fully follows the selected application theme, including JTable rows,
column headers, grid lines, selection colors, viewport background, buttons, and status text.
This resolves pale text becoming unreadable against Swing's default light table background.

## Universal themes in v2.2.0
Themes now apply to both the fullscreen dashboard and the complete Settings experience. The
Settings window previews the selected theme live before it is saved, including tabs, forms,
tables, buttons, API Usage, Find Location, and Find Team dialogs.

New holiday and seasonal presets:
- Holiday • Christmas
- Holiday • Halloween
- Holiday • Thanksgiving
- Holiday • Independence Day
- Holiday • Valentine’s Day
- Holiday • St. Patrick’s Day
- Seasonal • Winter Frost

## Settings control alignment fix in v2.2.1
Theme-aware Settings controls now use a platform-independent combo-box presentation. This removes
macOS native white interior artifacts and keeps dropdown values, arrows, borders, popup items, and
form rows consistently aligned across macOS, Windows, and Raspberry Pi OS.

## Team Celebrations & Theme Overlays in v2.3.0

Settings > Team Celebrations can store local recognition records for birthdays and work
anniversaries. On matching dates the Main Showcase automatically generates a temporary
recognition slide with the team member's name, optional photo, and completed anniversary years.

Photos selected through Settings are copied into:

`~/.weather-traffic-monitor/celebrations-media`

If no photo is supplied, the slide uses a polished initials-based placeholder.

A celebration slide can trigger a short confetti effect once per application session. Holiday
themes can also enable lightweight screen overlays through Settings > General:
Christmas/Winter snowfall, Halloween particles, Thanksgiving leaves, Independence Day sparks,
Valentine hearts, and St. Patrick's shamrocks. Overlay density can be Low, Medium, or High.

Automatic severe-weather map priority always supersedes decorative content: when AUTO LIVE severe
weather takes control, celebration/holiday effects are suppressed and the live map remains visible.

## Polished holiday effects in v2.3.1

Holiday overlays now use effect-specific animation rather than one shared particle style:

- Christmas / Winter Frost: drawn six-arm snowflakes with natural sway, rotation, depth, and a subtle frost/crystal treatment around the screen edges.
- Halloween: multiple translucent rolling fog banks that drift horizontally across the display.
- Independence Day: rising firework rockets that burst into radial red/blue/white/gold spark patterns with trails and gravity.
- Thanksgiving, Valentine's Day, and St. Patrick's Day retain their lightweight seasonal particles.
- Celebration confetti remains a separate one-time recognition effect.

Low / Medium / High intensity continues to control visual density. Severe-weather priority
suppresses all decorative overlays immediately.

## Holiday overlay refinements in v2.3.2

Halloween now uses broad overlapping rolling-fog layers with slow turbulence and thin wisps instead
of visible fog blobs. The theme also adds an orange/purple perimeter light string with soft glow and
subtle independent twinkling.

Thanksgiving now includes three leaf silhouettes (maple-inspired, oak-inspired, and pointed leaves)
with a wider autumn palette and more varied tumbling/drifting motion.

## Automatic holiday themes and overlay polish in v2.4.0

Settings > General now includes **Automatically switch to holiday / seasonal themes**. When
enabled, the application temporarily selects an appropriate holiday theme based on the current
local date while keeping the manually selected theme as the fallback outside holiday windows.

Automatic theme schedule:
- January 1–31: Winter Frost
- February 10–14: Valentine's Day
- March 10–17: St. Patrick's Day
- July 1–4: Independence Day
- October 15–31: Halloween
- Seven days before U.S. Thanksgiving through the following Sunday: Thanksgiving
- December 1–31: Christmas

The active automatic theme is re-evaluated while the display remains running, so date changes do
not require a restart.

Overlay refinements:
- Christmas: snowflakes + frost/crystals + red/green/warm-white perimeter string lights.
- Halloween: deeper multi-layer rolling fog, full-screen ground haze, continuous wisps, and orange/purple lights.
- Valentine's Day: glowing gradient hearts with floating petal accents.
- St. Patrick's Day: dimensional shamrocks with green glow and gold sparkle accents.

## Phone photo orientation fix in v2.4.1

JPEG images from phones/cameras can store their physical pixels sideways and rely on EXIF
Orientation metadata to tell viewers how to rotate them. Swing/ImageIO does not consistently
apply that metadata automatically.

v2.4.1 adds an orientation-aware image loader that reads the JPEG EXIF Orientation tag and
normalizes the pixels before display. Birthday/anniversary photos and Main Showcase JPG/JPEG
announcement images now automatically handle all standard EXIF rotation/mirroring states.

## Complete celebration confetti in v2.4.2

Celebration confetti is now event-driven instead of duration-driven. Each time a birthday or work-
anniversary slide appears, one finite shower is released. The application does not replenish or
remove that shower after a timer expires; every piece continues under its normal motion until it
falls fully beyond the display.

The shower therefore continues naturally if the Main Showcase advances to the map or another
announcement. If the celebration slide appears again later in the rotation, another complete shower
is released. Severe-weather map priority remains the only condition that immediately suppresses
decorative effects.

## Celebration timer lifecycle fix in v2.4.3

Dashboard rebuilds now explicitly dispose the previous Main Showcase before creating the new one.
This stops the old Swing slideshow timer and disconnects its celebration callback. Previously an
invisible old showcase could continue rotating after Settings changes and trigger confetti from a
celebration card that was no longer visible.

Christmas/Winter Frost has also been simplified by removing the crystalline line drawings. The
smooth edge frost gradient, snowflakes, and Christmas lights remain.

## Adaptive overlay performance in v2.5.0

Settings > General includes an **Overlay Performance** selector:

- **Automatic** — recommended. Measures actual overlay paint time and dynamically scales ambient
  effect density/cadence to protect animation fluidity.
- **High Quality** — keeps full visual density and quality-first Java2D rendering at about 30 FPS.
- **Balanced** — reduces ambient density and targets about 25 FPS.
- **Performance** — intended for Raspberry Pi/older hardware; uses a smaller ambient budget,
  simplified expensive details, and about 20 FPS.

Priority rules:
- Celebration confetti is never discarded by adaptive load shedding.
- Active confetti temporarily reduces ambient holiday density so the celebration shower remains smooth.
- Fireworks are treated as priority animation.
- Ambient snow, fog, leaves, hearts, shamrocks, petals, and decorative details scale first.
- Severe-weather priority still suppresses decorative overlays immediately.

Additional optimizations include cached frost gradients, lower-frequency fog simulation, adaptive
Java2D rendering hints, hard ambient-particle budgets, and skipped idle overlay repaints.

## Responsive Settings navigation in v2.5.1

The Settings dialog no longer assumes a fixed 980-pixel width. It now sizes itself from the usable
monitor work area and attempts to show the complete settings navigation row whenever the display has
room. On smaller monitors, the tab strip becomes horizontally scrollable rather than hiding later
categories.

The bottom Exit Application / Cancel / Save & Apply controls remain fixed and available.

The St. Patrick's Day overlay also receives a redesigned shamrock with heart-shaped leaflets,
emerald shading, leaflet veins, a curved stem, depth/highlight details, and the existing gold glints.

## Upcoming sports schedules in v2.6.0

The sports feature is now designed for weekday information displays rather than live game-day
scoreboards. Each configured team becomes an **Upcoming Schedule** option under Dashboard Blocks.

A sports schedule card can show:
- the next scheduled game with date/time,
- whether the configured team is home (`vs`) or away (`at`),
- the opponent,
- league information,
- the configured team's logo when enabled,
- and up to two additional upcoming games.

Normal dashboard sports refreshes now use schedule data only; premium live-score polling is no
longer performed. A 30–60 minute refresh interval is recommended because future schedules change
far less frequently than live scores.

Existing configured teams and SPORTS_n widget selections continue to work automatically.

## Theme-safe sports schedule text and wind icon in v2.6.1

Sports schedule labels are created after asynchronous provider refreshes, so they now explicitly
use the active application theme at creation time. This prevents black matchup/date text from
appearing on dark or holiday-themed dashboard cards.

The Wind & Gusts icon has also been redrawn as three rounded airflow ribbons with smooth curls for
a cleaner, more recognizable weather symbol on large displays.

## Employee of the Month in v2.7.0

Settings > Team Celebrations now keeps all recognition options on one employee row:

- Birthday
- Anniversary
- Employee of Month
- Confetti
- Enabled

Employee of the Month acts as a single selection. Checking one employee automatically unchecks
the previous recipient. Save & Apply associates that selection with the current month/year, and
the recognition expires automatically when the next month begins.

The Main Showcase generates a dedicated Employee of the Month card for the selected employee
throughout that month. It uses the existing optional employee photo (or initials when no photo is
available), includes trophy/star recognition artwork, and can use the existing confetti setting.

Birthday and anniversary preferences remain independent. An employee can opt out of birthday
recognition while still being eligible for anniversary or Employee of the Month recognition.

## Operations Calendar in v2.8.0

Settings > Operations Calendar provides a site-managed calendar for operating-schedule changes.

Each event contains:
- Event / holiday name
- Start date
- End date
- Operation type
- Start/end work time when applicable
- Optional per-event announcement lead time
- Enabled/disabled state

Operation types:
- **Full Closure** — no regular operations; time fields are ignored.
- **Limited Service** — the facility operates only during the specified limited-service window.
- **Modified Hours** — temporary operating hours for one date or a multi-day range.

The installation also stores its normal operating hours and normal weekdays. These values allow
generated slides to explain how temporary hours differ from normal and calculate when normal
operations resume.

### Automatic Main Showcase behavior

Operations announcements are generated from calendar data rather than stored as image files.
They appear automatically when their announcement lead-time window begins and are removed
automatically after the event period ends.

Adjacent/connected events are grouped into one slide. For example:

- Thanksgiving — Full Closure
- Friday after Thanksgiving — Limited Service, 11:00 AM–2:00 PM

becomes one Holiday & Operations Schedule slide instead of two separate announcements.

A Modified Hours event can span an entire week. The same generated announcement remains active
through the date range, changes its status wording as the period begins/ends, and disappears after
the last day.
