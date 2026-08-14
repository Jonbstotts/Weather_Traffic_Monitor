package com.wtm.ui;

import com.wtm.config.*;
import com.wtm.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Settings remain separate from the passive TV dashboard.
 *
 * Locations and routes are table-driven so a facility can add or remove as
 * many pins and commute destinations as needed without changing source code.
 */
public final class SettingsDialog extends JDialog {
    private final AppConfig cfg;
    private final Consumer<AppConfig> onSave;

    private final JTextField header=new JTextField();
    private final JTextField ticker=new JTextField();
    private final JCheckBox showHeader=new JCheckBox("Show title/header");
    private final JCheckBox showTicker=new JCheckBox("Show scrolling ticker");
    private final JCheckBox fullscreen=new JCheckBox("Fullscreen on startup");
    private final JCheckBox dark=new JCheckBox("Dark mode");
    private final JCheckBox radar=new JCheckBox("Show radar layer");
    private final JCheckBox traffic=new JCheckBox("Show traffic layer");
    private final JCheckBox alertMap=new JCheckBox("Show severe-weather polygons on map");
    private final JCheckBox liveSevereWeather=new JCheckBox(
            "Manual Live Severe Weather Mode — rapid weather/radar/alert monitoring");
    private final JCheckBox automaticSevereWeather=new JCheckBox(
            "Automatically enable Live Severe Weather Mode for qualifying NWS alerts");
    private final JCheckBox autoDisableSevereWeather=new JCheckBox(
            "Automatically return to normal refresh rates after severe alerts clear");

    private final JTextField primaryName=new JTextField();
    private final JTextField primaryLat=new JTextField();
    private final JTextField primaryLon=new JTextField();

    private final JTextField tomTom=new JTextField();
    private final JTextField mediaDir=new JTextField();

    private final JCheckBox showcaseMedia=new JCheckBox(
            "Cycle company announcement media with the live map");
    private final JCheckBox severeMapPriority=new JCheckBox(
            "Keep live map persistent while Automatic Severe Weather Mode is active");
    private final JComboBox<Integer> showcaseInterval =
            new JComboBox<>(new Integer[]{10,15,20,30,45,60,90,120,180,300});

    /** Quick production refresh controls. Values are in minutes. */
    private final JComboBox<Integer> routeRefresh =
            new JComboBox<>(new Integer[]{2,5,10,15,20,30});
    private final JComboBox<Integer> weatherRefresh =
            new JComboBox<>(new Integer[]{5,10,15,20,30,60});
    private final JComboBox<Integer> radarRefresh =
            new JComboBox<>(new Integer[]{2,5,10,15});
    private final JComboBox<Integer> alertRefresh =
            new JComboBox<>(new Integer[]{1,2,5,10,15});

    private final DefaultTableModel locationModel = new DefaultTableModel(
            new Object[]{"Pinned location","Latitude","Longitude"},0);
    private final JTable locationTable = new JTable(locationModel);

    private final DefaultTableModel routeModel = new DefaultTableModel(
            new Object[]{"Route name","Destination","Latitude","Longitude"},0);
    private final JTable routeTable = new JTable(routeModel);

    private final JComboBox<Integer> blockCount =
            new JComboBox<>(new Integer[]{6,8,10,12});

    /**
     * Controlled map/card resizing. Unlike a draggable split pane, this value
     * changes only through Settings and remains locked during normal display.
     */
    private final JSlider mapWidthSlider = new JSlider(55,75,63);
    private final JLabel mapWidthValue = new JLabel("63% map / 37% information");
    private final JPanel widgetRows = new JPanel(new GridBagLayout());
    private final List<JComboBox<WidgetChoice>> widgetBoxes = new ArrayList<>();

    public SettingsDialog(JFrame owner, AppConfig cfg, Consumer<AppConfig> onSave){
        super(owner,"Weather & Traffic Monitor Settings",true);
        this.cfg=cfg;
        this.onSave=onSave;

        setSize(980,790);
        setMinimumSize(new Dimension(860,650));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("General",general());
        tabs.addTab("Pinned Locations",locations());
        tabs.addTab("Routes",routes());
        tabs.addTab("Dashboard Blocks",widgets());
        tabs.addTab("Main Showcase",showcase());
        tabs.addTab("Data & APIs",data());

        add(tabs,BorderLayout.CENTER);
        add(buttons(),BorderLayout.SOUTH);

        automaticSevereWeather.addActionListener(e->updateAutomaticSevereControls());

        loadValues();
        updateAutomaticSevereControls();
    }

    private JPanel general(){
        JPanel p=form();
        int y=0;
        addRow(p,y++,"Header text",header);
        addRow(p,y++,"Ticker text",ticker);
        addFull(p,y++,showHeader);
        addFull(p,y++,showTicker);
        addFull(p,y++,fullscreen);
        addFull(p,y++,dark);
        return p;
    }

    private JPanel locations(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JPanel primary=form();
        int y=0;
        JLabel title=new JLabel("Primary facility / map center");
        title.setFont(title.getFont().deriveFont(Font.BOLD,15f));
        addFull(primary,y++,title);
        addRow(primary,y++,"Primary location name",primaryName);
        addRow(primary,y++,"Primary latitude",primaryLat);
        addRow(primary,y++,"Primary longitude",primaryLon);

        JTextArea help=new JTextArea(
                "Pinned locations appear on the map and become available as weather cards. "
              + "Add as many locations as the facility needs. For the Vance site, Hoover and "
              + "Trussville are included by default in this release.");
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        help.setEditable(false);
        help.setOpaque(false);
        addFull(primary,y++,help);

        outer.add(primary,BorderLayout.NORTH);

        locationTable.setFillsViewportHeight(true);
        locationTable.setRowHeight(26);
        outer.add(new JScrollPane(locationTable),BorderLayout.CENTER);

        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("+ Add pinned location");
        add.addActionListener(e->locationModel.addRow(new Object[]{"New Location","",""}));
        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(locationTable,locationModel));
        controls.add(add);
        controls.add(remove);
        outer.add(controls,BorderLayout.SOUTH);
        return outer;
    }

    private JPanel routes(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JTextArea help=new JTextArea(
                "Routes originate at the Primary Location. Add any number of commute routes. "
              + "A route can use the coordinates of any pinned location, or another destination.");
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        help.setEditable(false);
        help.setOpaque(false);
        outer.add(help,BorderLayout.NORTH);

        routeTable.setFillsViewportHeight(true);
        routeTable.setRowHeight(26);
        outer.add(new JScrollPane(routeTable),BorderLayout.CENTER);

        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("+ Add route");
        add.addActionListener(e->routeModel.addRow(new Object[]{"New Route","Destination","",""}));
        JButton addFromPin=new JButton("+ Route from selected pin");
        addFromPin.addActionListener(e->addRouteFromPinnedLocation());
        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(routeTable,routeModel));
        controls.add(add);
        controls.add(addFromPin);
        controls.add(remove);
        outer.add(controls,BorderLayout.SOUTH);
        return outer;
    }

    private JPanel widgets(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JPanel controls=new JPanel();
        controls.setLayout(new BoxLayout(controls,BoxLayout.Y_AXIS));

        JPanel countRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        countRow.add(new JLabel("Visible information blocks:"));
        countRow.add(blockCount);
        countRow.add(new JLabel("Use 10–12 on large 1080p/4K displays."));
        controls.add(countRow);
        controls.add(Box.createVerticalStrut(14));

        RoundedPanel layoutCard=new RoundedPanel(16);
        layoutCard.setLayout(new BorderLayout(10,10));
        layoutCard.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));

        JPanel ratioTop=new JPanel(new BorderLayout());
        ratioTop.setOpaque(false);
        JLabel ratioTitle=new JLabel("Map / Information Layout");
        ratioTitle.setFont(ratioTitle.getFont().deriveFont(Font.BOLD,14f));
        ratioTop.add(ratioTitle,BorderLayout.WEST);
        ratioTop.add(mapWidthValue,BorderLayout.EAST);

        mapWidthSlider.setMajorTickSpacing(5);
        mapWidthSlider.setMinorTickSpacing(1);
        mapWidthSlider.setPaintTicks(true);
        mapWidthSlider.setSnapToTicks(true);
        mapWidthSlider.addChangeListener(e->updateMapWidthLabel());

        JPanel presets=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        presets.setOpaque(false);
        JButton infoFocused=new JButton("Information Focused 55/45");
        infoFocused.addActionListener(e->mapWidthSlider.setValue(55));
        JButton balanced=new JButton("Balanced 63/37");
        balanced.addActionListener(e->mapWidthSlider.setValue(63));
        JButton mapFocused=new JButton("Map Focused 70/30");
        mapFocused.addActionListener(e->mapWidthSlider.setValue(70));
        presets.add(infoFocused);
        presets.add(balanced);
        presets.add(mapFocused);

        JLabel ratioNote=new JLabel(
                "<html>The selected ratio stays locked during normal operation. "
              + "Change it here and choose <b>Save & Apply</b> to resize intentionally.</html>");

        layoutCard.add(ratioTop,BorderLayout.NORTH);
        layoutCard.add(mapWidthSlider,BorderLayout.CENTER);

        JPanel layoutBottom=new JPanel();
        layoutBottom.setOpaque(false);
        layoutBottom.setLayout(new BoxLayout(layoutBottom,BoxLayout.Y_AXIS));
        layoutBottom.add(presets);
        layoutBottom.add(Box.createVerticalStrut(7));
        layoutBottom.add(ratioNote);
        layoutCard.add(layoutBottom,BorderLayout.SOUTH);

        layoutCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(layoutCard);

        outer.add(controls,BorderLayout.NORTH);

        JScrollPane scroll=new JScrollPane(widgetRows);
        scroll.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        outer.add(scroll,BorderLayout.CENTER);

        blockCount.addActionListener(e->rebuildWidgetRows());
        return outer;
    }

    private void updateMapWidthLabel(){
        int map=mapWidthSlider.getValue();
        mapWidthValue.setText(map+"% map / "+(100-map)+"% information");
    }

    private JPanel showcase(){
        JPanel p=form();
        int y=0;

        JLabel title=new JLabel("Main Map / Announcement Showcase");
        title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
        addFull(p,y++,title);

        JTextArea intro=new JTextArea(
                "The large left-side region can remain a live map at all times or cycle "
              + "between the live map and company announcement images. The smaller Media "
              + "dashboard block remains independent.");
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        intro.setEditable(false);
        intro.setOpaque(false);
        addFull(p,y++,intro);

        addFull(p,y++,showcaseMedia);
        addRow(p,y++,"Showcase interval (seconds)",showcaseInterval);
        addRow(p,y++,"Announcement media folder",mediaDir);
        addFull(p,y++,severeMapPriority);

        JLabel priorityNote=new JLabel(
                "<html><b>Recommended:</b> leave map priority enabled. If Automatic Severe "
              + "Weather Mode enters <b>AUTO LIVE</b>, the showcase immediately returns to "
              + "the live map and pauses announcement rotation until the alert clears. "
              + "Disable only for troubleshooting or deliberate media-cycle testing.</html>");
        addFull(p,y++,priorityNote);

        JTextArea formats=new JTextArea(
                "Supported Main Showcase media: PNG, JPG, JPEG, and GIF. "
              + "Files are cycled in filename order.");
        formats.setLineWrap(true);
        formats.setWrapStyleWord(true);
        formats.setEditable(false);
        formats.setOpaque(false);
        addFull(p,y++,formats);

        return p;
    }

    private JPanel data(){
        JPanel p=form();
        int y=0;
        addFull(p,y++,radar);
        addFull(p,y++,traffic);
        addFull(p,y++,alertMap);
        addFull(p,y++,liveSevereWeather);
        addFull(p,y++,automaticSevereWeather);
        addFull(p,y++,autoDisableSevereWeather);

        JLabel liveNote=new JLabel(
                "<html><b>Live mode:</b> weather every 2 min • radar every 2 min • "
              + "NWS alerts every 1 min. Traffic/routing keeps its normal interval.<br>"
              + "<b>Automatic trigger:</b> Tornado Warning/Watch, Tornado Emergency, "
              + "Severe Thunderstorm Warning/Watch, Flash Flood Warning, Extreme Wind Warning, "
              + "and other NWS alerts classified as Extreme.</html>");
        addFull(p,y++,liveNote);

        addRow(p,y++,"TomTom API key (traffic/routes)",tomTom);
        addRow(p,y++,"Route refresh (minutes)",routeRefresh);
        addRow(p,y++,"Weather refresh (minutes)",weatherRefresh);
        addRow(p,y++,"Radar refresh (minutes)",radarRefresh);
        addRow(p,y++,"NWS alert refresh (minutes)",alertRefresh);

        JTextArea note=new JTextArea(
                "Refresh changes take effect immediately after Save & Apply. "
              + "When Live Severe Weather Mode is enabled, its rapid weather/radar/alert "
              + "intervals temporarily override the normal values below. "
              + "For normal workday operation, a 10-minute route refresh is a good balance "
              + "between fresh commute information and conservative TomTom API usage. "
              + "Weather uses Open-Meteo and National Weather Service data without an API key. "
              + "Live TomTom traffic and route travel times require the TomTom key. Images placed "
              + "in the media folder are rotated by the Media dashboard block.");
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setEditable(false);
        note.setOpaque(false);
        addFull(p,y++,note);
        return p;
    }

    private JPanel buttons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exit=new JButton("Exit Application");
        exit.addActionListener(e->System.exit(0));
        JButton cancel=new JButton("Cancel");
        cancel.addActionListener(e->dispose());
        JButton save=new JButton("Save & Apply");
        save.addActionListener(e->save());
        p.add(exit);
        p.add(cancel);
        p.add(save);
        return p;
    }

    private void updateAutomaticSevereControls(){
        autoDisableSevereWeather.setEnabled(automaticSevereWeather.isSelected());
    }

    private void loadValues(){
        header.setText(cfg.headerText);
        ticker.setText(cfg.tickerText);
        showHeader.setSelected(cfg.showHeader);
        showTicker.setSelected(cfg.showTicker);
        fullscreen.setSelected(cfg.fullscreen);
        dark.setSelected(cfg.darkMode);
        radar.setSelected(cfg.showRadar);
        traffic.setSelected(cfg.showTraffic);
        alertMap.setSelected(cfg.showAlertsOnMap);
        liveSevereWeather.setSelected(cfg.liveSevereWeatherMode);
        automaticSevereWeather.setSelected(cfg.automaticSevereWeatherMode);
        autoDisableSevereWeather.setSelected(cfg.autoDisableSevereWeatherMode);
        tomTom.setText(cfg.tomTomApiKey);
        mediaDir.setText(cfg.mediaDirectory.toString());
        showcaseMedia.setSelected(cfg.mainShowcaseMediaEnabled);
        severeMapPriority.setSelected(cfg.severeWeatherMapPriority);
        selectInteger(showcaseInterval,cfg.mainShowcaseIntervalSeconds);
        selectInteger(routeRefresh,cfg.trafficRefreshMinutes);
        selectInteger(weatherRefresh,cfg.weatherRefreshMinutes);
        selectInteger(radarRefresh,cfg.radarRefreshMinutes);
        selectInteger(alertRefresh,cfg.alertRefreshMinutes);

        primaryName.setText(cfg.primary.name());
        primaryLat.setText(Double.toString(cfg.primary.latitude()));
        primaryLon.setText(Double.toString(cfg.primary.longitude()));

        locationModel.setRowCount(0);
        for(Location l:cfg.monitored)
            locationModel.addRow(new Object[]{l.name(),l.latitude(),l.longitude()});

        routeModel.setRowCount(0);
        for(RouteConfig r:cfg.routes)
            routeModel.addRow(new Object[]{
                    r.name(),r.destination().name(),
                    r.destination().latitude(),r.destination().longitude()
            });

        mapWidthSlider.setValue(Math.max(55,Math.min(75,cfg.mapWidthPercent)));
        updateMapWidthLabel();

        int count=Math.max(6,Math.min(12,cfg.visibleWidgetCount));
        if(count!=6&&count!=8&&count!=10&&count!=12) count=10;
        blockCount.setSelectedItem(count);
        rebuildWidgetRows();

        for(int i=0;i<widgetBoxes.size();i++){
            String id=i<cfg.widgetTypes.size()?cfg.widgetTypes.get(i):"STATUS";
            selectWidgetId(widgetBoxes.get(i),id);
        }
    }

    private void rebuildWidgetRows(){
        int count=(Integer)blockCount.getSelectedItem();
        List<String> oldIds=new ArrayList<>();
        for(JComboBox<WidgetChoice> box:widgetBoxes){
            WidgetChoice c=(WidgetChoice)box.getSelectedItem();
            oldIds.add(c==null?"STATUS":c.id());
        }

        widgetRows.removeAll();
        widgetBoxes.clear();
        List<WidgetChoice> choices=widgetChoices();

        for(int i=0;i<count;i++){
            JComboBox<WidgetChoice> box=new JComboBox<>(choices.toArray(new WidgetChoice[0]));
            widgetBoxes.add(box);

            GridBagConstraints a=new GridBagConstraints();
            a.gridx=0;a.gridy=i;a.anchor=GridBagConstraints.WEST;
            a.insets=new Insets(6,4,6,14);
            widgetRows.add(new JLabel("Block "+(i+1)),a);

            GridBagConstraints b=new GridBagConstraints();
            b.gridx=1;b.gridy=i;b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;
            b.insets=new Insets(6,4,6,4);
            widgetRows.add(box,b);

            String desired=i<oldIds.size()?oldIds.get(i):
                    (i<cfg.widgetTypes.size()?cfg.widgetTypes.get(i):"STATUS");
            selectWidgetId(box,desired);
        }

        GridBagConstraints filler=new GridBagConstraints();
        filler.gridx=0;filler.gridy=count;filler.gridwidth=2;filler.weighty=1;filler.fill=GridBagConstraints.VERTICAL;
        widgetRows.add(Box.createVerticalGlue(),filler);

        widgetRows.revalidate();
        widgetRows.repaint();
    }

    private List<WidgetChoice> widgetChoices(){
        List<WidgetChoice> out=new ArrayList<>();
        out.add(new WidgetChoice("WEATHER_PRIMARY","Current Weather • "+primaryNameValue()));
        out.add(new WidgetChoice("FORECAST_PRIMARY","Hourly Outlook • "+primaryNameValue()));
        out.add(new WidgetChoice("WIND_PRIMARY","Wind & Gusts • "+primaryNameValue()));
        out.add(new WidgetChoice("ALERTS","Severe Weather Alerts"));
        out.add(new WidgetChoice("MEDIA","Media / Announcements"));
        out.add(new WidgetChoice("STATUS","System Status"));

        for(int i=0;i<locationModel.getRowCount();i++){
            String name=cell(locationModel,i,0);
            if(!name.isBlank())
                out.add(new WidgetChoice("WEATHER_LOCATION_"+i,"Current Weather • "+name));
        }
        for(int i=0;i<routeModel.getRowCount();i++){
            String name=cell(routeModel,i,0);
            if(!name.isBlank())
                out.add(new WidgetChoice("ROUTE_"+i,"Route Time • "+name));
        }
        return out;
    }

    private void addRouteFromPinnedLocation(){
        int row=locationTable.getSelectedRow();
        if(row<0){
            JOptionPane.showMessageDialog(this,
                    "Select a pinned location on the Pinned Locations tab first.",
                    "Select a Location",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name=cell(locationModel,row,0);
        routeModel.addRow(new Object[]{name,name,cell(locationModel,row,1),cell(locationModel,row,2)});
        rebuildWidgetRows();
    }

    private void removeSelected(JTable table, DefaultTableModel model){
        int[] rows=table.getSelectedRows();
        for(int i=rows.length-1;i>=0;i--) model.removeRow(rows[i]);
        rebuildWidgetRows();
    }

    private void save(){
        try{
            stopTableEditing(locationTable);
            stopTableEditing(routeTable);

            cfg.headerText=header.getText().trim();
            cfg.tickerText=ticker.getText().trim();
            cfg.showHeader=showHeader.isSelected();
            cfg.showTicker=showTicker.isSelected();
            cfg.fullscreen=fullscreen.isSelected();
            cfg.darkMode=dark.isSelected();
            cfg.showRadar=radar.isSelected();
            cfg.showTraffic=traffic.isSelected();
            cfg.showAlertsOnMap=alertMap.isSelected();
            cfg.liveSevereWeatherMode=liveSevereWeather.isSelected();
            cfg.automaticSevereWeatherMode=automaticSevereWeather.isSelected();
            cfg.autoDisableSevereWeatherMode=autoDisableSevereWeather.isSelected();
            cfg.tomTomApiKey=tomTom.getText().trim();
            cfg.trafficRefreshMinutes=(Integer)routeRefresh.getSelectedItem();
            cfg.weatherRefreshMinutes=(Integer)weatherRefresh.getSelectedItem();
            cfg.radarRefreshMinutes=(Integer)radarRefresh.getSelectedItem();
            cfg.alertRefreshMinutes=(Integer)alertRefresh.getSelectedItem();
            cfg.mediaDirectory=Path.of(mediaDir.getText().trim());
            cfg.mainShowcaseMediaEnabled=showcaseMedia.isSelected();
            cfg.severeWeatherMapPriority=severeMapPriority.isSelected();
            cfg.mainShowcaseIntervalSeconds=(Integer)showcaseInterval.getSelectedItem();

            cfg.primary=new Location(
                    required(primaryName.getText(),"Primary location name"),
                    number(primaryLat.getText(),"Primary latitude"),
                    number(primaryLon.getText(),"Primary longitude")
            );

            cfg.monitored.clear();
            for(int i=0;i<locationModel.getRowCount();i++){
                String name=cell(locationModel,i,0).trim();
                if(name.isBlank()) continue;
                cfg.monitored.add(new Location(
                        name,
                        number(cell(locationModel,i,1),name+" latitude"),
                        number(cell(locationModel,i,2),name+" longitude")
                ));
            }
            if(cfg.monitored.isEmpty()) cfg.monitored.add(cfg.primary);

            cfg.routes.clear();
            for(int i=0;i<routeModel.getRowCount();i++){
                String routeName=cell(routeModel,i,0).trim();
                String destName=cell(routeModel,i,1).trim();
                if(routeName.isBlank()||destName.isBlank()) continue;
                Location d=new Location(
                        destName,
                        number(cell(routeModel,i,2),destName+" latitude"),
                        number(cell(routeModel,i,3),destName+" longitude")
                );
                cfg.routes.add(new RouteConfig(routeName,cfg.primary,d));
            }

            cfg.visibleWidgetCount=(Integer)blockCount.getSelectedItem();
            cfg.mapWidthPercent=mapWidthSlider.getValue();
            cfg.widgetTypes.clear();
            for(JComboBox<WidgetChoice> box:widgetBoxes){
                WidgetChoice choice=(WidgetChoice)box.getSelectedItem();
                cfg.widgetTypes.add(choice==null?"STATUS":choice.id());
            }

            ConfigService.save(cfg);
            onSave.accept(cfg);
            dispose();
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,
                    "Unable to save settings:\n"+ex.getMessage(),
                    "Settings Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void stopTableEditing(JTable table){
        if(table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    private static double number(String text,String label){
        try{return Double.parseDouble(text.trim());}
        catch(Exception e){throw new IllegalArgumentException(label+" must be a valid number.");}
    }

    private static String required(String text,String label){
        String s=text.trim();
        if(s.isBlank()) throw new IllegalArgumentException(label+" cannot be blank.");
        return s;
    }

    private String primaryNameValue(){
        String s=primaryName.getText().trim();
        return s.isBlank()?cfg.primary.name():s;
    }

    private static String cell(DefaultTableModel m,int row,int col){
        Object v=m.getValueAt(row,col);
        return v==null?"":v.toString();
    }

    private static void selectWidgetId(JComboBox<WidgetChoice> box,String id){
        for(int i=0;i<box.getItemCount();i++){
            WidgetChoice c=box.getItemAt(i);
            if(c.id().equals(id)){box.setSelectedIndex(i);return;}
        }
        // Missing IDs can occur after a route/location was deleted. Fall back safely.
        for(int i=0;i<box.getItemCount();i++)
            if(box.getItemAt(i).id().equals("STATUS")){box.setSelectedIndex(i);return;}
    }


    private static void selectInteger(JComboBox<Integer> box,int value){
        for(int i=0;i<box.getItemCount();i++){
            if(box.getItemAt(i)==value){
                box.setSelectedIndex(i);
                return;
            }
        }
        // Preserve old/custom configurations by adding their current value.
        box.addItem(value);
        box.setSelectedItem(value);
    }

    private static JPanel form(){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        return p;
    }

    private static void addRow(JPanel p,int y,String label,JComponent field){
        GridBagConstraints a=new GridBagConstraints();
        a.gridx=0;a.gridy=y;a.anchor=GridBagConstraints.WEST;
        a.insets=new Insets(6,4,6,12);
        p.add(new JLabel(label),a);

        GridBagConstraints b=new GridBagConstraints();
        b.gridx=1;b.gridy=y;b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;
        b.insets=new Insets(6,4,6,4);
        p.add(field,b);
    }

    private static void addFull(JPanel p,int y,JComponent c){
        GridBagConstraints b=new GridBagConstraints();
        b.gridx=0;b.gridy=y;b.gridwidth=2;b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;
        b.insets=new Insets(6,4,6,4);
        p.add(c,b);
    }

    private record WidgetChoice(String id,String label){
        @Override public String toString(){return label;}
    }
}
