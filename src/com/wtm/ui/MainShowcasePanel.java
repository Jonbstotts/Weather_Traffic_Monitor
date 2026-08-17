package com.wtm.ui;

import com.wtm.config.AppConfig;
import com.wtm.map.TileMapPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Configurable large-format content region.
 *
 * The live map is always the first item. If enabled, announcement images are
 * added after it and rotated on a site-defined interval.
 */
public final class MainShowcasePanel extends RoundedPanel {
    private final CardLayout cards=new CardLayout();
    private final JPanel deck=new JPanel(cards);
    private final TileMapPanel map;

    private AppConfig config;
    private boolean automaticSevereWeatherActive;
    private final List<String> cardIds=new ArrayList<>();
    private int currentIndex=0;
    private Timer rotationTimer;

    public MainShowcasePanel(AppConfig config, TileMapPanel map){
        super(20);
        this.config=config;
        this.map=map;

        putClientProperty("surfaceRole","map");
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        setMinimumSize(new Dimension(0,0));

        deck.setOpaque(false);
        add(deck,BorderLayout.CENTER);
        rebuild();
    }

    public void updateConfig(AppConfig newConfig){
        this.config=newConfig;
        rebuild();
    }

    public void setAutomaticSevereWeatherActive(boolean active){
        automaticSevereWeatherActive=active;
        if(active && config.severeWeatherMapPriority) showMap();
        updateRotationState();
    }

    private void rebuild(){
        if(rotationTimer!=null) rotationTimer.stop();

        deck.removeAll();
        cardIds.clear();
        currentIndex=0;

        deck.add(map,"MAP");
        cardIds.add("MAP");

        if(config.mainShowcaseMediaEnabled){
            int index=0;
            for(Path file:mediaFiles()){
                JComponent media=createMediaComponent(file);
                if(media!=null){
                    String id="MEDIA_"+index++;
                    deck.add(media,id);
                    cardIds.add(id);
                }
            }
        }

        cards.show(deck,"MAP");
        updateRotationState();
        revalidate();
        repaint();
    }

    private void updateRotationState(){
        if(rotationTimer!=null) rotationTimer.stop();

        boolean severeLock=automaticSevereWeatherActive && config.severeWeatherMapPriority;
        boolean rotate=config.mainShowcaseMediaEnabled && cardIds.size()>1 && !severeLock;

        if(!rotate){
            if(severeLock) showMap();
            return;
        }

        rotationTimer=new Timer(Math.max(5,config.mainShowcaseIntervalSeconds)*1000,e->advance());
        rotationTimer.setRepeats(true);
        rotationTimer.start();
    }

    private void advance(){
        if(automaticSevereWeatherActive && config.severeWeatherMapPriority){
            showMap();
            return;
        }
        if(cardIds.size()<=1) return;
        currentIndex=(currentIndex+1)%cardIds.size();
        cards.show(deck,cardIds.get(currentIndex));
    }

    private void showMap(){
        currentIndex=0;
        cards.show(deck,"MAP");
    }

    private List<Path> mediaFiles(){
        if(config.mediaDirectory==null || !Files.isDirectory(config.mediaDirectory))
            return List.of();

        try(var stream=Files.list(config.mediaDirectory)){
            return stream.filter(Files::isRegularFile)
                    .filter(this::isSupportedImage)
                    .sorted(Comparator.comparing(p->p.getFileName().toString().toLowerCase()))
                    .toList();
        }catch(IOException ex){
            System.err.println("Main Showcase media scan failed: "+ex.getMessage());
            return List.of();
        }
    }

    private boolean isSupportedImage(Path p){
        String n=p.getFileName().toString().toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg")
                || n.endsWith(".jpeg") || n.endsWith(".gif");
    }

    private JComponent createMediaComponent(Path file){
        try{
            BufferedImage image=ImageIO.read(file.toFile());
            if(image==null) return null;

            JPanel panel=new JPanel(new BorderLayout());
            panel.setBackground(Color.BLACK);

            JLabel imageView=new JLabel(){
                @Override
                protected void paintComponent(Graphics g){
                    super.paintComponent(g);
                    int w=getWidth(),h=getHeight();
                    if(w<=0||h<=0) return;

                    double scale=Math.min(w/(double)image.getWidth(),h/(double)image.getHeight());
                    int dw=(int)Math.round(image.getWidth()*scale);
                    int dh=(int)Math.round(image.getHeight()*scale);
                    int x=(w-dw)/2,y=(h-dh)/2;

                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(image,x,y,dw,dh,null);
                    g2.dispose();
                }
            };

            /*
             * Announcement media intentionally uses the entire showcase region.
             * Filenames are implementation details and are not displayed on the TV.
             */
            panel.add(imageView,BorderLayout.CENTER);
            return panel;
        }catch(IOException ex){
            System.err.println("Unable to load showcase media "+file+": "+ex.getMessage());
            return null;
        }
    }
}
