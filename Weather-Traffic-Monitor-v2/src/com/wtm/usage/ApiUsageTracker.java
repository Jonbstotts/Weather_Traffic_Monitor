package com.wtm.usage;

import com.wtm.config.ConfigService;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local request accounting for every external API call made by this application.
 *
 * Important: this is installation-local usage. It cannot see requests made by
 * another device that happens to share the same provider API key.
 *
 * Counts are persisted to api-usage.properties so restarts do not reset usage.
 */
public final class ApiUsageTracker {
    private static final ApiUsageTracker INSTANCE=new ApiUsageTracker();
    private static final String FILE="api-usage.properties";

    private final Map<String,AtomicLong> counts=new ConcurrentHashMap<>();
    private String dayKey=currentDay();
    private String monthKey=currentMonth();
    private String minuteKey=currentMinute();

    private ApiUsageTracker(){ load(); }

    public static ApiUsageTracker get(){ return INSTANCE; }

    /**
     * Records one outbound HTTP request. Classification is domain/path based so
     * provider services do not need to manually increment counters.
     */
    public synchronized void record(String url){
        rollPeriodsIfNeeded();

        String key=classify(url);
        if(key==null) return;

        increment("lifetime."+key);
        increment("day."+dayKey+"."+key);
        increment("month."+monthKey+"."+key);
        increment("minute."+minuteKey+"."+key);
        saveQuietly();
    }

    public synchronized List<ApiUsageRecord> snapshot(boolean openMeteoCustomer, boolean sportsPremium){
        rollPeriodsIfNeeded();

        List<ApiUsageRecord> rows=new ArrayList<>();

        // TomTom's current free allowance is daily under its current pricing model.
        rows.add(row("TomTom","Traffic / map tiles","TOMTOM_TILE",50_000,"day",
                "Current TomTom free tile allowance."));
        rows.add(row("TomTom","Routing / other non-tile","TOMTOM_NON_TILE",2_500,"day",
                "Current TomTom free non-tile allowance."));

        if(openMeteoCustomer){
            rows.add(row("Open-Meteo","Weather / geocoding","OPEN_METEO",-1,"day",
                    "Customer API: no fixed daily limit published for the customer endpoint."));
        }else{
            rows.add(row("Open-Meteo","Weather / geocoding","OPEN_METEO",10_000,"day",
                    "Free non-commercial allowance."));
        }

        rows.add(row("National Weather Service","Alerts / weather.gov","NWS",-1,"day",
                "No public fixed quota; NWS recommends alert requests no more often than every 30 seconds."));

        rows.add(row("RainViewer","Radar metadata / tiles","RAINVIEWER",-1,"day",
                "No fixed public quota represented here; requests are tracked locally."));

        rows.add(row("TheSportsDB","Sports API","SPORTSDB",
                sportsPremium?100:30,"minute",
                sportsPremium?"Premium request-rate reference.":"Free request-rate reference."));

        return rows;
    }

    public synchronized long getLifetime(String classification){
        return value("lifetime."+classification);
    }

    public synchronized void resetLocalHistory(){
        counts.clear();
        dayKey=currentDay();
        monthKey=currentMonth();
        minuteKey=currentMinute();
        saveQuietly();
    }

    private ApiUsageRecord row(
            String provider,String category,String key,long limit,String period,String note
    ){
        long used=switch(period){
            case "minute" -> value("minute."+minuteKey+"."+key);
            case "month" -> value("month."+monthKey+"."+key);
            default -> value("day."+dayKey+"."+key);
        };
        return new ApiUsageRecord(provider,category,used,limit,period,note);
    }

    private String classify(String url){
        try{
            URI uri=URI.create(url);
            String host=Optional.ofNullable(uri.getHost()).orElse("").toLowerCase();
            String path=Optional.ofNullable(uri.getPath()).orElse("").toLowerCase();

            if(host.contains("tomtom")){
                if(path.contains("/tile/") || path.contains("/tiles/"))
                    return "TOMTOM_TILE";
                return "TOMTOM_NON_TILE";
            }
            if(host.contains("open-meteo.com")) return "OPEN_METEO";
            if(host.contains("weather.gov")) return "NWS";
            if(host.contains("rainviewer.com")) return "RAINVIEWER";
            if(host.contains("thesportsdb.com")) return "SPORTSDB";
        }catch(Exception ignored){}
        return null;
    }

    private void increment(String key){
        counts.computeIfAbsent(key,k->new AtomicLong()).incrementAndGet();
    }

    private long value(String key){
        AtomicLong v=counts.get(key);
        return v==null?0:v.get();
    }

    private void rollPeriodsIfNeeded(){
        String d=currentDay(),m=currentMonth(),min=currentMinute();
        dayKey=d; monthKey=m; minuteKey=min;

        // Keep the persistence file compact: retain lifetime, current day,
        // current month and current minute only.
        counts.keySet().removeIf(k->
                k.startsWith("day.")&&!k.startsWith("day."+dayKey+".")
                || k.startsWith("month.")&&!k.startsWith("month."+monthKey+".")
                || k.startsWith("minute.")&&!k.startsWith("minute."+minuteKey+"."));
    }

    private void load(){
        Path file=ConfigService.appDataDir().resolve(FILE);
        if(!Files.exists(file)) return;
        Properties p=new Properties();
        try(InputStream in=Files.newInputStream(file)){
            p.load(in);
            for(String name:p.stringPropertyNames()){
                try{
                    counts.put(name,new AtomicLong(Long.parseLong(p.getProperty(name))));
                }catch(Exception ignored){}
            }
        }catch(Exception ex){
            System.err.println("API usage history load failed: "+ex.getMessage());
        }
    }

    private void saveQuietly(){
        try{
            Files.createDirectories(ConfigService.appDataDir());
            Properties p=new Properties();
            for(var e:counts.entrySet())
                p.setProperty(e.getKey(),Long.toString(e.getValue().get()));
            try(OutputStream out=Files.newOutputStream(ConfigService.appDataDir().resolve(FILE))){
                p.store(out,"Weather & Traffic Monitor local API request accounting");
            }
        }catch(Exception ex){
            System.err.println("API usage history save failed: "+ex.getMessage());
        }
    }

    private static String currentDay(){
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }
    private static String currentMonth(){
        return YearMonth.now().toString().replace("-","");
    }
    private static String currentMinute(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
}
