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

## 2.4.1
- Fixed sideways/rotated celebration photos from phones and cameras.
- Added orientation-aware JPEG loading with direct support for EXIF Orientation tag 0x0112.
- Supports all eight standard EXIF orientation states, including 90/180/270-degree rotation and mirrored/transposed variants.
- Celebration photos are normalized before scaling into birthday/work-anniversary slides.
- Applied the same orientation-aware loader to Main Showcase announcement JPG/JPEG images so phone-originated media displays correctly there as well.
- Upgraded celebration-photo scaling interpolation from bilinear to bicubic/high-quality rendering.
- No celebration scheduling, overlay, theme, severe-weather, API, or configuration behavior changed.

## 2.4.2
- Reworked celebration confetti from a fixed-duration effect into a finite physics-driven shower.
- Confetti now triggers every time an enabled birthday/work-anniversary slide rotates into view.
- Each trigger releases one complete batch; particles are no longer replenished on a timer.
- Every confetti piece remains visible until it naturally falls beyond the display boundary.
- Confetti continues across slideshow transitions, including when the celebration card advances to the map or another announcement.
- Holiday theme overlays continue independently while celebration confetti is falling.
- Seasonal overlay maintenance no longer clears active celebration confetti.
- Confetti spawn positions are staggered above the display for a more natural cascading entrance.
- Severe-weather priority remains the only runtime condition that immediately clears/suppresses celebration confetti.

## 2.4.3
- Fixed phantom celebration confetti that could appear after opening/saving Settings.
- Root cause: dashboard rebuilds replaced the visible Main Showcase but did not stop the old showcase's Swing rotation timer.
- Old invisible showcase timers could continue advancing and later trigger their hidden celebration cards, causing confetti while no celebration slide was visible.
- Added MainShowcasePanel.disposeShowcase() to stop its rotation timer and disconnect the celebration callback.
- DashboardFrame now disposes the existing Main Showcase before every Settings/theme/layout rebuild.
- This also prevents multiple hidden slideshow timers from accumulating after repeated Settings changes.
- Celebration confetti still triggers normally whenever the currently visible celebration slide rotates into view.
- Removed the drawn crystalline line strokes from Christmas/Winter Frost.
- Retained the smooth frosted-edge gradient, snowflakes, and Christmas perimeter lights.

## 2.5.0
- Added Settings > General > Overlay Performance with Automatic, High Quality, Balanced, and Performance modes.
- Added frame-budget-aware adaptive overlay rendering.
- Automatic mode measures overlay paint cost with an exponential moving average and dynamically adjusts ambient density and animation cadence.
- Automatic mode targets approximately 30 FPS under light load, 25 FPS under moderate load, and 20 FPS when overlay rendering becomes expensive.
- Celebration confetti retains priority and is never removed by adaptive ambient-particle trimming.
- When confetti is active, ambient holiday density is temporarily reduced to preserve smooth celebration motion.
- Fireworks retain priority while their maximum simultaneous count is hardware-profile aware.
- Added a hard ambient-particle budget that immediately trims seasonal decoration when adaptive load reduction is needed.
- Halloween fog updates at a lower simulation cadence in Balanced/Performance/heavy Automatic modes while remaining continuously painted.
- Simplified snowflake branch detail only under heavy Automatic load or Performance mode.
- Cached the static Christmas/Winter Frost edge-gradient layer so its gradients are not rebuilt every animation frame.
- Overlay rendering switches from quality-first Java2D hints to speed-first hints under constrained modes/heavy Automatic load.
- Overlay timer cadence is profile-aware: High Quality ~30 FPS, Balanced ~25 FPS, Performance ~20 FPS, Automatic adaptive.
- Idle overlay frames no longer request unnecessary repaints when no decorative animation is active.
- Existing finite celebration-confetti behavior, holiday visuals, severe-weather suppression, and slideshow logic remain unchanged.

## 2.5.1
- Made the Settings window responsive to the current monitor's usable work area.
- Settings now opens wider on larger displays so the full category navigation row is visible without manual resizing.
- Added scrollable JTabbedPane navigation so future settings categories remain reachable on smaller displays instead of being clipped off-screen.
- Initial settings width now considers both the monitor size and the preferred width of the current tab strip.
- Settings height is also capped to the monitor's usable work area so the dialog remains practical on Raspberry Pi/TV and laptop displays.
- Exit Application, Cancel, and Save & Apply remain fixed at the bottom of the dialog.
- Redesigned the St. Patrick's Day shamrock overlay with three heart-shaped clover leaflets instead of circular lobes.
- Added richer emerald gradients, leaflet outlines/veins, center depth, a curved tapered stem, subtle glow, and a small highlight for a more dimensional shamrock.
- Existing gold glint particles, adaptive overlay performance, and severe-weather suppression remain unchanged.

## 2.6.0
- Changed configurable Sports dashboard blocks from live/recent score tracking to upcoming schedule tracking.
- Sports blocks now request the configured team's upcoming schedule and display up to the next three games.
- The nearest upcoming game is emphasized with date/time, home/away context, opponent, league, and configured-team logo when available.
- Two additional future games are displayed as compact schedule rows when provider data is available.
- Dashboard sports titles now use SCHEDULE rather than SPORTS SCORE.
- Dashboard Block customization now labels configured sports choices as Upcoming Schedule.
- Removed premium-live-score polling from normal dashboard refreshes, substantially reducing unnecessary sports API traffic during the work week.
- Premium provider access remains available for enhanced team search where supported.
- Sports refresh presets are now 15, 30, 60, 120, and 240 minutes, with 30–60 minutes recommended.
- Existing installations with old sports refresh values below 15 minutes automatically migrate to a 30-minute schedule refresh.
- Existing SportsConfig records and SPORTS_n dashboard widget IDs remain compatible; users do not need to recreate configured teams or blocks.
- Legacy SportsScorePanel remains in source for compatibility but is no longer used by dashboard blocks.

## 2.6.1
- Fixed sports schedule text using the platform-default black foreground on dark and holiday themes.
- SportsSchedulePanel now applies Theme.text() / Theme.muted() directly when asynchronously generated schedule labels are created.
- Loading, no-schedule, primary matchup, and secondary upcoming-game rows now remain readable across every application theme.
- Redesigned the Wind & Gusts dashboard icon with three clean airflow ribbons and rounded curls for improved readability at TV distance.
- The new wind symbol removes the intersecting/stacked-arc appearance of the previous icon while preserving theme accent coloring.

## 2.6.2
- Refined the Wind & Gusts icon to use the familiar three-line meteorological wind-gust silhouette.
- Added smooth upper, center, and lower curls modeled after conventional weather wind symbols.
- Preserved vector rendering, rounded strokes, scaling, and active-theme accent coloring.

## 2.7.0
- Added Employee of the Month recognition to Settings > Team Celebrations.
- Preserved the one-row-per-team-member model: Birthday, Anniversary, and Employee of the Month are independent recognition options on the same employee record.
- Added an Employee of Month checkbox column to the Team Celebrations table.
- Employee of the Month is single-select: checking one team member automatically clears the selection from every other row.
- The selected recipient is stamped with the current month and year when Save & Apply is used.
- Employee of the Month recognition automatically expires when the calendar moves into a different month.
- Added a current-recipient status line showing the active month/year and selected employee.
- Existing birthday opt-out and anniversary opt-out behavior remains independent.
- Added a dedicated Employee of the Month Main Showcase card with month/year, employee photo or initials, trophy/star artwork, employee name, and congratulatory message.
- Employee of the Month remains visible throughout its assigned month and is kept separate from same-day birthday/anniversary cards.
- Employee of the Month participates in the existing per-employee Confetti setting and triggers the finite confetti shower whenever its slide rotates into view.
- Existing celebration configuration files load safely; older employee rows simply begin with no Employee of the Month assignment.

## 2.8.0
- Added a new Settings > Operations Calendar module.
- Added three reusable operating-status types: Full Closure, Limited Service, and Modified Hours.
- Operations records support both one-day events and multi-day date ranges.
- Added configurable normal operating hours and normal operating weekdays for each installation.
- Added a site-wide default announcement lead time plus optional per-event Lead Days overrides.
- Full Closure events automatically ignore work-hour fields.
- Limited Service and Modified Hours require validated start/end work hours.
- Time entry accepts common formats such as 7:30 AM, 11:00 AM, and 16:00.
- Operations Calendar announcements are generated dynamically and require no uploaded announcement image.
- Upcoming events automatically enter the Main Showcase when their announcement window begins.
- Operations slides automatically disappear after the final event date; no manual cleanup is required.
- Connected/adjacent operations dates are automatically grouped into one announcement slide.
- Example supported grouping: Thanksgiving Full Closure followed by Friday Limited Service becomes one slide.
- Multi-day Modified Hours remain in the slideshow throughout the entire configured date range and disappear after the range ends.
- Generated slide wording changes automatically between UPCOMING, OPERATIONS SCHEDULE IN EFFECT, and FINAL DAY.
- Added visual status treatment for Full Closure, Limited Service, and Modified Hours.
- Modified-hours announcements compare temporary hours against the site's normal operating schedule and can call out earlier starts or earlier/later endings.
- Generated announcements automatically calculate the next normal operating day after a grouped event period.
- Return-to-normal calculation respects configured normal weekdays and skips dates covered by other enabled operations events.
- Severe-weather Main Showcase priority remains unchanged and still takes precedence over operations announcements.
- Existing media, sports, Team Celebrations, Employee of the Month, themes, overlays, and API settings remain compatible.
