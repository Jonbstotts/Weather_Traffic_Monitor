package com.wtm.ui;

import com.wtm.config.*;
import com.wtm.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private final AppTheme originalTheme;

    private final JTextField header=new JTextField();
    private final JTextField ticker=new JTextField();
    private final JCheckBox showHeader=new JCheckBox("Show title/header");
    private final JCheckBox showTicker=new JCheckBox("Show scrolling ticker");
    private final JCheckBox fullscreen=new JCheckBox("Fullscreen on startup");
    private final JComboBox<AppTheme> themeSelector=new JComboBox<>(AppTheme.values());
    private final JPanel themePreview=new JPanel();
    private final JCheckBox automaticHolidayThemes=new JCheckBox(
            "Automatically switch to holiday / seasonal themes");
    private final JCheckBox themeEffects=new JCheckBox("Enable theme overlay effects");
    private final JComboBox<String> overlayIntensity=new JComboBox<>(new String[]{"LOW","MEDIUM","HIGH"});
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

    private final JPasswordField tomTom=new JPasswordField();
    private final JPasswordField weatherKey=new JPasswordField();
    private final JComboBox<String> weatherProvider=new JComboBox<>(new String[]{
            "Open-Meteo Free (no key)", "Open-Meteo Customer (API key)"});
    private final JComboBox<String> alertProvider=new JComboBox<>(new String[]{"National Weather Service (NWS)"});
    private final JComboBox<String> radarProvider=new JComboBox<>(new String[]{"RainViewer Public Radar"});
    private final JComboBox<String> trafficProvider=new JComboBox<>(new String[]{"TomTom Traffic & Routing"});
    private final JComboBox<String> sportsProvider=new JComboBox<>(new String[]{"TheSportsDB"});
    private final JPasswordField sportsKey=new JPasswordField();
    private final JCheckBox sportsPremium=new JCheckBox(
            "Use TheSportsDB Premium live scores (requires premium API key)");
    private final JTextField nwsUserAgent=new JTextField();
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
    private final JComboBox<Integer> sportsRefresh =
            new JComboBox<>(new Integer[]{2,5,10,15,30,60});

    private final DefaultTableModel locationModel = new DefaultTableModel(
            new Object[]{"Pinned location","Latitude","Longitude"},0);
    private final JTable locationTable = new JTable(locationModel);

    private final DefaultTableModel routeModel = new DefaultTableModel(
            new Object[]{"Route name","Destination","Latitude","Longitude"},0);
    private final JTable routeTable = new JTable(routeModel);

    private final DefaultTableModel sportsModel = new DefaultTableModel(
            new Object[]{"Block name","Sport","League ID","Team ID","Team name","Show logos"},0){
        @Override public Class<?> getColumnClass(int column){ return column==5?Boolean.class:String.class; }
    };
    private final JTable sportsTable = new JTable(sportsModel);

    private final JCheckBox celebrationsEnabled=new JCheckBox(
            "Automatically add today's birthday / anniversary slides to Main Showcase");
    private final JTextField celebrationMediaDir=new JTextField();
    private final DefaultTableModel celebrationModel=new DefaultTableModel(
            new Object[]{
                    "Name","Birthday (MM-DD)","Hire Date (YYYY-MM-DD)","Photo path",
                    "Birthday","Anniversary","Confetti","Enabled"
            },0){
        @Override public Class<?> getColumnClass(int column){
            return column>=4?Boolean.class:String.class;
        }
    };
    private final JTable celebrationTable=new JTable(celebrationModel);

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
        this.originalTheme=AppTheme.fromId(cfg.themeId);

        setSize(980,790);
        setMinimumSize(new Dimension(860,650));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("General",general());
        tabs.addTab("Pinned Locations",locations());
        tabs.addTab("Routes",routes());
        tabs.addTab("Sports",sports());
        tabs.addTab("Team Celebrations",celebrations());
        tabs.addTab("Dashboard Blocks",widgets());
        tabs.addTab("Main Showcase",showcase());
        tabs.addTab("API Providers",apiProviders());
        tabs.addTab("API Usage",new ApiUsagePanel(cfg));
        tabs.addTab("Data & Refresh",data());

        add(tabs,BorderLayout.CENTER);
        add(buttons(),BorderLayout.SOUTH);

        automaticSevereWeather.addActionListener(e->updateAutomaticSevereControls());

        loadValues();
        updateAutomaticSevereControls();
        applySettingsTheme((AppTheme)themeSelector.getSelectedItem());
    }

    private JPanel general(){
        JPanel p=form();
        int y=0;
        addRow(p,y++,"Header text",header);
        addRow(p,y++,"Ticker text",ticker);
        addFull(p,y++,showHeader);
        addFull(p,y++,showTicker);
        addFull(p,y++,fullscreen);
        addRow(p,y++,"Application theme",themeSelector);

        themePreview.setPreferredSize(new Dimension(420,72));
        themePreview.setBorder(BorderFactory.createTitledBorder("Theme preview"));
        addFull(p,y++,themePreview);
        addFull(p,y++,automaticHolidayThemes);

        JLabel holidayStatus=new JLabel(
                "<html>"+HolidayThemeService.automaticThemeDescription(LocalDate.now())
              + ". Manual theme is used outside automatic holiday windows.</html>");
        addFull(p,y++,holidayStatus);

        addFull(p,y++,themeEffects);
        addRow(p,y++,"Overlay intensity",overlayIntensity);

        JLabel overlayHelp=new JLabel(
                "<html>Holiday themes can add polished effects such as Christmas snow/lights, "
              + "Halloween fog/lights, Independence Day fireworks, Thanksgiving leaves, Valentine "
              + "hearts, and St. Patrick’s shamrocks/gold glints. Automatic severe-weather map "
              + "priority suppresses decorative overlays immediately.</html>");
        addFull(p,y++,overlayHelp);

        themeSelector.addActionListener(e->{
            updateThemePreview();
            applySettingsTheme((AppTheme)themeSelector.getSelectedItem());
        });
        return p;
    }

    /**
     * Live-preview the chosen theme across the entire Settings window.
     * Nothing is persisted until Save & Apply.
     */
    private void applySettingsTheme(AppTheme theme){
        if(theme==null)theme=originalTheme;
        ThemeStyler.apply(this,theme);
        repaint();
    }

    private void updateThemePreview(){
        AppTheme t=(AppTheme)themeSelector.getSelectedItem();
        if(t==null)t=AppTheme.DARK;

        themePreview.removeAll();
        themePreview.setLayout(new GridLayout(1,4,8,8));
        themePreview.setBackground(t.bg());

        themePreview.add(previewSwatch("Background",t.bg(),t.text()));
        themePreview.add(previewSwatch("Card",t.panel(),t.text()));
        themePreview.add(previewSwatch("Accent",t.accent(),Color.WHITE));
        themePreview.add(previewSwatch("Outline",t.panel2(),t.text()));

        themePreview.revalidate();
        themePreview.repaint();
    }

    private static JPanel previewSwatch(String label,Color bg,Color fg){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createLineBorder(fg,1,true));
        JLabel l=new JLabel(label);
        l.setForeground(fg);
        l.setFont(l.getFont().deriveFont(Font.BOLD,12f));
        p.add(l);
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
              + "Use Find Location to search a city/place and fill coordinates automatically. "
              + "Manual latitude/longitude remains available for very specific points.");
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

        JButton find=new JButton("Find Location");
        find.setToolTipText("Search for a city/place and fill latitude/longitude automatically.");
        find.addActionListener(e->findPinnedLocation());

        JButton primarySearch=new JButton("Find Primary Location");
        primarySearch.setToolTipText("Search and fill the Primary Location fields above.");
        primarySearch.addActionListener(e->findPrimaryLocation());

        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(locationTable,locationModel));

        controls.add(add);
        controls.add(find);
        controls.add(primarySearch);
        controls.add(remove);
        outer.add(controls,BorderLayout.SOUTH);
        return outer;
    }

    private JPanel routes(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JTextArea help=new JTextArea(
                "Routes originate at the Primary Location. Use Find Destination to search a city/place "
              + "and fill route coordinates automatically, or create a route from an existing pin. "
              + "Manual coordinates remain available as a fallback.");
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

        JButton findDestination=new JButton("Find Destination");
        findDestination.setToolTipText("Search for a route destination and fill coordinates automatically.");
        findDestination.addActionListener(e->findRouteDestination());

        JButton addFromPin=new JButton("+ Route from selected pin");
        addFromPin.addActionListener(e->addRouteFromPinnedLocation());

        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(routeTable,routeModel));

        controls.add(add);
        controls.add(findDestination);
        controls.add(addFromPin);
        controls.add(remove);
        outer.add(controls,BorderLayout.SOUTH);
        return outer;
    }

    private JPanel sports(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JTextArea help=new JTextArea(
                "Create sports selections here; each saved selection becomes a Sports Score choice "
              + "under Dashboard Blocks, just like a configured route. Use Find Team to search the "
              + "configured sports provider and automatically fill Team ID, League ID, Sport, and "
              + "Team Name. TheSportsDB currently reserves general team-name search for premium access, "
              + "so the free key may require manual IDs for teams other than its supported test search. "
              + "Existing schedule, artwork, and recent-result blocks remain usable with the configured tier." );
        help.setLineWrap(true);help.setWrapStyleWord(true);help.setEditable(false);help.setOpaque(false);
        outer.add(help,BorderLayout.NORTH);

        sportsTable.setFillsViewportHeight(true);sportsTable.setRowHeight(26);
        outer.add(new JScrollPane(sportsTable),BorderLayout.CENTER);

        JPanel bottom=new JPanel();bottom.setLayout(new BoxLayout(bottom,BoxLayout.Y_AXIS));
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add=new JButton("+ Add sports selection");
        add.addActionListener(e->{
            sportsModel.addRow(new Object[]{"New Sports Block","American Football","","","Team",Boolean.TRUE});
            rebuildWidgetRows();
        });
        JButton findTeam=new JButton("Find Team");
        findTeam.setToolTipText("Search the sports provider and fill IDs/details automatically.");
        findTeam.addActionListener(e->findSportsTeam());

        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(sportsTable,sportsModel));

        controls.add(add);
        controls.add(findTeam);
        controls.add(remove);

        JPanel refreshRow=new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshRow.add(new JLabel("Sports refresh (minutes):"));refreshRow.add(sportsRefresh);
        refreshRow.add(new JLabel("Use 2 minutes when premium live scores are enabled."));
        bottom.add(controls);bottom.add(refreshRow);
        outer.add(bottom,BorderLayout.SOUTH);
        return outer;
    }

    /**
     * Opens the provider-backed team finder. If a sports row is already
     * selected, that row is populated. Otherwise a new sports row is created.
     */
    private void findSportsTeam(){
        stopTableEditing(sportsTable);

        int selected=sportsTable.getSelectedRow();
        int modelRow=selected<0 ? -1 : sportsTable.convertRowIndexToModel(selected);

        String initial="";
        if(modelRow>=0){
            initial=cell(sportsModel,modelRow,4).trim();
            if(initial.equalsIgnoreCase("Team")) initial="";
        }

        String key=new String(sportsKey.getPassword()).trim();
        if(key.isBlank()) key=cfg.sportsApiKey==null?"123":cfg.sportsApiKey.trim();
        if(key.isBlank()) key="123";

        boolean premium=sportsPremium.isSelected();

        TeamSearchDialog dialog=new TeamSearchDialog(
                this,
                initial,
                key,
                premium,
                result->applyTeamSearchResult(modelRow,result)
        );
        dialog.setVisible(true);
    }

    private void applyTeamSearchResult(int existingRow,TeamSearchResult result){
        int row=existingRow;

        if(row<0 || row>=sportsModel.getRowCount()){
            String blockName=result.teamName();
            if(result.sport()!=null && !result.sport().isBlank())
                blockName+=" "+shortSportLabel(result.sport());

            sportsModel.addRow(new Object[]{
                    blockName,
                    result.sport(),
                    result.leagueId(),
                    result.teamId(),
                    result.teamName(),
                    Boolean.TRUE
            });
            row=sportsModel.getRowCount()-1;
        }else{
            sportsModel.setValueAt(result.sport(),row,1);
            sportsModel.setValueAt(result.leagueId(),row,2);
            sportsModel.setValueAt(result.teamId(),row,3);
            sportsModel.setValueAt(result.teamName(),row,4);
            sportsModel.setValueAt(Boolean.TRUE,row,5);

            String currentName=cell(sportsModel,row,0).trim();
            if(currentName.isBlank() || currentName.equalsIgnoreCase("New Sports Block"))
                sportsModel.setValueAt(result.teamName()+" "+shortSportLabel(result.sport()),row,0);
        }

        rebuildWidgetRows();

        int viewRow=sportsTable.convertRowIndexToView(row);
        if(viewRow>=0){
            sportsTable.setRowSelectionInterval(viewRow,viewRow);
            sportsTable.scrollRectToVisible(sportsTable.getCellRect(viewRow,0,true));
        }
    }

    private static String shortSportLabel(String sport){
        if(sport==null||sport.isBlank()) return "Sports";
        if("American Football".equalsIgnoreCase(sport)) return "Football";
        if("Ice Hockey".equalsIgnoreCase(sport)) return "Hockey";
        return sport;
    }

    private JPanel celebrations(){
        JPanel outer=new JPanel(new BorderLayout(10,10));
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JPanel top=form();
        int y=0;

        JLabel title=new JLabel("Automatic Team Celebrations");
        title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
        addFull(top,y++,title);

        JTextArea help=new JTextArea(
                "Birthday and work-anniversary records are stored only in the local application "
              + "configuration. On matching dates, the application generates a temporary Main "
              + "Showcase slide automatically. Photos are optional; when omitted, initials are used. "
              + "The celebration animation runs once per generated slide per application session.");
        help.setLineWrap(true);help.setWrapStyleWord(true);
        help.setEditable(false);help.setOpaque(false);
        addFull(top,y++,help);

        addFull(top,y++,celebrationsEnabled);
        addRow(top,y++,"Celebration photo folder",celebrationMediaDir);

        outer.add(top,BorderLayout.NORTH);

        celebrationTable.setFillsViewportHeight(true);
        celebrationTable.setRowHeight(28);
        outer.add(new JScrollPane(celebrationTable),BorderLayout.CENTER);

        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton add=new JButton("+ Add Team Member");
        add.addActionListener(e->celebrationModel.addRow(new Object[]{
                "Team Member","","","",Boolean.TRUE,Boolean.TRUE,Boolean.TRUE,Boolean.TRUE
        }));

        JButton choosePhoto=new JButton("Choose Photo for Selected");
        choosePhoto.addActionListener(e->chooseCelebrationPhoto());

        JButton remove=new JButton("Remove selected");
        remove.addActionListener(e->removeSelected(celebrationTable,celebrationModel));

        controls.add(add);
        controls.add(choosePhoto);
        controls.add(remove);
        outer.add(controls,BorderLayout.SOUTH);

        return outer;
    }

    private void chooseCelebrationPhoto(){
        int view=celebrationTable.getSelectedRow();
        if(view<0){
            JOptionPane.showMessageDialog(this,
                    "Select a team member first.",
                    "Select Team Member",JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser=new JFileChooser();
        if(!celebrationMediaDir.getText().trim().isBlank())
            chooser.setCurrentDirectory(Path.of(celebrationMediaDir.getText().trim()).toFile());

        int result=chooser.showOpenDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            try{
                Path source=chooser.getSelectedFile().toPath();
                Path folder=celebrationMediaDir.getText().trim().isBlank()
                        ?ConfigService.appDataDir().resolve("celebrations-media")
                        :Path.of(celebrationMediaDir.getText().trim());

                Files.createDirectories(folder);

                int row=celebrationTable.convertRowIndexToModel(view);
                String member=cell(celebrationModel,row,0).trim();
                String base=member.isBlank()?"team-member":member
                        .toLowerCase()
                        .replaceAll("[^a-z0-9]+","-")
                        .replaceAll("(^-|-$)","");

                String original=source.getFileName().toString();
                int dot=original.lastIndexOf('.');
                String extension=dot>=0?original.substring(dot):".jpg";

                Path target=folder.resolve(base+extension.toLowerCase());
                Files.copy(source,target,StandardCopyOption.REPLACE_EXISTING);

                celebrationMediaDir.setText(folder.toString());
                celebrationModel.setValueAt(target.toAbsolutePath().toString(),row,3);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to copy celebration photo: "+ex.getMessage(),
                        "Photo Import Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
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

    /**
     * Provider/credential settings are intentionally separate from refresh-rate
     * controls. New provider adapters can be added here later without changing
     * the rest of the Settings layout.
     */
    private JPanel apiProviders(){
        JPanel p=form();
        int y=0;

        JLabel title=new JLabel("API Providers & Credentials");
        title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
        addFull(p,y++,title);

        JTextArea intro=new JTextArea(
                "Choose the installed provider adapter for each data type and manage its credentials. "
              + "Open-Meteo Free, NWS, and RainViewer do not require API keys. Open-Meteo Customer "
              + "and TomTom use credentials. A different future vendor will require a provider adapter "
              + "for that vendor's response format, but this Settings structure will remain the same.");
        intro.setLineWrap(true); intro.setWrapStyleWord(true); intro.setEditable(false); intro.setOpaque(false);
        addFull(p,y++,intro);

        addRow(p,y++,"Weather provider",weatherProvider);
        addRow(p,y++,"Open-Meteo customer API key",weatherKey);
        addRow(p,y++,"Alert provider",alertProvider);
        addRow(p,y++,"NWS User-Agent",nwsUserAgent);
        addRow(p,y++,"Radar provider",radarProvider);
        addRow(p,y++,"Traffic / routing provider",trafficProvider);
        addRow(p,y++,"TomTom API key",tomTom);
        addRow(p,y++,"Sports provider",sportsProvider);
        addRow(p,y++,"TheSportsDB API key",sportsKey);
        addFull(p,y++,sportsPremium);

        JLabel security=new JLabel(
                "<html><b>Credential storage:</b> API keys are saved separately in "
              + "<code>credentials.properties</code> under the local application-data folder. "
              + "On Linux/macOS the app attempts owner-only file permissions.</html>");
        addFull(p,y++,security);

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

        addRow(p,y++,"Route refresh (minutes)",routeRefresh);
        addRow(p,y++,"Weather refresh (minutes)",weatherRefresh);
        addRow(p,y++,"Radar refresh (minutes)",radarRefresh);
        addRow(p,y++,"NWS alert refresh (minutes)",alertRefresh);
        addRow(p,y++,"Sports refresh (minutes)",sportsRefresh);

        JTextArea note=new JTextArea(
                "Refresh changes take effect immediately after Save & Apply. "
              + "When Live Severe Weather Mode is enabled, its rapid weather/radar/alert "
              + "intervals temporarily override the normal values below. "
              + "For normal workday operation, a 10-minute route refresh is a good balance "
              + "between fresh commute information and conservative TomTom API usage. "
              + "Provider selection and credentials are managed on the API Providers tab. "
              + "These controls only determine refresh cadence.");
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
        cancel.addActionListener(e->{
            Theme.setActive(originalTheme.id());
            dispose();
        });
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
        themeSelector.setSelectedItem(AppTheme.fromId(cfg.themeId));
        updateThemePreview();
        automaticHolidayThemes.setSelected(cfg.automaticHolidayThemes);
        themeEffects.setSelected(cfg.themeOverlayEffects);
        overlayIntensity.setSelectedItem(cfg.overlayIntensity);
        radar.setSelected(cfg.showRadar);
        traffic.setSelected(cfg.showTraffic);
        alertMap.setSelected(cfg.showAlertsOnMap);
        liveSevereWeather.setSelected(cfg.liveSevereWeatherMode);
        automaticSevereWeather.setSelected(cfg.automaticSevereWeatherMode);
        autoDisableSevereWeather.setSelected(cfg.autoDisableSevereWeatherMode);
        tomTom.setText(cfg.tomTomApiKey);
        weatherKey.setText(cfg.weatherApiKey);
        weatherProvider.setSelectedIndex("OPEN_METEO_CUSTOMER".equalsIgnoreCase(cfg.weatherProvider)?1:0);
        alertProvider.setSelectedIndex(0);
        radarProvider.setSelectedIndex(0);
        trafficProvider.setSelectedIndex(0);
        sportsProvider.setSelectedIndex(0);
        sportsKey.setText(cfg.sportsApiKey);
        sportsPremium.setSelected(cfg.sportsPremiumLiveScores);
        nwsUserAgent.setText(cfg.nwsUserAgent);
        mediaDir.setText(cfg.mediaDirectory.toString());
        showcaseMedia.setSelected(cfg.mainShowcaseMediaEnabled);
        severeMapPriority.setSelected(cfg.severeWeatherMapPriority);
        selectInteger(showcaseInterval,cfg.mainShowcaseIntervalSeconds);
        selectInteger(routeRefresh,cfg.trafficRefreshMinutes);
        selectInteger(weatherRefresh,cfg.weatherRefreshMinutes);
        selectInteger(radarRefresh,cfg.radarRefreshMinutes);
        selectInteger(alertRefresh,cfg.alertRefreshMinutes);
        selectInteger(sportsRefresh,cfg.sportsRefreshMinutes);

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

        sportsModel.setRowCount(0);
        for(SportsConfig sport:cfg.sports)
            sportsModel.addRow(new Object[]{sport.name(),sport.sport(),sport.leagueId(),sport.teamId(),sport.teamName(),sport.showLogos()});

        celebrationsEnabled.setSelected(cfg.celebrationsEnabled);
        celebrationMediaDir.setText(cfg.celebrationMediaDirectory.toString());
        celebrationModel.setRowCount(0);
        for(CelebrationConfig c:cfg.celebrations){
            String birthday=(c.birthdayMonth()>0&&c.birthdayDay()>0)
                    ?String.format("%02d-%02d",c.birthdayMonth(),c.birthdayDay())
                    :"";
            celebrationModel.addRow(new Object[]{
                    c.name(),
                    birthday,
                    c.hireDate()==null?"":c.hireDate().toString(),
                    c.photoPath(),
                    c.showBirthday(),
                    c.showAnniversary(),
                    c.celebrationEffect(),
                    c.enabled()
            });
        }

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
        for(int i=0;i<sportsModel.getRowCount();i++){
            String name=cell(sportsModel,i,0);
            if(!name.isBlank())
                out.add(new WidgetChoice("SPORTS_"+i,"Sports Score • "+name));
        }
        return out;
    }

    private void findPrimaryLocation(){
        String initial=primaryName.getText().trim();
        if(initial.equalsIgnoreCase("Primary Location")) initial="";

        LocationSearchDialog dialog=new LocationSearchDialog(
                this,
                initial,
                result->{
                    primaryName.setText(result.name());
                    primaryLat.setText(Double.toString(result.latitude()));
                    primaryLon.setText(Double.toString(result.longitude()));
                }
        );
        dialog.setVisible(true);
    }

    private void findPinnedLocation(){
        stopTableEditing(locationTable);

        int selected=locationTable.getSelectedRow();
        int modelRow=selected<0 ? -1 : locationTable.convertRowIndexToModel(selected);

        String initial="";
        if(modelRow>=0){
            initial=cell(locationModel,modelRow,0).trim();
            if(initial.equalsIgnoreCase("New Location")) initial="";
        }

        final int targetRow=modelRow;
        LocationSearchDialog dialog=new LocationSearchDialog(
                this,
                initial,
                result->applyPinnedLocationResult(targetRow,result)
        );
        dialog.setVisible(true);
    }

    private void applyPinnedLocationResult(int existingRow,LocationSearchResult result){
        int row=existingRow;
        if(row<0 || row>=locationModel.getRowCount()){
            locationModel.addRow(new Object[]{
                    result.name(),result.latitude(),result.longitude()
            });
            row=locationModel.getRowCount()-1;
        }else{
            locationModel.setValueAt(result.name(),row,0);
            locationModel.setValueAt(result.latitude(),row,1);
            locationModel.setValueAt(result.longitude(),row,2);
        }

        rebuildWidgetRows();
        int viewRow=locationTable.convertRowIndexToView(row);
        if(viewRow>=0){
            locationTable.setRowSelectionInterval(viewRow,viewRow);
            locationTable.scrollRectToVisible(locationTable.getCellRect(viewRow,0,true));
        }
    }

    private void findRouteDestination(){
        stopTableEditing(routeTable);

        int selected=routeTable.getSelectedRow();
        int modelRow=selected<0 ? -1 : routeTable.convertRowIndexToModel(selected);

        String initial="";
        if(modelRow>=0){
            initial=cell(routeModel,modelRow,1).trim();
            if(initial.equalsIgnoreCase("Destination")) initial="";
        }

        final int targetRow=modelRow;
        LocationSearchDialog dialog=new LocationSearchDialog(
                this,
                initial,
                result->applyRouteLocationResult(targetRow,result)
        );
        dialog.setVisible(true);
    }

    private void applyRouteLocationResult(int existingRow,LocationSearchResult result){
        int row=existingRow;
        if(row<0 || row>=routeModel.getRowCount()){
            routeModel.addRow(new Object[]{
                    result.name(),result.name(),result.latitude(),result.longitude()
            });
            row=routeModel.getRowCount()-1;
        }else{
            String currentRoute=cell(routeModel,row,0).trim();
            if(currentRoute.isBlank() || currentRoute.equalsIgnoreCase("New Route"))
                routeModel.setValueAt(result.name(),row,0);

            routeModel.setValueAt(result.name(),row,1);
            routeModel.setValueAt(result.latitude(),row,2);
            routeModel.setValueAt(result.longitude(),row,3);
        }

        rebuildWidgetRows();
        int viewRow=routeTable.convertRowIndexToView(row);
        if(viewRow>=0){
            routeTable.setRowSelectionInterval(viewRow,viewRow);
            routeTable.scrollRectToVisible(routeTable.getCellRect(viewRow,0,true));
        }
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
            stopTableEditing(sportsTable);
            stopTableEditing(celebrationTable);

            cfg.headerText=header.getText().trim();
            cfg.tickerText=ticker.getText().trim();
            cfg.showHeader=showHeader.isSelected();
            cfg.showTicker=showTicker.isSelected();
            cfg.fullscreen=fullscreen.isSelected();
            AppTheme selected=(AppTheme)themeSelector.getSelectedItem();
            if(selected==null) selected=AppTheme.DARK;
            cfg.themeId=selected.id();
            cfg.automaticHolidayThemes=automaticHolidayThemes.isSelected();
            cfg.darkMode=selected.dark();
            cfg.themeOverlayEffects=themeEffects.isSelected();
            cfg.overlayIntensity=String.valueOf(overlayIntensity.getSelectedItem());
            cfg.showRadar=radar.isSelected();
            cfg.showTraffic=traffic.isSelected();
            cfg.showAlertsOnMap=alertMap.isSelected();
            cfg.liveSevereWeatherMode=liveSevereWeather.isSelected();
            cfg.automaticSevereWeatherMode=automaticSevereWeather.isSelected();
            cfg.autoDisableSevereWeatherMode=autoDisableSevereWeather.isSelected();
            cfg.weatherProvider=weatherProvider.getSelectedIndex()==1?"OPEN_METEO_CUSTOMER":"OPEN_METEO_FREE";
            cfg.alertProvider="NWS";
            cfg.radarProvider="RAINVIEWER";
            cfg.trafficProvider="TOMTOM";
            cfg.sportsProvider="THESPORTSDB";
            cfg.sportsPremiumLiveScores=sportsPremium.isSelected();
            cfg.sportsApiKey=new String(sportsKey.getPassword()).trim();
            if(cfg.sportsApiKey.isBlank()) cfg.sportsApiKey="123";
            cfg.weatherApiKey=new String(weatherKey.getPassword()).trim();
            cfg.tomTomApiKey=new String(tomTom.getPassword()).trim();
            cfg.nwsUserAgent=nwsUserAgent.getText().trim();
            if(cfg.nwsUserAgent.isBlank())
                cfg.nwsUserAgent="WeatherTrafficMonitor/1.6 (workplace-display; contact=local-admin)";
            cfg.trafficRefreshMinutes=(Integer)routeRefresh.getSelectedItem();
            cfg.weatherRefreshMinutes=(Integer)weatherRefresh.getSelectedItem();
            cfg.radarRefreshMinutes=(Integer)radarRefresh.getSelectedItem();
            cfg.alertRefreshMinutes=(Integer)alertRefresh.getSelectedItem();
            cfg.sportsRefreshMinutes=(Integer)sportsRefresh.getSelectedItem();
            cfg.mediaDirectory=Path.of(mediaDir.getText().trim());
            cfg.mainShowcaseMediaEnabled=showcaseMedia.isSelected();
            cfg.severeWeatherMapPriority=severeMapPriority.isSelected();
            cfg.mainShowcaseIntervalSeconds=(Integer)showcaseInterval.getSelectedItem();
            cfg.celebrationsEnabled=celebrationsEnabled.isSelected();
            cfg.celebrationMediaDirectory=celebrationMediaDir.getText().trim().isBlank()
                    ?ConfigService.appDataDir().resolve("celebrations-media")
                    :Path.of(celebrationMediaDir.getText().trim());

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

            cfg.celebrations.clear();
            for(int i=0;i<celebrationModel.getRowCount();i++){
                String name=cell(celebrationModel,i,0).trim();
                if(name.isBlank()) continue;

                int month=0,day=0;
                String birthday=cell(celebrationModel,i,1).trim();
                if(!birthday.isBlank()){
                    String[] parts=birthday.split("-");
                    if(parts.length!=2)
                        throw new IllegalArgumentException(name+" birthday must use MM-DD.");
                    month=Integer.parseInt(parts[0]);
                    day=Integer.parseInt(parts[1]);
                    LocalDate.of(2000,month,day); // validates month/day
                }

                LocalDate hireDate=null;
                String hire=cell(celebrationModel,i,2).trim();
                if(!hire.isBlank()){
                    try{hireDate=LocalDate.parse(hire);}
                    catch(DateTimeParseException ex){
                        throw new IllegalArgumentException(name+" hire date must use YYYY-MM-DD.");
                    }
                }

                String photo=cell(celebrationModel,i,3).trim();
                boolean birthdayOn=Boolean.TRUE.equals(celebrationModel.getValueAt(i,4));
                boolean anniversaryOn=Boolean.TRUE.equals(celebrationModel.getValueAt(i,5));
                boolean confetti=Boolean.TRUE.equals(celebrationModel.getValueAt(i,6));
                boolean enabled=Boolean.TRUE.equals(celebrationModel.getValueAt(i,7));

                cfg.celebrations.add(new CelebrationConfig(
                        name,month,day,hireDate,photo,
                        birthdayOn,anniversaryOn,confetti,enabled
                ));
            }

            cfg.sports.clear();
            for(int i=0;i<sportsModel.getRowCount();i++){
                String name=cell(sportsModel,i,0).trim();
                String sport=cell(sportsModel,i,1).trim();
                String leagueId=cell(sportsModel,i,2).trim();
                String teamId=cell(sportsModel,i,3).trim();
                String teamName=cell(sportsModel,i,4).trim();
                boolean showLogos=Boolean.TRUE.equals(sportsModel.getValueAt(i,5));
                if(name.isBlank()||teamId.isBlank()||teamName.isBlank()) continue;
                cfg.sports.add(new SportsConfig(name,sport,leagueId,teamId,teamName,showLogos));
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
        b.gridx=1;
        b.gridy=y;
        b.weightx=1;
        b.fill=GridBagConstraints.HORIZONTAL;
        b.anchor=GridBagConstraints.CENTER;
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
