package com.wtm.app;

import com.wtm.config.*;
import com.wtm.ui.DashboardFrame;
import javax.swing.*;
import java.awt.*;

/** Application entry point. */
public final class Main {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Weather & Traffic Monitor");
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
            for (Object key : UIManager.getDefaults().keySet()) if (key.toString().toLowerCase().endsWith(".font")) UIManager.put(key, base);
            AppConfig cfg = ConfigService.load();
            DashboardFrame frame = new DashboardFrame(cfg);
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
    }
}
