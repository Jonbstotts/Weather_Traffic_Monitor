package com.wtm.ui;

import com.wtm.model.SportsConfig;
import com.wtm.model.SportsGame;
import com.wtm.net.HttpService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.*;

/** Compact two-team scoreboard used inside a standard dashboard card. */
public final class SportsScorePanel extends JPanel {
    private static final ExecutorService LOADER=Executors.newFixedThreadPool(2,r->{
        Thread t=new Thread(r,"sports-logo-loader");t.setDaemon(true);return t;});
    private static final Map<String,ImageIcon> CACHE=new ConcurrentHashMap<>();

    private final HttpService http;
    private final JLabel homeLogo=new JLabel();
    private final JLabel awayLogo=new JLabel();
    private final JLabel homeName=new JLabel("—",SwingConstants.CENTER);
    private final JLabel awayName=new JLabel("—",SwingConstants.CENTER);
    private final JLabel homeScore=new JLabel("",SwingConstants.CENTER);
    private final JLabel awayScore=new JLabel("",SwingConstants.CENTER);
    private final JLabel centerStatus=new JLabel("Loading…",SwingConstants.CENTER);
    private final JLabel footer=new JLabel("",SwingConstants.CENTER);

    public SportsScorePanel(HttpService http){
        this.http=http;
        setOpaque(false);
        setLayout(new BorderLayout(8,6));

        JPanel teams=new JPanel(new GridLayout(1,3,8,0));
        teams.setOpaque(false);
        teams.add(teamPanel(homeLogo,homeName,homeScore));
        centerStatus.setFont(centerStatus.getFont().deriveFont(Font.BOLD,13f));
        teams.add(centerStatus);
        teams.add(teamPanel(awayLogo,awayName,awayScore));
        add(teams,BorderLayout.CENTER);

        footer.setFont(footer.getFont().deriveFont(11f));
        add(footer,BorderLayout.SOUTH);
    }

    private JPanel teamPanel(JLabel logo,JLabel name,JLabel score){
        JPanel p=new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        logo.setAlignmentX(.5f); logo.setHorizontalAlignment(SwingConstants.CENTER);
        name.setAlignmentX(.5f); name.setFont(name.getFont().deriveFont(Font.BOLD,12f));
        score.setAlignmentX(.5f); score.setFont(score.getFont().deriveFont(Font.BOLD,24f));
        p.add(logo);p.add(Box.createVerticalStrut(3));p.add(name);p.add(score);return p;
    }

    public void updateGame(SportsGame game,SportsConfig cfg){
        if(game==null){ centerStatus.setText("Loading…"); return; }
        if("UNAVAILABLE".equalsIgnoreCase(game.status())){
            homeName.setText(cfg.teamName()); awayName.setText("—");
            homeScore.setText("");awayScore.setText("");centerStatus.setText("No event data");
            footer.setText(game.dataMode()); return;
        }

        homeName.setText(shortName(game.homeTeam())); awayName.setText(shortName(game.awayTeam()));
        homeScore.setText(game.homeScore()>=0?Integer.toString(game.homeScore()):"");
        awayScore.setText(game.awayScore()>=0?Integer.toString(game.awayScore()):"");

        String status;
        if(game.live()) status="● LIVE"+(game.progress().isBlank()?"":"  "+game.progress());
        else if(game.finished()) status="FINAL";
        else status="NEXT GAME";
        centerStatus.setText(status);
        if(game.live()) centerStatus.setForeground(Theme.danger());
        else centerStatus.setForeground(UIManager.getColor("Label.foreground"));

        if(game.startTime()!=null){
            String time=DateTimeFormatter.ofPattern("EEE MMM d • h:mm a")
                    .format(game.startTime().atZone(ZoneId.systemDefault()));
            footer.setText(time+"  •  "+game.dataMode());
        }else footer.setText(game.dataMode());

        if(cfg.showLogos()){
            loadLogo(homeLogo,game.homeBadgeUrl()); loadLogo(awayLogo,game.awayBadgeUrl());
        }else{ homeLogo.setIcon(null); awayLogo.setIcon(null); }
    }

    private void loadLogo(JLabel label,String url){
        if(url==null||url.isBlank()){label.setIcon(null);return;}
        ImageIcon cached=CACHE.get(url);
        if(cached!=null){label.setIcon(cached);return;}
        LOADER.submit(()->{
            try{
                byte[] bytes=http.getBytes(url);
                BufferedImage img=ImageIO.read(new ByteArrayInputStream(bytes));
                if(img==null)return;
                Image scaled=img.getScaledInstance(42,42,Image.SCALE_SMOOTH);
                ImageIcon icon=new ImageIcon(scaled);CACHE.put(url,icon);
                SwingUtilities.invokeLater(()->label.setIcon(icon));
            }catch(Exception ignored){}
        });
    }

    private static String shortName(String s){
        if(s==null||s.isBlank())return "—";
        return s.length()>18?s.substring(0,17)+"…":s;
    }
}
