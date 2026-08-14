package com.wtm.ui;

import java.awt.*;

/**
 * Central visual design system for the dashboard.
 *
 * Keeping all palette values here makes light and dark mode visually consistent
 * and prevents individual widgets from inventing their own colors.
 */
public final class Theme {
    private Theme() {}

    public static Color bg(boolean dark){
        return dark ? new Color(11,16,22) : new Color(244,247,250);
    }

    public static Color panel(boolean dark){
        return dark ? new Color(22,29,37) : Color.WHITE;
    }

    public static Color panel2(boolean dark){
        return dark ? new Color(27,35,44) : new Color(249,251,253);
    }

    public static Color panelHover(boolean dark){
        return dark ? new Color(31,40,50) : new Color(246,249,252);
    }

    public static Color border(boolean dark){
        return dark ? new Color(48,59,71) : new Color(220,226,232);
    }

    public static Color text(boolean dark){
        return dark ? new Color(242,245,248) : new Color(28,35,43);
    }

    public static Color muted(boolean dark){
        return dark ? new Color(168,178,188) : new Color(92,104,116);
    }

    public static Color accent(){ return new Color(62,154,255); }
    public static Color good(){ return new Color(54,177,91); }
    public static Color warn(){ return new Color(242,177,30); }
    public static Color danger(){ return new Color(229,72,77); }
    public static Color sun(){ return new Color(255,183,0); }
    public static Color cloud(){ return new Color(179,215,232); }
    public static Color rain(){ return new Color(60,151,229); }

    /** Converts a color to an HTML #RRGGBB string for Swing HTML labels. */
    public static String hex(Color c){
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
