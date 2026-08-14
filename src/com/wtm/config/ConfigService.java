package com.wtm.config;

import com.wtm.model.Location;
import com.wtm.model.RouteConfig;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Loads and saves human-readable .properties configuration in the user's home directory. */
public final class ConfigService {
    private static final String APP_DIR = ".weather-traffic-monitor";
    private static final String FILE_NAME = "config.properties";

    private ConfigService() {}

    public static Path appDataDir() {
        return Path.of(System.getProperty("user.home"), APP_DIR);
    }

    public static AppConfig load() {
        AppConfig cfg = new AppConfig();
        try {
            Files.createDirectories(appDataDir());
            Files.createDirectories(cfg.mediaDirectory);
            Path file = appDataDir().resolve(FILE_NAME);
            if (!Files.exists(file)) {
                save(cfg);
                return cfg;
            }
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) { p.load(in); }

            cfg.fullscreen = bool(p, "fullscreen", cfg.fullscreen);
            cfg.darkMode = bool(p, "darkMode", cfg.darkMode);
            cfg.showHeader = bool(p, "showHeader", cfg.showHeader);
            cfg.showTicker = bool(p, "showTicker", cfg.showTicker);
            cfg.showRadar = bool(p, "showRadar", cfg.showRadar);
            cfg.showTraffic = bool(p, "showTraffic", cfg.showTraffic);
            cfg.showAlertsOnMap = bool(p, "showAlertsOnMap", cfg.showAlertsOnMap);
            cfg.headerText = p.getProperty("headerText", cfg.headerText);
            cfg.tickerText = p.getProperty("tickerText", cfg.tickerText);
            cfg.tomTomApiKey = p.getProperty("tomTomApiKey", "").trim();
            cfg.weatherRefreshMinutes = integer(p, "weatherRefreshMinutes", 10);
            cfg.alertRefreshMinutes = integer(p, "alertRefreshMinutes", 2);
            cfg.radarRefreshMinutes = integer(p, "radarRefreshMinutes", 5);
            cfg.trafficRefreshMinutes = integer(p, "trafficRefreshMinutes", 5);
            cfg.liveSevereWeatherMode = bool(p, "liveSevereWeatherMode", false);
            cfg.automaticSevereWeatherMode = bool(p, "automaticSevereWeatherMode", true);
            cfg.autoDisableSevereWeatherMode = bool(p, "autoDisableSevereWeatherMode", true);
            cfg.mainShowcaseMediaEnabled = bool(p, "mainShowcaseMediaEnabled", false);
            cfg.mainShowcaseIntervalSeconds = Math.max(5, Math.min(600,
                    integer(p, "mainShowcaseIntervalSeconds", 30)));
            cfg.severeWeatherMapPriority = bool(p, "severeWeatherMapPriority", true);
            cfg.visibleWidgetCount = Math.max(6, Math.min(12, integer(p, "visibleWidgetCount", cfg.visibleWidgetCount)));
            cfg.mapWidthPercent = Math.max(55, Math.min(75, integer(p, "mapWidthPercent", cfg.mapWidthPercent)));
            cfg.mediaDirectory = Path.of(p.getProperty("mediaDirectory", cfg.mediaDirectory.toString()));
            Files.createDirectories(cfg.mediaDirectory);

            cfg.primary = readLocation(p, "primary", cfg.primary);
            cfg.monitored.clear();
            int mc = integer(p, "monitored.count", 3);
            for (int i = 0; i < mc; i++) {
                Location fallback = i < 3 ? new AppConfig().monitored.get(i) : cfg.primary;
                cfg.monitored.add(readLocation(p, "monitored." + i, fallback));
            }

            cfg.routes.clear();
            int rc = integer(p, "routes.count", 3);
            for (int i = 0; i < rc; i++) {
                String prefix = "route." + i;
                String name = p.getProperty(prefix + ".name", "Route " + (i + 1));
                Location dest = readLocation(p, prefix + ".destination", cfg.primary);
                cfg.routes.add(new RouteConfig(name, cfg.primary, dest));
            }

            cfg.widgetTypes.clear();
            int wc = integer(p, "widgets.count", 6);
            for (int i = 0; i < wc; i++) cfg.widgetTypes.add(p.getProperty("widget." + i, "STATUS"));
        } catch (Exception ex) {
            System.err.println("Configuration load failed; using defaults: " + ex.getMessage());
        }
        return cfg;
    }

    public static void save(AppConfig cfg) {
        try {
            Files.createDirectories(appDataDir());
            Files.createDirectories(cfg.mediaDirectory);
            Properties p = new Properties();
            p.setProperty("fullscreen", Boolean.toString(cfg.fullscreen));
            p.setProperty("darkMode", Boolean.toString(cfg.darkMode));
            p.setProperty("showHeader", Boolean.toString(cfg.showHeader));
            p.setProperty("showTicker", Boolean.toString(cfg.showTicker));
            p.setProperty("showRadar", Boolean.toString(cfg.showRadar));
            p.setProperty("showTraffic", Boolean.toString(cfg.showTraffic));
            p.setProperty("showAlertsOnMap", Boolean.toString(cfg.showAlertsOnMap));
            p.setProperty("liveSevereWeatherMode", Boolean.toString(cfg.liveSevereWeatherMode));
            p.setProperty("automaticSevereWeatherMode", Boolean.toString(cfg.automaticSevereWeatherMode));
            p.setProperty("autoDisableSevereWeatherMode", Boolean.toString(cfg.autoDisableSevereWeatherMode));
            p.setProperty("mainShowcaseMediaEnabled", Boolean.toString(cfg.mainShowcaseMediaEnabled));
            p.setProperty("mainShowcaseIntervalSeconds", Integer.toString(cfg.mainShowcaseIntervalSeconds));
            p.setProperty("severeWeatherMapPriority", Boolean.toString(cfg.severeWeatherMapPriority));
            p.setProperty("headerText", cfg.headerText);
            p.setProperty("tickerText", cfg.tickerText);
            p.setProperty("tomTomApiKey", cfg.tomTomApiKey == null ? "" : cfg.tomTomApiKey);
            p.setProperty("weatherRefreshMinutes", Integer.toString(cfg.weatherRefreshMinutes));
            p.setProperty("alertRefreshMinutes", Integer.toString(cfg.alertRefreshMinutes));
            p.setProperty("radarRefreshMinutes", Integer.toString(cfg.radarRefreshMinutes));
            p.setProperty("trafficRefreshMinutes", Integer.toString(cfg.trafficRefreshMinutes));
            p.setProperty("visibleWidgetCount", Integer.toString(cfg.visibleWidgetCount));
            p.setProperty("mapWidthPercent", Integer.toString(cfg.mapWidthPercent));
            p.setProperty("mediaDirectory", cfg.mediaDirectory.toString());
            writeLocation(p, "primary", cfg.primary);

            p.setProperty("monitored.count", Integer.toString(cfg.monitored.size()));
            for (int i = 0; i < cfg.monitored.size(); i++) writeLocation(p, "monitored." + i, cfg.monitored.get(i));

            p.setProperty("routes.count", Integer.toString(cfg.routes.size()));
            for (int i = 0; i < cfg.routes.size(); i++) {
                RouteConfig r = cfg.routes.get(i);
                p.setProperty("route." + i + ".name", r.name());
                writeLocation(p, "route." + i + ".destination", r.destination());
            }

            p.setProperty("widgets.count", Integer.toString(cfg.widgetTypes.size()));
            for (int i = 0; i < cfg.widgetTypes.size(); i++) p.setProperty("widget." + i, cfg.widgetTypes.get(i));

            try (OutputStream out = Files.newOutputStream(appDataDir().resolve(FILE_NAME))) {
                p.store(out, "Weather & Traffic Monitor configuration");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to save configuration", ex);
        }
    }

    private static boolean bool(Properties p, String k, boolean d) { return Boolean.parseBoolean(p.getProperty(k, Boolean.toString(d))); }
    private static int integer(Properties p, String k, int d) { try { return Integer.parseInt(p.getProperty(k, Integer.toString(d))); } catch (Exception e) { return d; } }
    private static Location readLocation(Properties p, String prefix, Location d) {
        String name = p.getProperty(prefix + ".name", d.name());
        double lat = number(p.getProperty(prefix + ".lat"), d.latitude());
        double lon = number(p.getProperty(prefix + ".lon"), d.longitude());
        return new Location(name, lat, lon);
    }
    private static void writeLocation(Properties p, String prefix, Location l) {
        p.setProperty(prefix + ".name", l.name());
        p.setProperty(prefix + ".lat", Double.toString(l.latitude()));
        p.setProperty(prefix + ".lon", Double.toString(l.longitude()));
    }
    private static double number(String v, double d) { try { return v == null ? d : Double.parseDouble(v); } catch (Exception e) { return d; } }
}
