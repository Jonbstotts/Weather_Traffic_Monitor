package com.wtm.config;

import com.wtm.model.Location;
import com.wtm.model.RouteConfig;
import com.wtm.model.SportsConfig;
import com.wtm.model.CelebrationConfig;
import com.wtm.ui.AppTheme;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;

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
            Files.createDirectories(cfg.celebrationMediaDirectory);
            Path file = appDataDir().resolve(FILE_NAME);
            if (!Files.exists(file)) {
                save(cfg);
                return cfg;
            }
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(file)) { p.load(in); }

            cfg.fullscreen = bool(p, "fullscreen", cfg.fullscreen);
            cfg.darkMode = bool(p, "darkMode", cfg.darkMode);
            String legacyThemeDefault=cfg.darkMode?"DARK":"LIGHT";
            cfg.themeId=p.getProperty("themeId",legacyThemeDefault).trim();
            cfg.automaticHolidayThemes=bool(p,"automaticHolidayThemes",false);
            AppTheme selectedTheme=AppTheme.fromId(cfg.themeId);
            cfg.themeId=selectedTheme.id();
            cfg.darkMode=selectedTheme.dark();
            cfg.showHeader = bool(p, "showHeader", cfg.showHeader);
            cfg.showTicker = bool(p, "showTicker", cfg.showTicker);
            cfg.showRadar = bool(p, "showRadar", cfg.showRadar);
            cfg.showTraffic = bool(p, "showTraffic", cfg.showTraffic);
            cfg.showAlertsOnMap = bool(p, "showAlertsOnMap", cfg.showAlertsOnMap);
            cfg.headerText = p.getProperty("headerText", cfg.headerText);
            cfg.tickerText = p.getProperty("tickerText", cfg.tickerText);
            // Provider selection is ordinary site configuration; API secrets are
            // loaded from credentials.properties below. Keep the legacy TomTom
            // property only as a one-time migration path from releases <=1.5.1.
            String legacyTomTomKey = p.getProperty("tomTomApiKey", "").trim();
            cfg.weatherProvider = p.getProperty("weatherProvider", cfg.weatherProvider).trim();
            cfg.alertProvider = p.getProperty("alertProvider", cfg.alertProvider).trim();
            cfg.radarProvider = p.getProperty("radarProvider", cfg.radarProvider).trim();
            cfg.trafficProvider = p.getProperty("trafficProvider", cfg.trafficProvider).trim();
            cfg.sportsProvider = p.getProperty("sportsProvider", cfg.sportsProvider).trim();
            cfg.sportsPremiumLiveScores = bool(p, "sportsPremiumLiveScores", false);
            cfg.nwsUserAgent = p.getProperty("nwsUserAgent", cfg.nwsUserAgent).trim();
            cfg.weatherRefreshMinutes = integer(p, "weatherRefreshMinutes", 10);
            cfg.alertRefreshMinutes = integer(p, "alertRefreshMinutes", 2);
            cfg.radarRefreshMinutes = integer(p, "radarRefreshMinutes", 5);
            cfg.trafficRefreshMinutes = integer(p, "trafficRefreshMinutes", 5);
            cfg.sportsRefreshMinutes = Math.max(2, integer(p, "sportsRefreshMinutes", 5));
            cfg.liveSevereWeatherMode = bool(p, "liveSevereWeatherMode", false);
            cfg.automaticSevereWeatherMode = bool(p, "automaticSevereWeatherMode", true);
            cfg.autoDisableSevereWeatherMode = bool(p, "autoDisableSevereWeatherMode", true);
            cfg.mainShowcaseMediaEnabled = bool(p, "mainShowcaseMediaEnabled", false);
            cfg.mainShowcaseIntervalSeconds = Math.max(5, Math.min(600,
                    integer(p, "mainShowcaseIntervalSeconds", 30)));
            cfg.severeWeatherMapPriority = bool(p, "severeWeatherMapPriority", true);
            cfg.themeOverlayEffects = bool(p, "themeOverlayEffects", true);
            cfg.overlayIntensity = p.getProperty("overlayIntensity", "LOW").trim().toUpperCase();
            cfg.celebrationsEnabled = bool(p, "celebrationsEnabled", true);
            cfg.celebrationMediaDirectory = Path.of(p.getProperty(
                    "celebrationMediaDirectory",
                    cfg.celebrationMediaDirectory.toString()));
            Files.createDirectories(cfg.celebrationMediaDirectory);
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

            cfg.sports.clear();
            int sc = integer(p, "sports.count", 2);
            AppConfig defaultsForSports = new AppConfig();
            for (int i = 0; i < sc; i++) {
                SportsConfig fallback = i < defaultsForSports.sports.size()
                        ? defaultsForSports.sports.get(i)
                        : new SportsConfig("Sports " + (i + 1), "American Football", "", "", "", true);
                String prefix="sports."+i;
                cfg.sports.add(new SportsConfig(
                        p.getProperty(prefix+".name", fallback.name()),
                        p.getProperty(prefix+".sport", fallback.sport()),
                        p.getProperty(prefix+".leagueId", fallback.leagueId()),
                        p.getProperty(prefix+".teamId", fallback.teamId()),
                        p.getProperty(prefix+".teamName", fallback.teamName()),
                        bool(p, prefix+".showLogos", fallback.showLogos())
                ));
            }

            cfg.celebrations.clear();
            int cc=integer(p,"celebrations.count",0);
            for(int i=0;i<cc;i++){
                String prefix="celebration."+i;
                String hire=p.getProperty(prefix+".hireDate","").trim();
                LocalDate hireDate=null;
                if(!hire.isBlank()){
                    try{hireDate=LocalDate.parse(hire);}catch(Exception ignored){}
                }
                cfg.celebrations.add(new CelebrationConfig(
                        p.getProperty(prefix+".name","Team Member"),
                        integer(p,prefix+".birthdayMonth",0),
                        integer(p,prefix+".birthdayDay",0),
                        hireDate,
                        p.getProperty(prefix+".photoPath",""),
                        bool(p,prefix+".showBirthday",true),
                        bool(p,prefix+".showAnniversary",true),
                        bool(p,prefix+".celebrationEffect",true),
                        bool(p,prefix+".enabled",true)
                ));
            }

            cfg.widgetTypes.clear();
            int wc = integer(p, "widgets.count", 6);
            for (int i = 0; i < wc; i++) cfg.widgetTypes.add(p.getProperty("widget." + i, "STATUS"));

            ApiCredentialService.loadInto(cfg);
            if ((cfg.tomTomApiKey == null || cfg.tomTomApiKey.isBlank()) && !legacyTomTomKey.isBlank()) {
                cfg.tomTomApiKey = legacyTomTomKey;
                ApiCredentialService.saveFrom(cfg);
            }
        } catch (Exception ex) {
            System.err.println("Configuration load failed; using defaults: " + ex.getMessage());
        }
        return cfg;
    }

    public static void save(AppConfig cfg) {
        try {
            Files.createDirectories(appDataDir());
            Files.createDirectories(cfg.mediaDirectory);
            Files.createDirectories(cfg.celebrationMediaDirectory);
            Properties p = new Properties();
            p.setProperty("fullscreen", Boolean.toString(cfg.fullscreen));
            p.setProperty("themeId", cfg.themeId);
            p.setProperty("automaticHolidayThemes",Boolean.toString(cfg.automaticHolidayThemes));
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
            p.setProperty("themeOverlayEffects", Boolean.toString(cfg.themeOverlayEffects));
            p.setProperty("overlayIntensity", cfg.overlayIntensity);
            p.setProperty("celebrationsEnabled", Boolean.toString(cfg.celebrationsEnabled));
            p.setProperty("celebrationMediaDirectory", cfg.celebrationMediaDirectory.toString());
            p.setProperty("headerText", cfg.headerText);
            p.setProperty("tickerText", cfg.tickerText);
            p.setProperty("weatherProvider", cfg.weatherProvider);
            p.setProperty("alertProvider", cfg.alertProvider);
            p.setProperty("radarProvider", cfg.radarProvider);
            p.setProperty("trafficProvider", cfg.trafficProvider);
            p.setProperty("sportsProvider", cfg.sportsProvider);
            p.setProperty("sportsPremiumLiveScores", Boolean.toString(cfg.sportsPremiumLiveScores));
            p.setProperty("nwsUserAgent", cfg.nwsUserAgent);
            p.setProperty("weatherRefreshMinutes", Integer.toString(cfg.weatherRefreshMinutes));
            p.setProperty("alertRefreshMinutes", Integer.toString(cfg.alertRefreshMinutes));
            p.setProperty("radarRefreshMinutes", Integer.toString(cfg.radarRefreshMinutes));
            p.setProperty("trafficRefreshMinutes", Integer.toString(cfg.trafficRefreshMinutes));
            p.setProperty("sportsRefreshMinutes", Integer.toString(cfg.sportsRefreshMinutes));
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

            p.setProperty("sports.count", Integer.toString(cfg.sports.size()));
            for (int i = 0; i < cfg.sports.size(); i++) {
                SportsConfig sport = cfg.sports.get(i);
                String prefix="sports."+i;
                p.setProperty(prefix+".name", sport.name());
                p.setProperty(prefix+".sport", sport.sport());
                p.setProperty(prefix+".leagueId", sport.leagueId());
                p.setProperty(prefix+".teamId", sport.teamId());
                p.setProperty(prefix+".teamName", sport.teamName());
                p.setProperty(prefix+".showLogos", Boolean.toString(sport.showLogos()));
            }

            p.setProperty("celebrations.count",Integer.toString(cfg.celebrations.size()));
            for(int i=0;i<cfg.celebrations.size();i++){
                CelebrationConfig c=cfg.celebrations.get(i);
                String prefix="celebration."+i;
                p.setProperty(prefix+".name",c.name()==null?"":c.name());
                p.setProperty(prefix+".birthdayMonth",Integer.toString(c.birthdayMonth()));
                p.setProperty(prefix+".birthdayDay",Integer.toString(c.birthdayDay()));
                p.setProperty(prefix+".hireDate",c.hireDate()==null?"":c.hireDate().toString());
                p.setProperty(prefix+".photoPath",c.photoPath()==null?"":c.photoPath());
                p.setProperty(prefix+".showBirthday",Boolean.toString(c.showBirthday()));
                p.setProperty(prefix+".showAnniversary",Boolean.toString(c.showAnniversary()));
                p.setProperty(prefix+".celebrationEffect",Boolean.toString(c.celebrationEffect()));
                p.setProperty(prefix+".enabled",Boolean.toString(c.enabled()));
            }

            p.setProperty("widgets.count", Integer.toString(cfg.widgetTypes.size()));
            for (int i = 0; i < cfg.widgetTypes.size(); i++) p.setProperty("widget." + i, cfg.widgetTypes.get(i));

            try (OutputStream out = Files.newOutputStream(appDataDir().resolve(FILE_NAME))) {
                p.store(out, "Weather & Traffic Monitor configuration");
            }
            ApiCredentialService.saveFrom(cfg);
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
