package com.wtm.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * Platform-independent JComboBox UI for Settings.
 *
 * macOS Aqua/Nimbus combo boxes can continue painting native white interior
 * regions after custom application colors are applied. This UI deliberately
 * replaces that native painting so themed combo boxes have consistent sizing,
 * vertical alignment, popup colors, and arrow buttons on macOS, Windows, and
 * Linux/Raspberry Pi OS.
 */
public final class ThemedComboBoxUI extends BasicComboBoxUI {
    private final AppTheme theme;

    public ThemedComboBoxUI(AppTheme theme){
        this.theme=theme;
    }

    @Override
    protected JButton createArrowButton(){
        BasicArrowButton button=new BasicArrowButton(
                SwingConstants.SOUTH,
                theme.accent(),
                theme.accent(),
                bestText(theme.accent()),
                theme.accent()
        );
        button.setName("ComboBox.arrowButton");
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
        button.setPreferredSize(new Dimension(34,34));
        return button;
    }

    @Override
    public void installUI(JComponent c){
        super.installUI(c);

        if(c instanceof JComboBox<?> combo){
            combo.setOpaque(true);
            combo.setBackground(theme.panel2());
            combo.setForeground(theme.text());
            combo.setMinimumSize(new Dimension(100,36));
            combo.setPreferredSize(new Dimension(
                    Math.max(120,combo.getPreferredSize().width),
                    36
            ));
            combo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(theme.border(),1,true),
                    new EmptyBorder(2,6,2,4)
            ));

            if(combo.getEditor()!=null && combo.getEditor().getEditorComponent() instanceof JComponent editor){
                editor.setBorder(new EmptyBorder(4,7,4,7));
                editor.setBackground(theme.panel2());
                editor.setForeground(theme.text());
            }
        }
    }

    @Override
    public void paintCurrentValueBackground(
            Graphics g,
            Rectangle bounds,
            boolean hasFocus
    ){
        g.setColor(theme.panel2());
        g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
    }

    @Override
    protected Rectangle rectangleForCurrentValue(){
        Rectangle r=super.rectangleForCurrentValue();

        // Keep text vertically centered with a little breathing room above/below.
        r.y+=2;
        r.height=Math.max(1,r.height-4);
        return r;
    }

    private static Color bestText(Color bg){
        double lum=(0.299*bg.getRed()+0.587*bg.getGreen()+0.114*bg.getBlue())/255.0;
        return lum>0.62?Color.BLACK:Color.WHITE;
    }
}
