package com.wtm.ui;

import com.wtm.alerts.NwsAlertService;
import com.wtm.config.*;
import com.wtm.map.TileMapPanel;
import com.wtm.model.*;
import com.wtm.net.HttpService;
import com.wtm.radar.RainViewerService;
import com.wtm.traffic.TomTomService;
import com.wtm.weather.OpenMeteoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

/** Main fullscreen operations display. All external data refreshes run off the Swing EDT. */
public final class DashboardFrame extends JFrame {
    private AppConfig config;
    private final HttpService http = new HttpService();
    private final OpenMeteoService weatherService = new OpenMeteoService(http);
    private final NwsAlertService alertService = new NwsAlertService(http);
    private final RainViewerService radarService = new RainViewerService(http);
    private final TomTomService trafficService = new TomTomService(http);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4, r -> {
        Thread t=new Thread(r,"dashboard-refresh");
        t.setDaemon(true);
        return t;
    });

    /*
     * Keep handles to each repeating job so Settings can change refresh rates
     * immediately without restarting the application.
     */
    private ScheduledFuture<?> weatherTask;
    private ScheduledFuture<?> alertTask;
    private ScheduledFuture<?> radarTask;
    private ScheduledFuture<?> trafficTask;
    private ScheduledFuture<?> mediaTask;

    /**
     * Runtime state set only by automatic NWS alert detection. It remains
     * separate from the user's manual live-mode checkbox.
     */
    private volatile boolean automaticLiveWeatherActive = false;

    private TileMapPanel map;
    private MainShowcasePanel mainShowcase;
    private JPanel root, headerPanel, forecastStrip, sidePanel, tickerPanel, severePanel;
    private JLabel headerLabel, clockLabel, tickerLabel, severeLabel;
    private int tickerX=0;
    private final javax.swing.Timer tickerTimer;
    private final Map<String,WeatherSnapshot> weather = new ConcurrentHashMap<>();
    private final Map<Integer,RouteStatus> routes = new ConcurrentHashMap<>();
    private volatile List<WeatherAlert> alerts=List.of();
    private volatile RadarFrame radarFrame;
    private volatile Instant lastSuccess;
    private final Map<Integer,Integer> mediaIndex = new ConcurrentHashMap<>();

    public DashboardFrame(AppConfig config){
        super("Weather & Traffic Monitor"); this.config=config;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1200,700));
        setSize(1600,900); setLocationRelativeTo(null);
        tickerTimer=new javax.swing.Timer(30,e->advanceTicker());
        buildUi();
        addKeyListener(new KeyAdapter(){@Override public void keyPressed(KeyEvent e){if(e.getKeyCode()==KeyEvent.VK_F11)toggleFullscreen(); if(e.getKeyCode()==KeyEvent.VK_ESCAPE && !config.fullscreen)setExtendedState(JFrame.NORMAL);}});
        startRefreshers();
    }

    private void buildUi(){
        getContentPane().removeAll();
        final int gap=14;
        root=new JPanel(new BorderLayout(gap,gap));
        root.setBorder(new EmptyBorder(12,12,12,12));
        getContentPane().add(root);
        severePanel=new JPanel(new BorderLayout());severeLabel=new JLabel("",SwingConstants.CENTER);severeLabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,18));severePanel.add(severeLabel);severePanel.setVisible(false);
        JPanel north=new JPanel(new BorderLayout(0,14));
        north.setOpaque(false);
        north.add(severePanel,BorderLayout.NORTH);

        headerPanel=buildHeader();
        forecastStrip=buildForecastStrip();

        JPanel headBody=new JPanel(new BorderLayout(0,14));
        headBody.setOpaque(false);
        if(config.showHeader) headBody.add(headerPanel,BorderLayout.NORTH);
        headBody.add(forecastStrip,BorderLayout.CENTER);
        north.add(headBody,BorderLayout.CENTER);
        root.add(north,BorderLayout.NORTH);

        map=new TileMapPanel(config,http);
        mainShowcase=new MainShowcasePanel(config,map);
        mainShowcase.setAutomaticSevereWeatherActive(automaticLiveWeatherActive);

        sidePanel=new JPanel(sideGridLayout());
        sidePanel.setOpaque(false);
        sidePanel.setMinimumSize(new Dimension(0,0));
        rebuildSideWidgets();

        /*
         * FixedRatioLayout gives the map the user-selected share of the
         * dashboard. The ratio is controlled only through Settings, so card
         * preferred/minimum sizes cannot alter it and viewers cannot drag it
         * accidentally during normal operation.
         */
        JPanel dashboardBody=new JPanel(new FixedRatioLayout(config.mapWidthPercent / 100.0,14));
        dashboardBody.setOpaque(false);
        dashboardBody.add(mainShowcase);
        dashboardBody.add(sidePanel);

        root.add(dashboardBody,BorderLayout.CENTER);
        tickerPanel=buildTicker();if(config.showTicker)root.add(tickerPanel,BorderLayout.SOUTH);
        applyTheme();revalidate();repaint();
        if(config.fullscreen) SwingUtilities.invokeLater(() -> setExtendedState(JFrame.MAXIMIZED_BOTH));
    }

    /**
     * Chooses a balanced information-card grid for the requested block count.
     * Ten/twelve cards use three columns to take advantage of large displays.
     */
    private LayoutManager sideGridLayout(){
        int count=Math.max(6,Math.min(12,config.visibleWidgetCount));
        int cols=count>=10?3:2;
        int rows=(int)Math.ceil(count/(double)cols);
        return new GridLayout(rows,cols,14,14);
    }

    private JPanel buildHeader(){
        RoundedPanel p=new RoundedPanel(18);
        p.putClientProperty("surfaceRole","header");
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(13,18,13,12));

        headerLabel=new JLabel(config.headerText);
        headerLabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,24));

        clockLabel=new JLabel();
        clockLabel.setFont(new Font(Font.MONOSPACED,Font.BOLD,16));

        JButton settings=new JButton("⚙");
        settings.setToolTipText("Settings");
        settings.setFont(settings.getFont().deriveFont(20f));
        settings.setPreferredSize(new Dimension(48,34));
        settings.setFocusable(false);
        settings.setBorderPainted(true);
        settings.setContentAreaFilled(false);
        settings.addActionListener(e->new SettingsDialog(this,config,this::applyConfig).setVisible(true));

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));
        right.setOpaque(false);
        right.add(clockLabel);
        right.add(settings);

        p.add(headerLabel,BorderLayout.WEST);
        p.add(right,BorderLayout.EAST);

        new javax.swing.Timer(1000,e->clockLabel.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM d  •  h:mm:ss a"))
        )).start();
        return p;
    }

    private JPanel buildForecastStrip(){
        // Keep forecast cards visually independent, especially in light mode where white
        // cards can otherwise appear to merge into a single continuous strip.
        int count=Math.max(1,config.monitored.size());
        int columns=Math.min(5,count);
        int rows=(int)Math.ceil(count/(double)columns);
        JPanel p=new JPanel(new GridLayout(rows,columns,14,14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(3,0,5,0));
        for(Location l:config.monitored)p.add(forecastCard(l));
        return p;
    }
    private JPanel forecastCard(Location l){
        RoundedPanel p=new RoundedPanel(18);
        p.setLayout(new BorderLayout(14,0));
        p.setBorder(new EmptyBorder(12,16,12,16));
        p.putClientProperty("forecastName",l.name());
        p.putClientProperty("surfaceRole","forecast");

        JLabel icon=new JLabel(new DashboardIcon(DashboardIcon.Kind.CLOUD,46,Theme.accent()));
        icon.putClientProperty("forecastIcon",Boolean.TRUE);

        JPanel text=new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));

        JLabel name=new JLabel(l.name());
        name.putClientProperty("forecastNameLabel",Boolean.TRUE);
        name.setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));

        JLabel data=new JLabel("Loading weather...");
        data.putClientProperty("forecastData",Boolean.TRUE);
        data.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));

        text.add(name);
        text.add(Box.createVerticalStrut(3));
        text.add(data);

        p.add(icon,BorderLayout.WEST);
        p.add(text,BorderLayout.CENTER);
        return p;
    }

    /**
     * Returns the message currently intended for the bottom ticker.
     *
     * AUTO LIVE severe-weather mode temporarily takes priority over the site's
     * normal announcement ticker so viewers can immediately tell that the
     * system switched into rapid weather monitoring. When AUTO LIVE clears,
     * the configured site ticker automatically returns.
     */
    private String currentTickerText(){
        if(automaticLiveWeatherActive){
            String event=qualifyingSevereAlertName();
            if(event==null || event.isBlank()){
                return "⚠ AUTO LIVE SEVERE WEATHER MODE ACTIVE • Rapid weather, radar, and NWS alert monitoring enabled";
            }
            return "⚠ AUTO LIVE SEVERE WEATHER MODE ACTIVE • "
                    + event.toUpperCase()
                    + " • Live map priority and rapid weather monitoring enabled";
        }

        if(config.liveSevereWeatherMode){
            return "⚠ MANUAL LIVE WEATHER MODE ACTIVE • Rapid weather, radar, and NWS alert monitoring enabled";
        }

        return config.tickerText==null ? "" : config.tickerText;
    }

    private void refreshTickerMessage(){
        if(tickerLabel!=null){
            tickerLabel.setText("  "+currentTickerText()+"  ");
            tickerX=getWidth();
        }
    }

    private JPanel buildTicker(){
        RoundedPanel p=new RoundedPanel(16);
        p.putClientProperty("surfaceRole","ticker");
        p.setLayout(null);
        p.setPreferredSize(new Dimension(10,42));
        tickerLabel=new JLabel("  "+currentTickerText()+"  ");
        tickerLabel.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
        p.add(tickerLabel);
        tickerTimer.start();
        return p;
    }
    private void advanceTicker(){if(tickerPanel==null||tickerLabel==null||!config.showTicker)return;int w=tickerLabel.getPreferredSize().width;tickerX-=2;if(tickerX<-w)tickerX=tickerPanel.getWidth();tickerLabel.setBounds(tickerX,0,w,42);}

    private void rebuildSideWidgets(){
        if(sidePanel==null)return;
        sidePanel.removeAll();
        sidePanel.setLayout(sideGridLayout());
        int count=Math.max(6,Math.min(12,config.visibleWidgetCount));
        for(int i=0;i<count;i++){
            String type=i<config.widgetTypes.size()?config.widgetTypes.get(i):"STATUS";
            sidePanel.add(buildWidget(type,i));
        }
        sidePanel.revalidate();
        sidePanel.repaint();
    }
    private JPanel buildWidget(String type,int slot){
        RoundedPanel p=new RoundedPanel(18);
        p.setLayout(new BorderLayout(0,10));
        p.setBorder(new EmptyBorder(14,15,14,15));
        p.putClientProperty("widgetType",type);
        p.putClientProperty("slot",slot);
        p.putClientProperty("surfaceRole","widget");

        JLabel title=new JLabel(widgetTitle(type));
        title.putClientProperty("widgetTitle",Boolean.TRUE);
        title.setFont(new Font(Font.SANS_SERIF,Font.BOLD,13));

        JPanel content=new JPanel(new BorderLayout(12,0));
        content.setOpaque(false);

        JLabel icon=new JLabel(iconForWidget(type,null));
        icon.putClientProperty("widgetIcon",Boolean.TRUE);
        icon.setVerticalAlignment(SwingConstants.CENTER);

        JLabel body=new JLabel("<html>Loading…</html>");
        body.putClientProperty("widgetBody",Boolean.TRUE);
        body.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,14));

        content.add(icon,BorderLayout.WEST);
        content.add(body,BorderLayout.CENTER);

        p.add(title,BorderLayout.NORTH);
        p.add(content,BorderLayout.CENTER);

        if("MEDIA".equals(type)){
            content.removeAll();
            content.add(mediaComponent(slot),BorderLayout.CENTER);
        }
        return p;
    }

    private Icon iconForWidget(String type, RouteStatus route){
        if(type.startsWith("ROUTE_")){
            Color c=Theme.good();
            if(route!=null){
                if("HEAVY".equalsIgnoreCase(route.status())||"SEVERE".equalsIgnoreCase(route.status())) c=Theme.danger();
                else if("MODERATE".equalsIgnoreCase(route.status())) c=Theme.warn();
            }
            return new DashboardIcon(DashboardIcon.Kind.CAR,44,c);
        }

        WeatherSnapshot w=weatherForWidget(type);
        if("WEATHER_PRIMARY".equals(type) || type.startsWith("WEATHER_LOCATION_")){
            DashboardIcon.Kind kind=DashboardIcon.weatherKind(w==null?"":w.condition());
            return new DashboardIcon(kind,46,Theme.accent());
        }
        if("ALERTS".equals(type)) return new DashboardIcon(DashboardIcon.Kind.ALERT,42,Theme.danger());
        if("FORECAST_PRIMARY".equals(type)) return new DashboardIcon(DashboardIcon.Kind.CLOCK,42,Theme.accent());
        if("WIND_PRIMARY".equals(type)) return new DashboardIcon(DashboardIcon.Kind.WIND,42,Theme.accent());
        if("MEDIA".equals(type)) return new DashboardIcon(DashboardIcon.Kind.MEDIA,42,Theme.accent());
        return new DashboardIcon(DashboardIcon.Kind.STATUS,42,Theme.good());
    }

    private String widgetTitle(String type){
        if(type.startsWith("ROUTE_")){
            int i=parseRouteIndex(type);
            return i>=0&&i<config.routes.size()?"ROUTE • "+config.routes.get(i).name().toUpperCase():"ROUTE";
        }
        if(type.startsWith("WEATHER_LOCATION_")){
            int i=parseLocationIndex(type);
            return i>=0&&i<config.monitored.size()
                    ? config.monitored.get(i).name().toUpperCase()+" WEATHER"
                    : "LOCATION WEATHER";
        }
        return switch(type){
            case "WEATHER_PRIMARY"->config.primary.name().toUpperCase()+" WEATHER";
            case "ALERTS"->"SEVERE WEATHER";
            case "FORECAST_PRIMARY"->"HOURLY OUTLOOK";
            case "WIND_PRIMARY"->"WIND & GUSTS";
            case "MEDIA"->"ANNOUNCEMENTS";
            default->"SYSTEM STATUS";
        };
    }

    private JComponent mediaComponent(int slot){
        JLabel image=new JLabel("No media files",SwingConstants.CENTER);
        image.setHorizontalTextPosition(SwingConstants.CENTER);
        image.setVerticalTextPosition(SwingConstants.BOTTOM);
        image.putClientProperty("mediaSlot",slot);
        return image;
    }

    private JLabel findWidgetBody(Container c){
        for(Component child:c.getComponents()){
            if(child instanceof JLabel l && Boolean.TRUE.equals(l.getClientProperty("widgetBody"))) return l;
            if(child instanceof Container nested){
                JLabel found=findWidgetBody(nested);
                if(found!=null) return found;
            }
        }
        return null;
    }

    private JLabel findWidgetIcon(Container c){
        for(Component child:c.getComponents()){
            if(child instanceof JLabel l && Boolean.TRUE.equals(l.getClientProperty("widgetIcon"))) return l;
            if(child instanceof Container nested){
                JLabel found=findWidgetIcon(nested);
                if(found!=null) return found;
            }
        }
        return null;
    }

    private void updateWidgets(){if(sidePanel==null)return;for(Component c:sidePanel.getComponents()){if(!(c instanceof JPanel p))continue;String type=(String)((JComponent)p).getClientProperty("widgetType");if(type==null)continue;JLabel body=findWidgetBody(p);if(body==null)continue;
            if(type.startsWith("ROUTE_")){
                int i=parseRouteIndex(type);
                RouteStatus r=(i>=0&&i<config.routes.size())?routes.get(i):null;
                body.setText(routeHtml(r));
                JLabel icon=findWidgetIcon(p);
                if(icon!=null)icon.setIcon(iconForWidget(type,r));
            }
            else if("WEATHER_PRIMARY".equals(type) || type.startsWith("WEATHER_LOCATION_")){
                body.setText(weatherHtml(weatherForWidget(type)));
                JLabel icon=findWidgetIcon(p);
                if(icon!=null)icon.setIcon(iconForWidget(type,null));
            }
            else if("ALERTS".equals(type))body.setText(alertHtml());
            else if("FORECAST_PRIMARY".equals(type))body.setText(hourlyHtml(weather.get(config.primary.name())));
            else if("WIND_PRIMARY".equals(type)){WeatherSnapshot w=weather.get(config.primary.name());body.setText(w==null?"<html>Loading…</html>":"<html><b>"+Math.round(w.windMph())+" mph</b> sustained<br>Gusts up to <b>"+Math.round(w.gustMph())+" mph</b></html>");}
            else if("STATUS".equals(type))body.setText(statusHtml());
        }
        refreshMedia();
    }

    private WeatherSnapshot weatherForWidget(String type){
        if(type.startsWith("WEATHER_LOCATION_")){
            int i=parseLocationIndex(type);
            if(i>=0&&i<config.monitored.size())
                return weather.get(config.monitored.get(i).name());
        }
        return weather.get(config.primary.name());
    }

    private String weatherHtml(WeatherSnapshot w){
        if(w==null)return "<html>Loading weather…</html>";
        return "<html><span style='font-size:22px'><b>"+Math.round(w.temperatureF())+"°F</b></span>"
                +"<br>"+w.condition()
                +"<br>Feels like "+Math.round(w.apparentTemperatureF())+"°"
                +"<br>High "+Math.round(w.highF())+"° / Low "+Math.round(w.lowF())+"°"
                +"<br>Rain chance "+Math.round(w.precipitationProbability())+"%</html>";
    }
    private String routeHtml(RouteStatus r){
        if(r==null)return "<html>Loading route…</html>";
        if(r.travelMinutes()<0)return "<html><b>Traffic unavailable</b><br>Check TomTom configuration.</html>";
        Color sc=Theme.good();
        if("MODERATE".equalsIgnoreCase(r.status())) sc=Theme.warn();
        if("HEAVY".equalsIgnoreCase(r.status())||"SEVERE".equalsIgnoreCase(r.status())) sc=Theme.danger();
        return "<html><span style='font-size:21px'><b>"+r.travelMinutes()+" min</b></span>"
                +"<br>Normal: "+r.noTrafficMinutes()+" min"
                +"<br>Delay: +"+r.delayMinutes()+" min"
                +"<br><font color='"+Theme.hex(sc)+"'><b>"+r.status()+"</b></font></html>";
    }
    private String alertHtml(){if(alerts.isEmpty())return "<html><b>No active NWS alerts</b><br>for the primary location.</html>";WeatherAlert a=alerts.get(0);String exp=a.expires()==null?"":a.expires().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a"));return "<html><b>⚠ "+escape(a.event())+"</b><br>"+escape(shorten(a.headline(),115))+"<br>Expires "+exp+"</html>";}
    private String hourlyHtml(WeatherSnapshot w){if(w==null||w.hourly().isEmpty())return "<html>Loading forecast…</html>";StringBuilder b=new StringBuilder("<html>");for(int i=0;i<Math.min(4,w.hourly().size());i++){WeatherSnapshot.HourlyPoint h=w.hourly().get(i);String t=h.time().length()>=16?h.time().substring(11,16):h.time();b.append("<b>").append(t).append("</b> • ").append(Math.round(h.temperatureF())).append("° • rain ").append(Math.round(h.precipitationProbability())).append("%<br>");}return b.append("</html>").toString();}
    private String statusHtml(){
        String last=lastSuccess==null
                ?"Waiting for first update"
                :DateTimeFormatter.ofPattern("h:mm:ss a").format(lastSuccess.atZone(ZoneId.systemDefault()));

        String traffic=(config.tomTomApiKey==null||config.tomTomApiKey.isBlank())
                ?"Traffic API: not configured"
                :"Traffic API: configured";

        String monitoring;
        if(config.liveSevereWeatherMode){
            monitoring="Weather monitoring: MANUAL LIVE";
        }else if(automaticLiveWeatherActive){
            String event=qualifyingSevereAlertName();
            monitoring="Weather monitoring: AUTO LIVE"
                    +(event.isBlank()?"":" ("+escape(event)+")");
        }else if(config.automaticSevereWeatherMode){
            monitoring="Weather monitoring: NORMAL • auto severe enabled";
        }else{
            monitoring="Weather monitoring: NORMAL";
        }

        return "<html><b>Data services running</b>"
                +"<br>"+monitoring
                +"<br>Last good update: "+last
                +"<br>"+traffic
                +"<br>Config: "+ConfigService.appDataDir()
                +"</html>";
    }

    private void updateForecastStrip(){
        for(Component c:forecastStrip.getComponents()){
            if(!(c instanceof JPanel p))continue;
            String name=(String)((JComponent)p).getClientProperty("forecastName");
            WeatherSnapshot w=weather.get(name);
            if(w==null)continue;

            for(Component cc:allComponents(p)){
                if(cc instanceof JLabel l && Boolean.TRUE.equals(l.getClientProperty("forecastData"))){
                    l.setText("<html><span style='font-size:16px'><b>"+Math.round(w.temperatureF())+"°F</b></span>"
                            +"  •  "+w.condition()
                            +"<br>H "+Math.round(w.highF())+"° / L "+Math.round(w.lowF())+"°"
                            +"  •  Rain "+Math.round(w.precipitationProbability())+"%</html>");
                }
                if(cc instanceof JLabel l && Boolean.TRUE.equals(l.getClientProperty("forecastIcon"))){
                    l.setIcon(new DashboardIcon(DashboardIcon.weatherKind(w.condition()),46,Theme.accent()));
                }
            }
        }
    }

    private void refreshWeather(){for(Location l:uniqueLocations()){try{WeatherSnapshot w=weatherService.fetch(l);weather.put(l.name(),w);lastSuccess=Instant.now();}catch(Exception ex){System.err.println("Weather refresh failed for "+l.name()+": "+ex.getMessage());}}SwingUtilities.invokeLater(()->{updateForecastStrip();updateWidgets();});}
    private List<Location> uniqueLocations(){LinkedHashMap<String,Location> m=new LinkedHashMap<>();m.put(config.primary.name(),config.primary);for(Location l:config.monitored)m.put(l.name(),l);return new ArrayList<>(m.values());}
    private void refreshAlerts(){
        try{
            alerts=alertService.fetch(config.primary);
            lastSuccess=Instant.now();

            evaluateAutomaticSevereWeatherMode();

            SwingUtilities.invokeLater(()->{
                map.setAlerts(alerts);
                if(mainShowcase!=null)
                    mainShowcase.setAutomaticSevereWeatherActive(automaticLiveWeatherActive);
                refreshTickerMessage();
                updateSevereBanner();
                updateWidgets();
            });
        }catch(Exception ex){
            System.err.println("NWS alerts failed: "+ex.getMessage());
        }
    }

    private boolean hasQualifyingSevereAlert(List<WeatherAlert> currentAlerts){
        if(currentAlerts==null || currentAlerts.isEmpty()) return false;

        for(WeatherAlert alert:currentAlerts){
            String event=alert.event()==null?"":alert.event().trim().toLowerCase();

            if(event.contains("tornado warning")
                    || event.contains("tornado watch")
                    || event.contains("tornado emergency")
                    || event.contains("severe thunderstorm warning")
                    || event.contains("severe thunderstorm watch")
                    || event.contains("flash flood warning")
                    || event.contains("extreme wind warning")){
                return true;
            }

            if("extreme".equalsIgnoreCase(alert.severity())) return true;
        }
        return false;
    }

    private String qualifyingSevereAlertName(){
        if(alerts==null) return "";
        for(WeatherAlert alert:alerts){
            if(hasQualifyingSevereAlert(List.of(alert)))
                return alert.event()==null?"Severe weather":alert.event();
        }
        return "";
    }

    /**
     * Automatic mode only manages the runtime automatic flag. It never edits
     * the manual Live Severe Weather Mode checkbox.
     */
    private void evaluateAutomaticSevereWeatherMode(){
        if(!config.automaticSevereWeatherMode){
            if(automaticLiveWeatherActive){
                automaticLiveWeatherActive=false;
                startRefreshers();
            }
            return;
        }

        boolean severeNow=hasQualifyingSevereAlert(alerts);

        if(severeNow && !automaticLiveWeatherActive){
            automaticLiveWeatherActive=true;
            SwingUtilities.invokeLater(()->{
                if(mainShowcase!=null) mainShowcase.setAutomaticSevereWeatherActive(true);
                refreshTickerMessage();
            });
            System.out.println("Automatic severe-weather mode enabled: "+qualifyingSevereAlertName());
            startRefreshers();
            return;
        }

        if(!severeNow && automaticLiveWeatherActive && config.autoDisableSevereWeatherMode){
            automaticLiveWeatherActive=false;
            SwingUtilities.invokeLater(()->{
                if(mainShowcase!=null) mainShowcase.setAutomaticSevereWeatherActive(false);
                refreshTickerMessage();
            });
            System.out.println("Automatic severe-weather mode cleared; returning to normal refresh rates.");
            startRefreshers();
        }
    }
    private void refreshRadar(){try{radarFrame=radarService.latest();lastSuccess=Instant.now();SwingUtilities.invokeLater(()->map.setRadarFrame(radarFrame));}catch(Exception ex){System.err.println("Radar metadata failed: "+ex.getMessage());}}
    private void refreshTraffic(){for(int i=0;i<config.routes.size();i++){try{routes.put(i,trafficService.fetchRoute(config.routes.get(i),config.tomTomApiKey));lastSuccess=Instant.now();}catch(Exception ex){routes.put(i,new RouteStatus(config.routes.get(i).name(),-1,-1,0,"UNAVAILABLE",Instant.now()));System.err.println("Route refresh failed: "+ex.getMessage());}}SwingUtilities.invokeLater(this::updateWidgets);}

    private void updateSevereBanner(){WeatherAlert critical=alerts.stream().filter(a->"Extreme".equalsIgnoreCase(a.severity())||a.event().toLowerCase().contains("tornado warning")||a.event().toLowerCase().contains("severe thunderstorm warning")).findFirst().orElse(null);if(critical==null){severePanel.setVisible(false);}else{severeLabel.setText("⚠  "+critical.event().toUpperCase()+"  •  "+shorten(critical.headline(),160));severePanel.setVisible(true);}revalidate();}

    /**
     * Starts or restarts all repeating data jobs using the current settings.
     *
     * Existing ScheduledFuture instances are cancelled first. This is what
     * makes refresh-rate changes from Settings take effect immediately rather
     * than waiting for the application to restart.
     */
    private synchronized void startRefreshers(){
        cancelTask(weatherTask);
        cancelTask(alertTask);
        cancelTask(radarTask);
        cancelTask(trafficTask);
        cancelTask(mediaTask);

        weatherTask=scheduler.scheduleAtFixedRate(
                this::refreshWeather,
                0,
                effectiveWeatherRefreshMinutes(),
                TimeUnit.MINUTES);

        alertTask=scheduler.scheduleAtFixedRate(
                this::refreshAlerts,
                0,
                effectiveAlertRefreshMinutes(),
                TimeUnit.MINUTES);

        radarTask=scheduler.scheduleAtFixedRate(
                this::refreshRadar,
                0,
                effectiveRadarRefreshMinutes(),
                TimeUnit.MINUTES);

        trafficTask=scheduler.scheduleAtFixedRate(
                this::refreshTraffic,
                0,
                Math.max(2,config.trafficRefreshMinutes),
                TimeUnit.MINUTES);

        mediaTask=scheduler.scheduleAtFixedRate(
                ()->SwingUtilities.invokeLater(this::refreshMedia),
                8,
                12,
                TimeUnit.SECONDS);
    }

    /**
     * Live severe-weather mode intentionally leaves TomTom traffic/routing on
     * its normal schedule. Only meteorological services are accelerated.
     */
    private boolean isLiveSevereWeatherActive(){
        return config.liveSevereWeatherMode || automaticLiveWeatherActive;
    }

    private int effectiveWeatherRefreshMinutes(){
        return isLiveSevereWeatherActive() ? 2 : Math.max(2,config.weatherRefreshMinutes);
    }

    private int effectiveRadarRefreshMinutes(){
        return isLiveSevereWeatherActive() ? 2 : Math.max(2,config.radarRefreshMinutes);
    }

    private int effectiveAlertRefreshMinutes(){
        return isLiveSevereWeatherActive() ? 1 : Math.max(1,config.alertRefreshMinutes);
    }

    private static void cancelTask(ScheduledFuture<?> task){
        if(task!=null) task.cancel(false);
    }

    private JLabel findMediaLabel(Container c){
        for(Component child:c.getComponents()){
            if(child instanceof JLabel l && l.getClientProperty("mediaSlot")!=null) return l;
            if(child instanceof Container nested){
                JLabel found=findMediaLabel(nested);
                if(found!=null)return found;
            }
        }
        return null;
    }

    private void refreshMedia(){if(sidePanel==null)return;for(Component c:sidePanel.getComponents()){if(!(c instanceof JPanel p))continue;if(!"MEDIA".equals(((JComponent)p).getClientProperty("widgetType")))continue;int slot=(int)((JComponent)p).getClientProperty("slot");JLabel label=findMediaLabel(p);if(label==null)continue;File[] files=config.mediaDirectory.toFile().listFiles(f->{String n=f.getName().toLowerCase();return n.endsWith(".png")||n.endsWith(".jpg")||n.endsWith(".jpeg")||n.endsWith(".gif");});if(files==null||files.length==0){label.setIcon(null);label.setText("Place PNG/JPG/GIF files in\n"+config.mediaDirectory);continue;}Arrays.sort(files);int idx=mediaIndex.merge(slot,1,(a,b)->(a+b)%files.length)%files.length;try{ImageIcon raw=new ImageIcon(files[idx].getAbsolutePath());int w=Math.max(120,p.getWidth()-24),h=Math.max(80,p.getHeight()-45);Image scaled=raw.getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);label.setIcon(new ImageIcon(scaled));label.setText("");}catch(Exception ignored){}}}

    private void applyConfig(AppConfig c){
        this.config=c;

        /*
         * Reconcile automatic state immediately whenever Settings are saved.
         * Disabling the automatic feature always clears its runtime live state.
         */
        if(!c.automaticSevereWeatherMode){
            automaticLiveWeatherActive=false;
        }else if(hasQualifyingSevereAlert(alerts)){
            automaticLiveWeatherActive=true;
        }else if(c.autoDisableSevereWeatherMode){
            automaticLiveWeatherActive=false;
        }

        buildUi();
        refreshTickerMessage();
        startRefreshers();

        if(c.fullscreen) setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    private void applyTheme(){
        boolean d=config.darkMode;
        Color bg=Theme.bg(d);
        Color text=Theme.text(d);
        Color muted=Theme.muted(d);
        Color border=Theme.border(d);

        root.setBackground(bg);

        for(Component c:allComponents(root)){
            if(c instanceof RoundedPanel rp){
                Object role=rp.getClientProperty("surfaceRole");
                if("forecast".equals(role)) rp.setBackground(Theme.panel2(d));
                else rp.setBackground(Theme.panel(d));
                rp.putClientProperty("outlineColor",border);
            }

            if(c instanceof JLabel l){
                l.setForeground(text);
                if(Boolean.TRUE.equals(l.getClientProperty("widgetTitle"))) l.setForeground(text);
            }

            if(c instanceof JPanel p && !(p instanceof RoundedPanel)) p.setBackground(bg);

            if(c instanceof JButton b){
                b.setForeground(text);
                b.setBackground(Theme.panel2(d));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(border,1,true),
                        new EmptyBorder(4,10,4,10)
                ));
            }

        }

        severePanel.setBackground(Theme.danger());
        severeLabel.setForeground(Color.WHITE);

        if(tickerPanel!=null){
            tickerPanel.setBackground(Theme.panel(d));
            tickerPanel.putClientProperty("outlineColor",border);
        }

        repaint();
    }

    private List<Component> allComponents(Container root){List<Component> out=new ArrayList<>();for(Component c:root.getComponents()){out.add(c);if(c instanceof Container ct)out.addAll(allComponents(ct));}return out;}
    private void toggleFullscreen(){setExtendedState(getExtendedState()==JFrame.MAXIMIZED_BOTH?JFrame.NORMAL:JFrame.MAXIMIZED_BOTH);}
    private static int parseRouteIndex(String type){
        try{return Integer.parseInt(type.substring(type.indexOf('_')+1));}
        catch(Exception e){return -1;}
    }
    private static int parseLocationIndex(String type){
        try{return Integer.parseInt(type.substring("WEATHER_LOCATION_".length()));}
        catch(Exception e){return -1;}
    }
    private static String shorten(String s,int n){if(s==null)return "";return s.length()<=n?s:s.substring(0,n-1)+"…";}
    private static String escape(String s){return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
