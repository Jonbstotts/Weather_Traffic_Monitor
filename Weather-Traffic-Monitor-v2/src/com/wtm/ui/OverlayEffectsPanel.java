package com.wtm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Transparent decorative overlay layer.
 *
 * Holiday effects intentionally use different animation systems instead of a
 * single generic particle animation:
 *
 *  • Christmas/Winter Frost: drawn snowflakes + frosted screen edges.
 *  • Halloween: layered, horizontally rolling fog banks.
 *  • Independence Day: rocket launches and radial firework bursts.
 *  • Thanksgiving: drifting autumn leaves.
 *  • Valentine's Day: floating hearts.
 *  • St. Patrick's Day: drifting shamrocks.
 *  • Celebration slides: short confetti burst.
 *
 * The layer never intercepts mouse input and is immediately suppressed during
 * automatic severe-weather map priority.
 */
public final class OverlayEffectsPanel extends JComponent {
    private final Random random=new Random();

    private final List<Particle> particles=new ArrayList<>();
    private final List<Firework> fireworks=new ArrayList<>();
    private final List<FogBank> fogBanks=new ArrayList<>();

    private final javax.swing.Timer timer;

    private AppTheme theme=AppTheme.DARK;
    private boolean themeEffectsEnabled=true;
    private boolean severeSuppressed=false;
    private String intensity="LOW";
    private long confettiUntil=0;
    private long nextFireworkAt=0;

    public OverlayEffectsPanel(){
        setOpaque(false);
        setFocusable(false);

        timer=new javax.swing.Timer(33,e->tick()); // ~30 fps for smooth motion
        timer.start();
    }

    @Override
    public boolean contains(int x,int y){
        return false;
    }

    public void configure(AppTheme theme,boolean enabled,String intensity){
        this.theme=theme==null?AppTheme.DARK:theme;
        this.themeEffectsEnabled=enabled;
        this.intensity=intensity==null?"LOW":intensity.toUpperCase();

        particles.clear();
        fireworks.clear();
        fogBanks.clear();
        nextFireworkAt=0;

        if(themeEffectsEnabled && this.theme==AppTheme.HALLOWEEN)
            createFogBanks();

        repaint();
    }

    public void setSevereSuppressed(boolean suppressed){
        severeSuppressed=suppressed;

        if(suppressed){
            confettiUntil=0;
            particles.clear();
            fireworks.clear();
            fogBanks.clear();
        }else if(themeEffectsEnabled && theme==AppTheme.HALLOWEEN){
            createFogBanks();
        }

        repaint();
    }

    /** Runs once when a generated celebration card first appears. */
    public void celebrate(){
        if(severeSuppressed)return;

        confettiUntil=System.currentTimeMillis()+6500;

        // Celebration confetti remains intentionally independent from holiday
        // animation and temporarily takes visual priority.
        for(int i=0;i<count(55,90,135);i++)
            particles.add(Particle.confetti(random,getWidth(),getHeight()));
    }

    private void tick(){
        if(!isShowing())return;

        if(severeSuppressed){
            if(!particles.isEmpty()||!fireworks.isEmpty()||!fogBanks.isEmpty()){
                particles.clear();
                fireworks.clear();
                fogBanks.clear();
                repaint();
            }
            return;
        }

        boolean confetti=System.currentTimeMillis()<confettiUntil;

        if(confetti){
            maintainConfetti();
        }else if(themeEffectsEnabled){
            switch(theme){
                case CHRISTMAS, WINTER_FROST -> maintainSnow();
                case HALLOWEEN -> updateFog();
                case INDEPENDENCE -> updateFireworks();
                case THANKSGIVING -> maintainSimpleParticles("LEAF");
                case VALENTINE -> maintainSimpleParticles("HEART");
                case ST_PATRICKS -> maintainSimpleParticles("SHAMROCK");
                default -> clearHolidayCollections();
            }
        }else{
            clearHolidayCollections();
        }

        updateParticles();
        repaint();
    }

    private void maintainConfetti(){
        fireworks.clear();
        fogBanks.clear();

        int target=count(55,90,135);
        while(particles.size()<target)
            particles.add(Particle.confetti(random,getWidth(),getHeight()));
    }

    private void maintainSnow(){
        fireworks.clear();
        fogBanks.clear();

        int target=count(45,80,125);
        while(countKind("SNOWFLAKE")<target)
            particles.add(Particle.snowflake(random,getWidth(),getHeight(),intensity));
    }

    private void maintainSimpleParticles(String kind){
        fireworks.clear();
        fogBanks.clear();

        int target=count(22,38,58);
        while(countKind(kind)<target)
            particles.add(Particle.theme(random,getWidth(),getHeight(),kind));
    }

    private void clearHolidayCollections(){
        fireworks.clear();
        fogBanks.clear();

        // Do not clear active celebration confetti.
        if(System.currentTimeMillis()>=confettiUntil)
            particles.clear();
    }

    private int countKind(String kind){
        int n=0;
        for(Particle p:particles)
            if(kind.equals(p.kind))n++;
        return n;
    }

    private void updateParticles(){
        for(Iterator<Particle> it=particles.iterator();it.hasNext();){
            Particle p=it.next();
            p.update(getWidth(),getHeight());

            if(p.dead)
                it.remove();
        }
    }

    // -----------------------------------------------------------------
    // Halloween fog
    // -----------------------------------------------------------------

    private void createFogBanks(){
        fogBanks.clear();

        int count=count(4,6,8);
        for(int i=0;i<count;i++)
            fogBanks.add(FogBank.create(random,getWidth(),getHeight(),i,count));
    }

    private void updateFog(){
        particles.clear();
        fireworks.clear();

        if(fogBanks.isEmpty())
            createFogBanks();

        for(FogBank fog:fogBanks)
            fog.update(getWidth(),getHeight());
    }

    // -----------------------------------------------------------------
    // Independence Day fireworks
    // -----------------------------------------------------------------

    private void updateFireworks(){
        particles.clear();
        fogBanks.clear();

        long now=System.currentTimeMillis();

        if(nextFireworkAt==0)
            nextFireworkAt=now+700;

        if(now>=nextFireworkAt && fireworks.size()<count(2,3,4)){
            fireworks.add(Firework.launch(random,getWidth(),getHeight()));

            int minDelay=switch(intensity){
                case "HIGH" -> 700;
                case "MEDIUM" -> 1100;
                default -> 1500;
            };
            nextFireworkAt=now+minDelay+random.nextInt(650);
        }

        for(Iterator<Firework> it=fireworks.iterator();it.hasNext();){
            Firework f=it.next();
            f.update(random,getWidth(),getHeight());

            if(f.dead)
                it.remove();
        }
    }

    // -----------------------------------------------------------------
    // Painting
    // -----------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(severeSuppressed)return;

        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        boolean confetti=System.currentTimeMillis()<confettiUntil;

        if(!confetti && themeEffectsEnabled){
            if(theme==AppTheme.HALLOWEEN){
                paintFog(g2);
                paintHalloweenLights(g2);
            }

            if(theme==AppTheme.INDEPENDENCE)
                paintFireworks(g2);
        }

        paintParticles(g2,confetti);

        if(!confetti && themeEffectsEnabled
                && (theme==AppTheme.CHRISTMAS || theme==AppTheme.WINTER_FROST)){
            paintFrost(g2);
        }

        g2.dispose();
    }

    private void paintParticles(Graphics2D g2,boolean confetti){
        for(Particle p:particles){
            if(confetti || "CONFETTI".equals(p.kind)){
                g2.setColor(p.color);
                g2.rotate(p.rotation,p.x,p.y);
                g2.fillRoundRect(
                        (int)p.x,(int)p.y,
                        p.size,Math.max(3,p.size/2),
                        2,2
                );
                g2.rotate(-p.rotation,p.x,p.y);
                continue;
            }

            switch(p.kind){
                case "SNOWFLAKE" -> drawSnowflake(g2,p);
                case "LEAF" -> drawLeaf(g2,p);
                case "HEART" -> drawHeart(g2,(int)p.x,(int)p.y,p.size,p.color);
                case "SHAMROCK" -> drawShamrock(g2,(int)p.x,(int)p.y,p.size,p.color);
            }
        }
    }

    private void drawSnowflake(Graphics2D g2,Particle p){
        Graphics2D f=(Graphics2D)g2.create();

        f.translate(p.x,p.y);
        f.rotate(p.rotation);

        float alpha=Math.max(.25f,Math.min(.95f,p.alpha));
        f.setComposite(AlphaComposite.SrcOver.derive(alpha));
        f.setColor(new Color(245,251,255));

        float stroke=Math.max(1f,p.size/9f);
        f.setStroke(new BasicStroke(
                stroke,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        double radius=p.size/2.0;

        // Six-point snowflake with small branch details.
        for(int arm=0;arm<6;arm++){
            double angle=Math.PI*2*arm/6.0;
            double ex=Math.cos(angle)*radius;
            double ey=Math.sin(angle)*radius;

            f.draw(new Line2D.Double(0,0,ex,ey));

            double branchStart=radius*.55;
            double bx=Math.cos(angle)*branchStart;
            double by=Math.sin(angle)*branchStart;
            double branchLen=radius*.25;

            double a1=angle+Math.PI*.72;
            double a2=angle-Math.PI*.72;

            f.draw(new Line2D.Double(
                    bx,by,
                    bx+Math.cos(a1)*branchLen,
                    by+Math.sin(a1)*branchLen
            ));
            f.draw(new Line2D.Double(
                    bx,by,
                    bx+Math.cos(a2)*branchLen,
                    by+Math.sin(a2)*branchLen
            ));
        }

        f.dispose();
    }

    private void paintFrost(Graphics2D g2){
        int w=getWidth();
        int h=getHeight();
        if(w<=0||h<=0)return;

        Graphics2D f=(Graphics2D)g2.create();

        int depth=switch(intensity){
            case "HIGH" -> 54;
            case "MEDIUM" -> 42;
            default -> 30;
        };

        // Soft translucent frost gradients around all four edges.
        GradientPaint top=new GradientPaint(
                0,0,new Color(225,244,255,125),
                0,depth,new Color(225,244,255,0)
        );
        f.setPaint(top);
        f.fillRect(0,0,w,depth);

        GradientPaint bottom=new GradientPaint(
                0,h,new Color(225,244,255,120),
                0,h-depth,new Color(225,244,255,0)
        );
        f.setPaint(bottom);
        f.fillRect(0,h-depth,w,depth);

        GradientPaint left=new GradientPaint(
                0,0,new Color(225,244,255,90),
                depth,0,new Color(225,244,255,0)
        );
        f.setPaint(left);
        f.fillRect(0,0,depth,h);

        GradientPaint right=new GradientPaint(
                w,0,new Color(225,244,255,90),
                w-depth,0,new Color(225,244,255,0)
        );
        f.setPaint(right);
        f.fillRect(w-depth,0,depth,h);

        // Fine crystalline lines concentrated near the corners/edges.
        f.setColor(new Color(240,250,255,125));
        f.setStroke(new BasicStroke(1.15f));

        int crystals=count(18,30,45);
        Random edgeRandom=new Random(31); // stable pattern, avoids flicker

        for(int i=0;i<crystals;i++){
            int x=edgeRandom.nextBoolean()
                    ?edgeRandom.nextInt(Math.max(1,Math.min(w,220)))
                    :Math.max(0,w-1-edgeRandom.nextInt(Math.max(1,Math.min(w,220))));
            int y=edgeRandom.nextBoolean()
                    ?edgeRandom.nextInt(Math.max(1,Math.min(h,120)))
                    :Math.max(0,h-1-edgeRandom.nextInt(Math.max(1,Math.min(h,120))));

            int len=8+edgeRandom.nextInt(20);

            f.drawLine(x,y,x+len,y);
            f.drawLine(x,y,x,y+len/2);

            if(edgeRandom.nextBoolean())
                f.drawLine(x,y,x-len/2,y+len/2);
        }

        f.dispose();
    }

    private void paintFog(Graphics2D g2){
        for(FogBank fog:fogBanks)
            fog.paint(g2);
    }

    /**
     * Orange/purple perimeter string lights for the Halloween theme.
     * A small sinusoidal brightness shift gives them a slow, tasteful twinkle.
     */
    private void paintHalloweenLights(Graphics2D g2){
        int w=getWidth();
        int h=getHeight();
        if(w<=0||h<=0)return;

        Graphics2D l=(Graphics2D)g2.create();
        l.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        int spacing=switch(intensity){
            case "HIGH" -> 38;
            case "MEDIUM" -> 46;
            default -> 56;
        };

        long now=System.currentTimeMillis();
        Color orange=new Color(255,126,22);
        Color purple=new Color(170,76,235);

        // Wire around the screen perimeter.
        l.setColor(new Color(55,45,60,165));
        l.setStroke(new BasicStroke(2f));
        l.drawRoundRect(12,12,Math.max(1,w-25),Math.max(1,h-25),16,16);

        int index=0;

        // Top and bottom.
        for(int x=26;x<w-26;x+=spacing){
            drawHalloweenBulb(l,x,15,index++,now,orange,purple,false);
            drawHalloweenBulb(l,x,h-16,index++,now,orange,purple,true);
        }

        // Left and right.
        for(int y=28;y<h-28;y+=spacing){
            drawHalloweenBulb(l,15,y,index++,now,orange,purple,false);
            drawHalloweenBulb(l,w-16,y,index++,now,orange,purple,true);
        }

        l.dispose();
    }

    private static void drawHalloweenBulb(
            Graphics2D g,
            int x,
            int y,
            int index,
            long now,
            Color orange,
            Color purple,
            boolean alternatePhase
    ){
        Color base=((index+(alternatePhase?1:0))%2==0)?orange:purple;

        double pulse=.78+.22*Math.sin(now/650.0+index*.73);
        int alpha=(int)(125+105*pulse);

        // Glow halo.
        int halo=18;
        RadialGradientPaint glow=new RadialGradientPaint(
                new Point2D.Double(x,y),
                halo,
                new float[]{0f,.35f,1f},
                new Color[]{
                        new Color(base.getRed(),base.getGreen(),base.getBlue(),Math.min(180,alpha)),
                        new Color(base.getRed(),base.getGreen(),base.getBlue(),65),
                        new Color(base.getRed(),base.getGreen(),base.getBlue(),0)
                }
        );

        g.setPaint(glow);
        g.fill(new Ellipse2D.Double(x-halo,y-halo,halo*2,halo*2));

        // Socket.
        g.setColor(new Color(45,42,47,220));
        g.fillRoundRect(x-4,y-7,8,6,3,3);

        // Bulb.
        g.setColor(new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                Math.min(255,alpha+55)
        ));
        g.fillOval(x-5,y-2,10,13);

        // Highlight.
        g.setColor(new Color(255,255,255,135));
        g.fillOval(x-2,y,3,4);
    }

    private void paintFireworks(Graphics2D g2){
        for(Firework f:fireworks)
            f.paint(g2);
    }

    private static void drawLeaf(Graphics2D g,Particle p){
        Graphics2D l=(Graphics2D)g.create();
        l.translate(p.x,p.y);
        l.rotate(p.rotation);

        l.setColor(p.color);
        l.setStroke(new BasicStroke(1.1f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));

        int variant=p.variant%3;

        if(variant==0){
            // Maple-inspired silhouette.
            Path2D maple=new Path2D.Double();
            double s=p.size;
            maple.moveTo(0,-s*.55);
            maple.lineTo(s*.13,-s*.24);
            maple.lineTo(s*.34,-s*.34);
            maple.lineTo(s*.25,-s*.08);
            maple.lineTo(s*.48,0);
            maple.lineTo(s*.18,s*.12);
            maple.lineTo(s*.20,s*.42);
            maple.lineTo(0,s*.24);
            maple.lineTo(-s*.20,s*.42);
            maple.lineTo(-s*.18,s*.12);
            maple.lineTo(-s*.48,0);
            maple.lineTo(-s*.25,-s*.08);
            maple.lineTo(-s*.34,-s*.34);
            maple.lineTo(-s*.13,-s*.24);
            maple.closePath();
            l.fill(maple);

        }else if(variant==1){
            // Oak-inspired lobed leaf.
            double s=p.size;
            Path2D oak=new Path2D.Double();
            oak.moveTo(0,-s*.55);
            oak.curveTo(s*.18,-s*.48,s*.28,-s*.32,s*.17,-s*.20);
            oak.curveTo(s*.38,-s*.18,s*.40,s*.02,s*.18,s*.04);
            oak.curveTo(s*.35,s*.14,s*.27,s*.31,s*.10,s*.28);
            oak.curveTo(s*.16,s*.43,s*.07,s*.52,0,s*.55);
            oak.curveTo(-s*.07,s*.52,-s*.16,s*.43,-s*.10,s*.28);
            oak.curveTo(-s*.27,s*.31,-s*.35,s*.14,-s*.18,s*.04);
            oak.curveTo(-s*.40,s*.02,-s*.38,-s*.18,-s*.17,-s*.20);
            oak.curveTo(-s*.28,-s*.32,-s*.18,-s*.48,0,-s*.55);
            oak.closePath();
            l.fill(oak);

        }else{
            // Simple pointed autumn leaf.
            double s=p.size;
            Path2D pointed=new Path2D.Double();
            pointed.moveTo(0,-s*.58);
            pointed.curveTo(s*.48,-s*.25,s*.42,s*.25,0,s*.55);
            pointed.curveTo(-s*.42,s*.25,-s*.48,-s*.25,0,-s*.58);
            pointed.closePath();
            l.fill(pointed);
        }

        // Central vein and short stem.
        l.setColor(new Color(105,65,32,145));
        l.draw(new Line2D.Double(0,-p.size*.42,0,p.size*.48));
        l.draw(new Line2D.Double(0,p.size*.42,0,p.size*.68));

        l.dispose();
    }

    private static void drawHeart(Graphics2D g,int x,int y,int s,Color c){
        g.setColor(c);

        int half=Math.max(4,s/2);
        g.fillOval(x,y,half,half);
        g.fillOval(x+half-1,y,half,half);

        Polygon p=new Polygon(
                new int[]{x,x+s,x+s/2},
                new int[]{y+half/2,y+half/2,y+s},
                3
        );
        g.fillPolygon(p);
    }

    private static void drawShamrock(Graphics2D g,int x,int y,int s,Color c){
        g.setColor(c);

        int d=Math.max(4,s/2);
        g.fillOval(x+d/2,y,d,d);
        g.fillOval(x,y+d/2,d,d);
        g.fillOval(x+d,y+d/2,d,d);
        g.fillRect(x+d-1,y+d,d/3+1,s);
    }

    private int count(int low,int medium,int high){
        return switch(intensity){
            case "HIGH" -> high;
            case "MEDIUM" -> medium;
            default -> low;
        };
    }

    // -----------------------------------------------------------------
    // Shared drifting particle
    // -----------------------------------------------------------------

    private static final class Particle {
        double x,y,dx,dy,rotation,dr,swayPhase,swaySpeed;
        int size;
        int variant;
        Color color;
        String kind;
        boolean dead;
        float alpha=1f;

        static Particle confetti(Random r,int w,int h){
            Particle p=new Particle();
            p.kind="CONFETTI";
            p.x=r.nextInt(Math.max(1,w));
            p.y=-r.nextInt(Math.max(30,h/3+1));
            p.dx=-1.4+r.nextDouble()*2.8;
            p.dy=2.3+r.nextDouble()*3.2;
            p.dr=-.15+r.nextDouble()*.30;
            p.rotation=r.nextDouble()*Math.PI;
            p.size=7+r.nextInt(8);

            Color[] colors={
                    new Color(255,70,70),
                    new Color(65,160,255),
                    new Color(255,205,55),
                    new Color(75,205,110),
                    new Color(210,90,230)
            };

            p.color=colors[r.nextInt(colors.length)];
            return p;
        }

        static Particle snowflake(Random r,int w,int h,String intensity){
            Particle p=new Particle();
            p.kind="SNOWFLAKE";
            p.x=r.nextInt(Math.max(1,w));
            p.y=-r.nextInt(Math.max(40,h+1));

            p.size=switch(intensity){
                case "HIGH" -> 8+r.nextInt(14);
                case "MEDIUM" -> 7+r.nextInt(12);
                default -> 6+r.nextInt(10);
            };

            // Larger flakes appear slightly closer and fall more quickly.
            double depth=p.size/20.0;
            p.dy=.55+depth*1.75+r.nextDouble()*.55;
            p.dx=-.15+r.nextDouble()*.30;

            p.swayPhase=r.nextDouble()*Math.PI*2;
            p.swaySpeed=.018+r.nextDouble()*.025;

            p.rotation=r.nextDouble()*Math.PI;
            p.dr=-.012+r.nextDouble()*.024;

            p.alpha=(float)(.45+r.nextDouble()*.5);
            p.color=Color.WHITE;

            return p;
        }

        static Particle theme(Random r,int w,int h,String kind){
            Particle p=new Particle();
            p.kind=kind;
            p.x=r.nextInt(Math.max(1,w));
            p.y=-r.nextInt(Math.max(20,h+1));
            p.dx=-.45+r.nextDouble()*.9;
            p.dy=.65+r.nextDouble()*1.5;
            p.size=7+r.nextInt(11);
            p.rotation=r.nextDouble()*Math.PI;
            p.dr=-.035+r.nextDouble()*.07;

            if("LEAF".equals(kind)){
                p.variant=r.nextInt(3);

                Color[] autumn={
                        new Color(201,82,36,190),
                        new Color(228,128,36,190),
                        new Color(239,173,54,190),
                        new Color(165,77,38,190),
                        new Color(183,123,48,190),
                        new Color(132,84,42,185)
                };
                p.color=autumn[r.nextInt(autumn.length)];

                // Leaves float more laterally and tumble more than other icons.
                p.dx=-.85+r.nextDouble()*1.7;
                p.dy=.45+r.nextDouble()*1.15;
                p.dr=-.065+r.nextDouble()*.13;
                p.size=10+r.nextInt(14);
                p.swayPhase=r.nextDouble()*Math.PI*2;
                p.swaySpeed=.018+r.nextDouble()*.030;
            }else{
                p.color=switch(kind){
                    case "HEART" -> new Color(225,70,120,150);
                    case "SHAMROCK" -> new Color(55,190,105,150);
                    default -> Color.WHITE;
                };
            }

            return p;
        }

        void update(int w,int h){
            if("SNOWFLAKE".equals(kind)){
                swayPhase+=swaySpeed;
                x+=dx+Math.sin(swayPhase)*.32;
            }else if("LEAF".equals(kind)){
                swayPhase+=swaySpeed;
                x+=dx+Math.sin(swayPhase)*.55;
            }else{
                x+=dx;
            }

            y+=dy;
            rotation+=dr;

            if(y>h+55||x<-70||x>w+70)
                dead=true;
        }
    }

    // -----------------------------------------------------------------
    // Layered rolling Halloween fog
    // -----------------------------------------------------------------

    private static final class FogBank {
        double x;
        double y;
        double speed;
        double width;
        double height;
        double phase;
        double phase2;
        float alpha;
        float depth;

        static FogBank create(Random r,int w,int h,int index,int total){
            FogBank f=new FogBank();

            // Wide banks overlap heavily, which removes the appearance of
            // individual "cloud blobs" and creates a continuous rolling layer.
            f.width=Math.max(620,w*(.62+r.nextDouble()*.28));
            f.height=135+r.nextDouble()*150;

            double pct=index/(double)Math.max(1,total-1);
            f.y=h*(.28+pct*.62)-r.nextDouble()*55;

            f.x=-f.width+r.nextDouble()*Math.max(1,w+f.width);
            f.depth=(float)(.55+.45*pct);

            // Near fog moves a little faster than distant layers.
            f.speed=.15+r.nextDouble()*.22+f.depth*.10;
            f.phase=r.nextDouble()*Math.PI*2;
            f.phase2=r.nextDouble()*Math.PI*2;
            f.alpha=(float)(.035+r.nextDouble()*.035+f.depth*.025);

            return f;
        }

        void update(int w,int h){
            x+=speed;
            phase+=.0045+depth*.0025;
            phase2+=.0028+depth*.0017;

            // Gentle vertical turbulence rather than obvious up/down bouncing.
            y+=Math.sin(phase2)*.025;

            if(x>w+width*.18)
                x=-width*1.08;
        }

        void paint(Graphics2D g){
            Graphics2D f=(Graphics2D)g.create();
            f.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            // Large translucent body gradient.
            float bodyAlpha=Math.min(.14f,alpha*1.28f);
            LinearGradientPaint body=new LinearGradientPaint(
                    new Point2D.Double(0,y-height*.65),
                    new Point2D.Double(0,y+height*.70),
                    new float[]{0f,.18f,.52f,.82f,1f},
                    new Color[]{
                            new Color(205,210,220,0),
                            new Color(202,207,216,(int)(255*bodyAlpha*.35)),
                            new Color(190,196,207,(int)(255*bodyAlpha)),
                            new Color(180,187,200,(int)(255*bodyAlpha*.65)),
                            new Color(175,182,195,0)
                    }
            );

            f.setPaint(body);
            f.fill(new RoundRectangle2D.Double(
                    x,
                    y-height*.62,
                    width,
                    height*1.25,
                    height,
                    height
            ));

            /*
             * Soft overlapping density patches. These use broad elliptical
             * gradients with very low opacity; because they overlap the main
             * body, the viewer perceives rolling fog rather than separate blobs.
             */
            int patches=11;
            for(int i=0;i<patches;i++){
                double pct=i/(double)(patches-1);
                double cx=x+pct*width;
                double wave1=Math.sin(phase+i*.62)*height*.10;
                double wave2=Math.sin(phase2+i*.37)*height*.05;
                double cy=y+wave1+wave2;

                double radius=width*(.095+.025*Math.sin(i*.84+phase));
                radius=Math.max(85,radius);

                int innerAlpha=(int)Math.max(5,Math.min(28,255*alpha*.86));

                RadialGradientPaint mist=new RadialGradientPaint(
                        new Point2D.Double(cx,cy),
                        (float)radius,
                        new float[]{0f,.38f,.72f,1f},
                        new Color[]{
                                new Color(215,219,226,innerAlpha),
                                new Color(202,207,216,(int)(innerAlpha*.72)),
                                new Color(190,196,207,(int)(innerAlpha*.32)),
                                new Color(180,185,197,0)
                        }
                );

                f.setPaint(mist);
                f.fill(new Ellipse2D.Double(
                        cx-radius,
                        cy-radius*.43,
                        radius*2,
                        radius*.86
                ));
            }

            // Thin wisps drift slightly ahead/behind the main bank.
            f.setStroke(new BasicStroke(
                    2.2f+depth*1.2f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            f.setColor(new Color(220,224,232,(int)(255*alpha*.42)));

            Path2D wisp=new Path2D.Double();
            double startX=x+width*.08;
            double baseY=y-height*.10;
            wisp.moveTo(startX,baseY);

            int segments=10;
            for(int i=1;i<=segments;i++){
                double px=x+width*(.08+.84*i/segments);
                double py=baseY
                        +Math.sin(phase+i*.72)*height*.075
                        +Math.sin(phase2+i*.44)*height*.035;
                wisp.lineTo(px,py);
            }

            f.draw(wisp);
            f.dispose();
        }
    }

    // -----------------------------------------------------------------
    // Independence Day firework system
    // -----------------------------------------------------------------

    private static final class Firework {
        private double rocketX;
        private double rocketY;
        private double rocketDY;

        private double targetY;
        private boolean exploded=false;
        private boolean dead=false;

        private final List<Spark> sparks=new ArrayList<>();
        private Color color;
        private Color secondary;

        static Firework launch(Random r,int w,int h){
            Firework f=new Firework();

            f.rocketX=w*.12+r.nextDouble()*Math.max(1,w*.76);
            f.rocketY=h+20;
            f.targetY=h*.12+r.nextDouble()*Math.max(60,h*.42);
            f.rocketDY=-(5.5+r.nextDouble()*2.4);

            Color[] palette={
                    new Color(238,55,65),
                    new Color(75,130,255),
                    new Color(250,250,245),
                    new Color(255,198,60)
            };

            f.color=palette[r.nextInt(palette.length)];
            f.secondary=palette[r.nextInt(palette.length)];

            return f;
        }

        void update(Random r,int w,int h){
            if(!exploded){
                rocketY+=rocketDY;
                rocketDY+=.045;

                if(rocketY<=targetY || rocketDY>=-1.0)
                    explode(r);
            }else{
                boolean any=false;

                for(Spark s:sparks){
                    s.update();

                    if(s.alpha>0.01f && s.y<h+80)
                        any=true;
                }

                if(!any)
                    dead=true;
            }
        }

        private void explode(Random r){
            exploded=true;

            int rays=34+r.nextInt(22);

            for(int i=0;i<rays;i++){
                double angle=Math.PI*2*i/rays+r.nextDouble()*.08;
                double speed=2.2+r.nextDouble()*3.2;

                Spark s=new Spark();
                s.x=rocketX;
                s.y=rocketY;
                s.dx=Math.cos(angle)*speed;
                s.dy=Math.sin(angle)*speed;
                s.alpha=.95f;
                s.size=2+r.nextInt(3);
                s.color=(i%4==0)?secondary:color;

                sparks.add(s);

                // Small trailing spark behind some primary rays.
                if(i%3==0){
                    Spark trail=new Spark();
                    trail.x=rocketX;
                    trail.y=rocketY;
                    trail.dx=Math.cos(angle)*speed*.68;
                    trail.dy=Math.sin(angle)*speed*.68;
                    trail.alpha=.72f;
                    trail.size=2;
                    trail.color=new Color(
                            s.color.getRed(),
                            s.color.getGreen(),
                            s.color.getBlue()
                    );
                    sparks.add(trail);
                }
            }
        }

        void paint(Graphics2D g){
            if(!exploded){
                // Rising launch trail.
                GradientPaint trail=new GradientPaint(
                        (float)rocketX,(float)rocketY,
                        new Color(color.getRed(),color.getGreen(),color.getBlue(),230),
                        (float)rocketX,(float)(rocketY+38),
                        new Color(color.getRed(),color.getGreen(),color.getBlue(),0)
                );

                g.setPaint(trail);
                g.setStroke(new BasicStroke(2.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(
                        rocketX,rocketY,
                        rocketX,rocketY+38
                ));

                g.setColor(new Color(255,245,220,235));
                g.fill(new Ellipse2D.Double(rocketX-2.5,rocketY-2.5,5,5));

                return;
            }

            for(Spark s:sparks){
                if(s.alpha<=0) continue;

                Graphics2D sg=(Graphics2D)g.create();
                sg.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f,Math.min(1f,s.alpha))));

                Color sparkColor=new Color(
                        s.color.getRed(),
                        s.color.getGreen(),
                        s.color.getBlue()
                );

                sg.setColor(sparkColor);
                sg.setStroke(new BasicStroke(
                        Math.max(1f,s.size*.70f),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                sg.draw(new Line2D.Double(
                        s.x,
                        s.y,
                        s.x-s.dx*2.1,
                        s.y-s.dy*2.1
                ));

                sg.fill(new Ellipse2D.Double(
                        s.x-s.size/2.0,
                        s.y-s.size/2.0,
                        s.size,
                        s.size
                ));

                sg.dispose();
            }
        }

        private static final class Spark {
            double x,y,dx,dy;
            float alpha;
            int size;
            Color color;

            void update(){
                x+=dx;
                y+=dy;

                dx*=.985;
                dy=dy*.985+.055; // subtle gravity

                alpha-=.0135f;
            }
        }
    }
}
