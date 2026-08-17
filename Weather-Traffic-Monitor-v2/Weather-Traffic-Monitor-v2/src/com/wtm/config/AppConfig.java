package com.wtm.config;

import com.wtm.model.Location;
import com.wtm.model.RouteConfig;
import com.wtm.model.SportsConfig;
import java.nio.file.Path;
import java.util.*;

/**
 * In-memory application configuration.
 *
 * Site-specific values live here instead of in dashboard classes so one build
 * can be reused at any facility. Pinned locations and commute routes are
 * deliberately dynamic; there is no hard-coded facility limit.
 */
public final class AppConfig {
    public boolean fullscreen = true;
    /**
     * Theme preset ID. darkMode is retained as a compatibility/map-rendering
     * flag and is synchronized from the chosen theme.
     */
    public String themeId = "DARK";
    public boolean darkMode = true;
    public boolean showHeader = true;
    public boolean showTicker = true;
    public boolean showRadar = true;
    public boolean showTraffic = true;
    public boolean showAlertsOnMap = true;
    public String headerText = "VANCE LOGISTICS WEATHER & TRAFFIC";
    public String tickerText = "Weather and traffic conditions refresh automatically throughout the day.";
    public int weatherRefreshMinutes = 10;
    public int alertRefreshMinutes = 2;
    public int radarRefreshMinutes = 5;
    public int trafficRefreshMinutes = 5;

    /**
     * High-frequency severe-weather monitoring mode.
     *
     * This does not alter TomTom traffic cadence. It temporarily overrides
     * weather/radar/NWS alert scheduler intervals while enabled.
     */
    public boolean liveSevereWeatherMode = false;

    /**
     * Allows NWS alert detection to automatically enter rapid severe-weather
     * polling without changing the user's manual live-mode preference.
     */
    public boolean automaticSevereWeatherMode = true;

    /**
     * Returns an automatically-triggered live state to normal after qualifying
     * severe-weather alerts clear.
     */
    public boolean autoDisableSevereWeatherMode = true;

    /** Supported weather adapter: OPEN_METEO_FREE or OPEN_METEO_CUSTOMER. */
    public String weatherProvider = "OPEN_METEO_FREE";

    /** Optional Open-Meteo customer-plan API key. Blank for free access. */
    public String weatherApiKey = "";

    /** Alert adapter. NWS is the currently installed U.S. severe-weather adapter. */
    public String alertProvider = "NWS";

    /** NWS identifies clients by User-Agent rather than a traditional API key. */
    public String nwsUserAgent = "WeatherTrafficMonitor/1.6 (workplace-display; contact=local-admin)";

    /** Radar adapter. RainViewer public radar is currently installed. */
    public String radarProvider = "RAINVIEWER";

    /** Traffic/routing adapter. TomTom is currently installed. */
    public String trafficProvider = "TOMTOM";

    /** TomTom API credential. Stored separately from normal site settings. */
    public String tomTomApiKey = "";

    /** Sports adapter. TheSportsDB is the first installed general-sports provider. */
    public String sportsProvider = "THESPORTSDB";

    /** TheSportsDB v1 free key is currently 123; replace with a premium key if purchased. */
    public String sportsApiKey = "123";

    /** Enables TheSportsDB premium v2 livescore endpoint when a premium key is supplied. */
    public boolean sportsPremiumLiveScores = false;

    /** Sports block refresh cadence. Two minutes is appropriate for premium live scores. */
    public int sportsRefreshMinutes = 5;
    public Path mediaDirectory = ConfigService.appDataDir().resolve("media");

    /**
     * Enables the large Main Showcase to cycle between the live map and
     * company-announcement media.
     */
    public boolean mainShowcaseMediaEnabled = false;

    /** Number of seconds each Main Showcase item stays visible. */
    public int mainShowcaseIntervalSeconds = 30;

    /**
     * When AUTO LIVE severe-weather monitoring is active, keep the map visible
     * and suspend Main Showcase media rotation. Can be disabled for testing.
     */
    public boolean severeWeatherMapPriority = true;

    /** Number of visible information cards beside the map. Supported range: 6–12. */
    public int visibleWidgetCount = 10;

    /**
     * Percentage of the main dashboard width reserved for the map.
     * The remaining width is used by the information-card grid.
     */
    public int mapWidthPercent = 63;

    public Location primary = new Location("Vance", 33.1743, -87.2336);

    /** All map pins / forecast locations. Add as many as the site needs. */
    public final List<Location> monitored = new ArrayList<>(List.of(
            new Location("Tuscaloosa", 33.2098, -87.5692),
            new Location("Vance", 33.1743, -87.2336),
            new Location("Birmingham", 33.5186, -86.8104),
            new Location("Hoover", 33.4054, -86.8114),
            new Location("Trussville", 33.6198, -86.6089)
    ));

    /**
     * Sports selections become dashboard-block choices exactly like configured
     * routes. These two college-football examples can be edited or removed.
     */
    public final List<SportsConfig> sports = new ArrayList<>(List.of(
            new SportsConfig("Alabama Football", "American Football", "4479", "136168", "Alabama", true),
            new SportsConfig("Tennessee Football", "American Football", "4479", "136957", "Tennessee", true)
    ));

    /** Traffic-aware routes always originate at the primary facility. */
    public final List<RouteConfig> routes = new ArrayList<>(List.of(
            new RouteConfig("Tuscaloosa", primary, new Location("Tuscaloosa", 33.2098, -87.5692)),
            new RouteConfig("Birmingham", primary, new Location("Birmingham", 33.5186, -86.8104)),
            new RouteConfig("Hoover", primary, new Location("Hoover", 33.4054, -86.8114)),
            new RouteConfig("Trussville", primary, new Location("Trussville", 33.6198, -86.6089))
    ));

    /**
     * Widget identifiers are persisted rather than card classes. Dynamic IDs
     * use ROUTE_n and WEATHER_LOCATION_n so newly added site data can be placed
     * on the dashboard without changing source code.
     */
    public final List<String> widgetTypes = new ArrayList<>(List.of(
            "WEATHER_PRIMARY", "ROUTE_0",
            "ROUTE_1", "ROUTE_2",
            "ROUTE_3", "ALERTS",
            "WEATHER_LOCATION_0", "WEATHER_LOCATION_2",
            "FORECAST_PRIMARY", "MEDIA",
            "WIND_PRIMARY", "STATUS"
    ));
}
