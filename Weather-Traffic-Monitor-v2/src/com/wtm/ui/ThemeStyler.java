package com.wtm.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Recursively applies an AppTheme to Settings and administrative dialogs.
 *
 * Controls that have strongly platform-specific native painting (especially
 * JComboBox on macOS) receive a platform-independent Swing UI so the theme and
 * alignment remain consistent across macOS, Windows and Raspberry Pi OS.
 */
public final class ThemeStyler {
    private ThemeStyler(){}

    private static final int FIELD_HEIGHT=36;

    public static void apply(Component root,AppTheme theme){
        if(root==null||theme==null)return;

        style(root,theme);

        if(root instanceof JScrollPane scroll){
            scroll.getViewport().setBackground(theme.panel());
            scroll.setBackground(theme.panel());
            scroll.setBorder(BorderFactory.createLineBorder(theme.border()));
        }

        if(root instanceof JTable table)
            styleTable(table,theme);

        if(root instanceof Container container){
            for(Component child:container.getComponents())
                apply(child,theme);
        }
    }

    private static void style(Component c,AppTheme t){
        if(c instanceof JDialog || c instanceof JFrame){
            c.setBackground(t.bg());

        }else if(c instanceof JTabbedPane tabs){
            tabs.setBackground(t.bg());
            tabs.setForeground(t.text());

        }else if(c instanceof RoundedPanel rp){
            rp.setBackground(t.panel());
            rp.setForeground(t.text());

        }else if(c instanceof JPanel p){
            p.setBackground(t.bg());
            p.setForeground(t.text());

        }else if(c instanceof JLabel l){
            l.setForeground(t.text());

        }else if(c instanceof JTextArea a){
            a.setForeground(t.text());
            if(a.isOpaque())a.setBackground(t.panel2());
            a.setCaretColor(t.text());
            a.setSelectionColor(t.accent());
            a.setSelectedTextColor(bestText(t.accent()));

        }else if(c instanceof JPasswordField f){
            styleTextField(f,t);

        }else if(c instanceof JTextField f){
            styleTextField(f,t);

        }else if(c instanceof JCheckBox b){
            b.setBackground(t.bg());
            b.setForeground(t.text());
            b.setOpaque(true);

        }else if(c instanceof JRadioButton b){
            b.setBackground(t.bg());
            b.setForeground(t.text());
            b.setOpaque(true);

        }else if(c instanceof JComboBox<?> combo){
            styleComboBox(combo,t);

        }else if(c instanceof JButton b){
            b.setBackground(t.panel2());
            b.setForeground(t.text());
            b.setOpaque(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(t.border(),1,true),
                    BorderFactory.createEmptyBorder(6,11,6,11)
            ));

        }else if(c instanceof JSlider slider){
            slider.setBackground(t.bg());
            slider.setForeground(t.text());

        }else if(c instanceof JSeparator sep){
            sep.setBackground(t.border());
            sep.setForeground(t.border());
        }
    }

    private static void styleTextField(JTextField f,AppTheme t){
        f.setBackground(t.panel2());
        f.setForeground(t.text());
        f.setCaretColor(t.text());
        f.setSelectionColor(t.accent());
        f.setSelectedTextColor(bestText(t.accent()));
        f.setBorder(inputBorder(t.border()));

        Dimension preferred=f.getPreferredSize();
        f.setPreferredSize(new Dimension(
                preferred==null?200:preferred.width,
                FIELD_HEIGHT
        ));
        f.setMinimumSize(new Dimension(80,FIELD_HEIGHT));
    }

    private static void styleComboBox(JComboBox<?> combo,AppTheme t){
        /*
         * Install our own UI before applying renderer/border. This prevents the
         * macOS native combo implementation from drawing a white interior strip
         * or vertically offsetting the selected value.
         */
        combo.setUI(new ThemedComboBoxUI(t));
        combo.setBackground(t.panel2());
        combo.setForeground(t.text());
        combo.setOpaque(true);
        combo.setRenderer(new ComboRenderer(t));

        Dimension preferred=combo.getPreferredSize();
        combo.setPreferredSize(new Dimension(
                preferred==null?160:Math.max(120,preferred.width),
                FIELD_HEIGHT
        ));
        combo.setMinimumSize(new Dimension(100,FIELD_HEIGHT));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE,FIELD_HEIGHT));
    }

    public static void styleTable(JTable table,AppTheme t){
        table.setBackground(t.panel());
        table.setForeground(t.text());
        table.setGridColor(t.border());
        table.setSelectionBackground(t.accent());
        table.setSelectionForeground(bestText(t.accent()));

        JTableHeader header=table.getTableHeader();
        if(header!=null){
            header.setBackground(t.panel2());
            header.setForeground(t.text());
            header.setBorder(BorderFactory.createMatteBorder(0,0,1,0,t.border()));
        }

        if(table.getParent() instanceof JViewport vp)
            vp.setBackground(t.panel());
    }

    private static Border inputBorder(Color c){
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c,1,true),
                BorderFactory.createEmptyBorder(5,8,5,8)
        );
    }

    private static Color bestText(Color bg){
        double lum=(0.299*bg.getRed()+0.587*bg.getGreen()+0.114*bg.getBlue())/255.0;
        return lum>0.62?Color.BLACK:Color.WHITE;
    }

    /**
     * Renderer for both the selected item and the popup list.
     *
     * Explicit padding and a fixed minimum cell height keep every dropdown
     * aligned regardless of font metrics or operating system.
     */
    private static final class ComboRenderer extends DefaultListCellRenderer {
        private final AppTheme theme;

        ComboRenderer(AppTheme theme){
            this.theme=theme;
            setBorder(BorderFactory.createEmptyBorder(5,9,5,9));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean selected,
                boolean focus
        ){
            JLabel l=(JLabel)super.getListCellRendererComponent(
                    list,value,index,selected,focus
            );

            l.setOpaque(true);
            l.setBorder(BorderFactory.createEmptyBorder(5,9,5,9));
            l.setVerticalAlignment(SwingConstants.CENTER);
            l.setHorizontalAlignment(SwingConstants.LEFT);

            if(selected){
                l.setBackground(theme.accent());
                l.setForeground(bestText(theme.accent()));
            }else{
                l.setBackground(theme.panel2());
                l.setForeground(theme.text());
            }

            Dimension p=l.getPreferredSize();
            if(p.height<32)
                l.setPreferredSize(new Dimension(p.width,32));

            return l;
        }
    }
}
