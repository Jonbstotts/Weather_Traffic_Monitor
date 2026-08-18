package com.wtm.app;

import com.wtm.config.*;
import com.wtm.security.AuthService;
import com.wtm.ui.*;

import javax.swing.*;
import java.awt.*;

/** Application entry point. */
public final class Main {
    private Main(){}

    public static void main(String[] args){
        System.setProperty(
                "apple.awt.application.name",
                "Weather & Traffic Monitor"
        );

        SwingUtilities.invokeLater(()->{
            configureDesktopLookAndFeel();

            AppConfig config=ConfigService.load();
            AppTheme theme=AppTheme.fromId(config.themeId);
            Theme.setActive(theme.id());

            if(config.loginRequiredOnStartup){
                if(!AuthService.hasPassword()){
                    JOptionPane.showMessageDialog(
                            null,
                            "Startup login is enabled, but the administrator "
                            +"password file is unavailable. For security, the "
                            +"dashboard will not start.",
                            "Authentication Configuration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                boolean unlocked=LoginDialog.authenticate(
                        null,
                        "Weather & Traffic Monitor Login",
                        "Enter the administrator password to start the dashboard.",
                        theme
                );

                if(!unlocked)return;
            }

            DashboardFrame frame=new DashboardFrame(config);
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
    }

    private static void configureDesktopLookAndFeel(){
        try{
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        }catch(Exception ignored){}

        Font base=new Font(Font.SANS_SERIF,Font.PLAIN,14);
        for(Object key:UIManager.getDefaults().keySet()){
            if(key.toString().toLowerCase().endsWith(".font"))
                UIManager.put(key,base);
        }
    }
}
