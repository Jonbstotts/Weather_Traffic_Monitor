# Changelog

## 1.0.0
- Fullscreen dark-mode workplace dashboard
- Vance/Tuscaloosa/Birmingham default weather strip
- Cached road map with pan and zoom
- RainViewer weather-radar overlay
- NWS severe-weather alerts and GeoJSON map polygons
- TomTom live traffic-flow overlay when an API key is supplied
- TomTom traffic-aware route travel times for three configurable routes
- Six configurable information blocks
- Rotating image media block
- Optional facility title and scrolling announcement ticker
- Settings-only application exit
- Local configuration/cache/media storage
- Resilient refresh behavior that retains last successful data
- Java 21 dependency-free build and launcher scripts for macOS/Linux/Windows

## 1.0.1
- Fixed RainViewer radar tiles displaying “Zoom Level Not Supported” when the base map zoom exceeded RainViewer's maximum z=7.
- Higher map zooms now crop/upscale the correct z=7 radar parent tile while preserving road-map detail.

## 1.0.2
- Increased separation between the three top forecast cards and gave forecast cards a distinct secondary surface in light and dark themes.
- Switched RainViewer radar requests from 256px to 512px tiles for higher visual resolution.
- Added bilinear radar scaling above RainViewer zoom 7 to reduce blocky/pixelated enlargement while preserving detailed road-map zoom.

## 1.1.0
- Reworked the dashboard visual system to match the polished operations-display concept.
- Standardized 14px spacing between header, forecasts, map, widgets, and ticker.
- Added consistent rounded outlines to cards in both light and dark mode.
- Added built-in vector weather icons to location forecasts and current-weather widgets.
- Added built-in route, severe-alert, wind, forecast, media, and system-status icons.
- Improved route cards with traffic-severity status coloring.
- Refined typography hierarchy and card padding for large TV readability.
- Adjusted map/widget split so information blocks have a more balanced, uniform footprint.
- Refined light and dark palettes while preserving existing API, radar, traffic, settings, and caching behavior.

## 1.2.0
- Added an unlimited pinned-location editor in Settings.
- Added Hoover and Trussville to the fresh-install Vance configuration.
- Pinned locations automatically appear on the map and become selectable weather widgets.
- Replaced the fixed three-route settings form with an unlimited route table.
- Added a one-click “Route from selected pin” workflow.
- Dashboard widget choices are now generated from current pinned locations and routes.
- Added selectable 6, 8, 10, or 12 information blocks beside the map.
- Ten/twelve-block layouts use a three-column adaptive grid for large TV displays.
- Forecast cards wrap after five columns so large location lists remain readable.
- Existing configuration files migrate forward; current TomTom/API settings remain external to the JAR.

## 1.2.1
- Removed the interactive JSplitPane divider between the map and information blocks.
- Eliminated the small divider/resize control that could accidentally shrink the map or enlarge the cards.
- Replaced the split pane with a responsive fixed 63/37 GridBagLayout.
- The dashboard still scales with fullscreen/window size, but the map-to-card ratio can no longer be manually dragged.

## 1.2.2
- Fixed the map shrinking after the dashboard completed its Swing layout pass.
- Replaced GridBagLayout for the map/card regions with a deterministic FixedRatioLayout.
- The map now permanently receives 63% of available dashboard width and cards receive 37%.
- Card preferred/minimum sizes can no longer force the map narrower after startup.
- Removed all interactive or implicit map/card resizing behavior while preserving normal fullscreen/window scaling.

## 1.3.0
- Added controlled map/information resizing in Settings > Dashboard Blocks.
- Added a 55%–75% map-width slider with a locked complementary information-panel percentage.
- Added one-click Information Focused (55/45), Balanced (63/37), and Map Focused (70/30) presets.
- The chosen ratio is saved in the site configuration and restored on every launch.
- Save & Apply immediately rebuilds the dashboard at the selected ratio.
- Normal dashboard operation remains non-draggable, preventing accidental TV-layout changes.

## 1.3.1
- Added quick refresh-rate controls under Settings > Data & APIs.
- Route/traffic refresh can now be set to 2, 5, 10, 15, 20, or 30 minutes.
- Weather refresh can be set to 5, 10, 15, 20, 30, or 60 minutes.
- Radar refresh can be set to 2, 5, 10, or 15 minutes.
- NWS alert refresh can be set to 1, 2, 5, 10, or 15 minutes.
- Save & Apply now cancels and recreates background scheduler jobs immediately.
- Refresh-rate changes no longer require an application restart.
- Existing custom interval values are preserved and shown even if they are not one of the standard presets.

## 1.3.2
- Added persistent Live Severe Weather Mode under Settings > Data & APIs.
- Live mode refreshes NWS alerts every 1 minute.
- Live mode refreshes radar every 2 minutes.
- Live mode refreshes current weather every 2 minutes.
- TomTom traffic/routing remains on the normal user-selected interval to protect API usage.
- Turning Live Severe Weather Mode off restores the normal weather/radar/alert refresh settings.
- Save & Apply switches between live and normal scheduler intervals immediately without restarting.

## 1.4.0
- Added Automatic Severe Weather Mode.
- Added a separate checkbox to enable/disable automatic triggering.
- Added an Auto Return checkbox, enabled by default, to return to normal refresh rates after qualifying severe alerts clear.
- Automatic mode triggers for Tornado Warning/Watch, Tornado Emergency, Severe Thunderstorm Warning/Watch, Flash Flood Warning, Extreme Wind Warning, and NWS alerts classified as Extreme.
- Automatic live polling uses 1-minute NWS alerts plus 2-minute radar and current-weather checks.
- TomTom traffic/routing remains on its normal selected interval.
- Manual Live Severe Weather Mode remains independent and always overrides normal polling while selected.
- System Status now displays NORMAL, MANUAL LIVE, or AUTO LIVE and shows the triggering alert when available.

## 1.5.0
- Converted the large map region into a configurable Main Showcase.
- Added Settings > Main Showcase.
- Main Showcase can remain map-only or cycle between the live map and announcement images.
- Added configurable 10-second through 5-minute showcase intervals.
- Main Showcase uses PNG/JPG/JPEG/GIF files from the configured media folder.
- Added Severe Weather Map Priority, enabled by default.
- AUTO LIVE severe-weather monitoring immediately forces the Main Showcase back to the map and pauses media rotation.
- Media rotation resumes automatically after the automatic severe-weather state clears.
- Severe Weather Map Priority can be disabled independently for troubleshooting/testing.
- The smaller Media dashboard block remains independent from Main Showcase rotation.

## 1.5.1
- Removed media filenames from the Main Showcase.
- Announcement images now use the full Main Showcase region without a filename/caption bar.
- AUTO LIVE severe-weather mode now takes priority over the bottom ticker.
- AUTO LIVE ticker text identifies the triggering NWS alert when available.
- MANUAL LIVE mode also displays a distinct live-weather ticker status.
- When live severe-weather mode clears, the normal configured ticker message returns automatically.
- Ticker status refreshes immediately when severe-weather state changes.

## 1.6.0
- Added Settings > API Providers for centralized provider and credential management.
- Added Open-Meteo Free and Open-Meteo Customer weather-provider choices.
- Open-Meteo Customer mode uses customer-api.open-meteo.com and an API key entered in Settings.
- Added configurable NWS User-Agent identification.
- Listed installed alert, radar, traffic/routing providers in Settings for future adapter expansion.
- Moved TomTom and weather API secrets into a separate credentials.properties file.
- Added automatic migration of an existing TomTom key from older config.properties files.
- On POSIX systems, credentials.properties is restricted to owner read/write where supported.
- System Status now identifies the configured weather and traffic providers/credential state.

## 1.7.0
- Added a dedicated Sports configuration tab.
- Sports selections behave like routes: each configured selection automatically appears under Dashboard Blocks as a Sports Score option.
- Added TheSportsDB as the first sports provider, with provider/key controls under API Providers.
- Added editable Alabama Football and Tennessee Football examples using TheSportsDB NCAA Division 1 Football IDs.
- Sports score cards display home/away teams, scores when available, game status, kickoff time, and team artwork/logos.
- Added configurable sports refresh intervals from 2 to 60 minutes.
- Free TheSportsDB mode supports team artwork, upcoming events, and recent/final results.
- Premium TheSportsDB mode can use the v2 live-score endpoint when a premium API key is supplied.
- Sports credentials are stored with the application's other API credentials.
- Sports service and normalized models are separated from the UI for future provider adapters.

## 1.8.0
- Added Find Team to Settings > Sports.
- Team search is provider-backed and supports any sport/team returned by the configured provider.
- Search results display team name, league, sport, country, Team ID, League ID, and provider.
- Added team-logo preview inside the Find Team result dialog.
- Use This Team automatically fills Sport, League ID, Team ID, Team Name, and enables logos.
- Find Team can populate an existing selected sports row or create a new sports selection automatically.
- Newly configured teams continue to appear automatically under Dashboard Blocks as Sports Score choices.
- Added provider-neutral TeamSearchResult model so future sports providers can implement the same search workflow.
- TheSportsDB v2 premium team search is supported; the UI clearly reports current free-v1 general-search restrictions instead of silently failing.

## 1.9.0
- Added Find Location search to Settings > Pinned Locations.
- Added Find Primary Location to populate the primary facility/map-center fields.
- Added Find Destination to Settings > Routes.
- Location search uses Open-Meteo's keyless Geocoding API and ranked GeoNames place results.
- Search results show location, state/region, country, latitude, longitude, timezone, and population.
- Choosing a result automatically fills latitude/longitude and descriptive location fields.
- Find Location can populate an existing selected row or create a new pinned location.
- Find Destination can populate an existing route or create a new route directly.
- Manual coordinate entry remains available for exact sites or places not represented in the geocoder.
- Added provider-neutral LocationSearchResult so another geocoder can be introduced later without redesigning the UI.

## 2.0.0
- Replaced the binary Light/Dark option with a full application theme system.
- Added ten built-in themes: Dark, Light, Graphite/Silver, Operations Blue, Midnight Blue, Slate, Emerald, Amber/Night, High Contrast, and Warm Neutral.
- Added a live theme palette preview under Settings > General.
- Theme selection controls dashboard backgrounds, cards, secondary surfaces, borders, text, muted text, and accent colors.
- Dark-family presets automatically use the dark map/traffic presentation; light-family presets automatically use the light map presentation.
- Existing installations migrate automatically: prior Dark Mode becomes Dark and prior Light Mode becomes Light.
- Theme IDs are persisted in site configuration so each facility can maintain its own visual identity.
- Centralized palette architecture makes additional themes straightforward to add later without changing individual widgets.

## 2.1.0
- Added Settings > API Usage.
- Added installation-local request accounting through the centralized HttpService.
- Tracks TomTom tile requests separately from TomTom non-tile/routing requests.
- Tracks Open-Meteo, NWS, RainViewer, and TheSportsDB requests.
- Persists counters across application restarts in api-usage.properties.
- Displays current allowance period, known limit, percentage used, status, and provider notes.
- Adds OK / WATCH / WARNING / CRITICAL threshold states at <60%, 60%, 80%, and 95%.
- Clearly labels locally tracked usage separately from provider account-wide usage.
- Added Reset Local Counters for troubleshooting without altering provider-side usage.
- Uses current TomTom daily allowance model (50,000 tiles / 2,500 non-tile) rather than an obsolete monthly allowance.

## 2.1.1
- Fixed API Usage table text blending into the background on dark-family themes.
- Made the API Usage JTable fully theme-aware.
- Applied theme colors to table body, headers, viewport, grid lines, borders, buttons, summary text, and selections.
- Added automatic contrasting selection text for bright/dark accent colors.
- Preserved status coloring for INFO, WATCH, WARNING, and CRITICAL rows without sacrificing readability.
- No API counting or quota logic changed in this patch.

## 2.2.0
- Extended application themes across the entire Settings interface.
- Added recursive ThemeStyler support for tabs, panels, labels, inputs, password fields, text areas, checkboxes, combo boxes, buttons, sliders, tables, headers, scroll panes, viewports, and separators.
- Settings now previews the selected theme live across the full dialog before Save & Apply.
- Added universal theme support to Find Location and Find Team dialogs.
- API Usage now participates in the same universal settings-theme infrastructure while preserving usage-status colors.
- Added Holiday • Christmas.
- Added Holiday • Halloween.
- Added Holiday • Thanksgiving.
- Added Holiday • Independence Day.
- Added Holiday • Valentine’s Day.
- Added Holiday • St. Patrick’s Day.
- Added Seasonal • Winter Frost.
- Existing operational themes remain available.

## 2.2.1
- Fixed misaligned/clipped dropdown controls introduced by universal theming on macOS.
- Replaced platform-native JComboBox painting in themed Settings screens with a consistent BasicComboBoxUI implementation.
- Removed the white native interior strip visible beneath dark-themed combo-box values.
- Standardized dropdown height, text padding, vertical centering, popup-row height, arrow-button width, borders, and selection colors.
- Applied the fix globally to Theme, Dashboard Blocks, Main Showcase, API Providers, Data & Refresh, and all future Settings combo boxes.
- Standardized themed text-field height and padding for better row alignment.
- Updated GridBag form rows to center controls consistently across macOS, Windows, and Raspberry Pi OS.
- No dashboard functionality, API logic, or saved configuration format changed.

## 2.3.0
- Added Settings > Team Celebrations.
- Added local birthday and work-anniversary records with optional employee photos.
- Birthday records use month/day only; work-anniversary records retain hire year so completed years can be calculated automatically.
- Today's matching birthday/anniversary records generate temporary Main Showcase slides automatically.
- Celebration slides disappear automatically when the date no longer matches.
- Long-running displays refresh date-driven celebration content automatically across midnight without requiring an application restart.
- Added optional initials-based celebration artwork when no employee photo is supplied.
- Added photo import that copies selected employee images into the local celebrations-media application-data directory.
- Added one-time confetti animation when a celebration slide first appears during an application session.
- Confetti can be disabled per team member.
- Added optional application-wide theme overlay effects.
- Christmas and Winter Frost use snowfall.
- Halloween uses subtle drifting spooky particles.
- Thanksgiving uses falling autumn leaves.
- Independence Day uses red/blue spark particles.
- Valentine's Day uses floating hearts.
- St. Patrick's Day uses shamrock particles.
- Added Low / Medium / High overlay intensity.
- Automatic severe-weather map priority suppresses all decorative overlays and celebration effects immediately.
- Decorative effects resume after automatic severe-weather priority clears.
- Celebration names, dates, and photo paths remain local site configuration and are not compiled into the source code.

## 2.3.1
- Reworked holiday effects so each theme has its own distinct animation system instead of generic recolored particles.
- Halloween now uses layered rolling fog banks with translucent haze and horizontal movement across the display.
- Independence Day now uses launch-and-burst fireworks with rising rocket trails, radial bursts, secondary colors, spark trails, fade-out, and gravity.
- Christmas and Winter Frost now use individually drawn six-arm snowflakes instead of falling ovals.
- Snowflakes vary in size, depth, sway, rotation, fall speed, and opacity for a more natural snowfall effect.
- Christmas/Winter Frost now add a subtle frosted-glass edge treatment and crystalline frost detail around the display perimeter.
- Overlay intensity continues to control the density/frequency of snow, fog layers, and fireworks.
- Celebration confetti remains unchanged and independent from holiday theme effects.
- Automatic severe-weather priority still suppresses every decorative effect immediately.

## 2.3.2
- Improved Halloween fog with much wider overlapping fog banks, smoother low-opacity gradients, depth-based movement, slow turbulence, and thin wispy layers.
- Removed the remaining cloud/blob appearance from the Halloween effect in favor of a continuous rolling-mist presentation.
- Added Halloween perimeter string lights with alternating orange and purple bulbs.
- Halloween lights include soft glow halos, visible sockets/bulbs, and a gentle asynchronous twinkle.
- Expanded Thanksgiving leaves from one generic shape to three distinct silhouettes: maple-inspired, oak-inspired, and pointed autumn leaves.
- Added a broader Thanksgiving autumn palette with orange, amber, rust, brown, and muted golden variations.
- Thanksgiving leaves now vary more in size, tumble speed, lateral drift, and sway.
- Existing Christmas snow/frost, Independence Day fireworks, celebration confetti, and severe-weather suppression behavior remain unchanged.

## 2.4.0
- Added optional automatic holiday/seasonal theme switching under Settings > General.
- Automatic holiday switching preserves the saved manual theme as the fallback outside holiday windows.
- Automatic theme windows include January Winter Frost, Valentine's Day lead-in, St. Patrick's Day lead-in, Independence Day week, Halloween season, U.S. Thanksgiving week, and the December Christmas season.
- Long-running displays re-evaluate the effective holiday theme automatically, including across midnight, without requiring an application restart.
- Added Christmas perimeter string lights with red, green, and warm-white bulbs, soft glow, green sockets/wire, and asynchronous twinkle.
- Christmas lights complement the existing snowflake and frost/crystal overlay.
- Further improved Halloween fog with more/larger depth layers, broader fog banks, additional full-screen ground haze, long continuous wispy streams, lower opacity, parallax movement, and softer transitions between banks.
- Retained Halloween orange/purple perimeter lights.
- Upgraded Valentine's Day overlay with layered, gradient-filled heart silhouettes, outer glow, glossy highlights, floating petals, varied pink/red tones, slower drift, and gentle sway.
- Upgraded St. Patrick's Day overlay with shaded dimensional shamrocks, green glow, detailed stems/highlights, and independent gold sparkle/glint particles.
- Existing Thanksgiving multi-shape leaves, Independence Day fireworks, Christmas snow/frost, celebration confetti, and severe-weather suppression remain intact.
- Automatic severe-weather map priority continues to suppress every decorative holiday/celebration effect immediately.
