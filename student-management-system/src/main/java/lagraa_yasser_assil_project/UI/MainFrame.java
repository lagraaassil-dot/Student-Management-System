package lagraa_yasser_assil_project.UI;
import javax.swing.plaf.FontUIResource;
import lagraa_yasser_assil_project.UI.components.NavigationPanel;
import lagraa_yasser_assil_project.UI.components.ContentPanel;
import lagraa_yasser_assil_project.UI.components.NavigationController;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.*;

// Root window. Owns the colour palette and font constants used everywhere.
public class MainFrame extends JFrame {

    
    public static final Color BG_DEEP        = new Color(0x0F, 0x1A, 0x2E); 
    public static final Color BG_PANEL       = new Color(0x16, 0x24, 0x3C); 
    public static final Color BG_CARD        = new Color(0x1C, 0x2D, 0x48); 
    public static final Color ACCENT_GOLD    = new Color(0xD4, 0xAF, 0x37); 
    public static final Color ACCENT_BLUE    = new Color(0x4A, 0x9E, 0xFF); 
    public static final Color NAV_SELECT     = new Color(0x1E, 0x45, 0x7A); 
    public static final Color TEXT_PRIMARY   = new Color(0xEA, 0xEA, 0xEA);
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x9B, 0xB4);
    public static final Color SUCCESS_GREEN  = new Color(0x2E, 0xCC, 0x71);
    public static final Color DANGER_RED     = new Color(0xE7, 0x4C, 0x3C);
    public static final Color BORDER_SUBTLE  = new Color(0x2A, 0x3F, 0x5F);

    
    
    public static final Font  FONT_TITLE     = new Font("Courier New", Font.BOLD, 13);
    
    public static final Font  FONT_LABEL     = new Font("Courier New", Font.PLAIN, 12);
    
    public static final Font  FONT_BODY      = new Font("SansSerif", Font.PLAIN, 13);
    
    public static final Font  FONT_DISPLAY   = new Font("Courier New", Font.BOLD, 18);

    private final NavigationController controller;
    private final NavigationPanel      navPanel;
    private final ContentPanel         contentPanel;

    public MainFrame() {
        super("Système de Gestion Académique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));

        
        applyGlobalUI();

        
        contentPanel = new ContentPanel();
        controller   = new NavigationController(contentPanel);
        navPanel     = new NavigationPanel(controller);

        
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DEEP);
        add(navPanel,     BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        
        add(buildTitleBar(), BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DEEP);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
        bar.setPreferredSize(new Dimension(0, 48));

        JLabel title = new JLabel(" ♦ SYSTEME DE GESTION ACADEMIQUE");
        title.setFont(FONT_DISPLAY);
        title.setForeground(ACCENT_GOLD);
        bar.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("USTHB — Université des Sciences et de la Technologie Houari Boumediene  ");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_SECONDARY);
        bar.add(sub, BorderLayout.EAST);

        return bar;
    }

    

private void applyGlobalUI() {
    FontUIResource dialogFont = new FontUIResource("Segoe UI", Font.PLAIN, 13);

    UIManager.put("Panel.background",              BG_PANEL);
    UIManager.put("Label.foreground",              TEXT_PRIMARY);
    UIManager.put("Label.font",                    dialogFont);        
    UIManager.put("TextField.background",          BG_CARD);
    UIManager.put("TextField.foreground",          TEXT_PRIMARY);
    UIManager.put("TextField.font",                dialogFont);        
    UIManager.put("TextField.caretForeground",     ACCENT_GOLD);
    UIManager.put("TextField.border",
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    UIManager.put("ComboBox.background",           BG_CARD);
    UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
    UIManager.put("ComboBox.font",                 dialogFont);        
    UIManager.put("ComboBox.selectionBackground",  NAV_SELECT);
    UIManager.put("ComboBox.selectionForeground",  TEXT_PRIMARY);
    UIManager.put("Table.background",              BG_CARD);
    UIManager.put("Table.foreground",              TEXT_PRIMARY);
    UIManager.put("Table.font",                    dialogFont);        
    UIManager.put("Table.gridColor",               BORDER_SUBTLE);
    UIManager.put("Table.selectionBackground",     NAV_SELECT);
    UIManager.put("Table.selectionForeground",     TEXT_PRIMARY);
    UIManager.put("TableHeader.background",        BG_PANEL);
    UIManager.put("TableHeader.foreground",        ACCENT_GOLD);
    UIManager.put("TableHeader.font",              dialogFont);        
    UIManager.put("ScrollPane.background",         BG_PANEL);
    UIManager.put("Viewport.background",           BG_CARD);
    UIManager.put("ScrollBar.background",          BG_PANEL);
    UIManager.put("ScrollBar.thumb",               BORDER_SUBTLE);
    UIManager.put("OptionPane.background",         BG_PANEL);
    UIManager.put("OptionPane.messageForeground",  TEXT_PRIMARY);
    UIManager.put("Button.background",             NAV_SELECT);
    UIManager.put("Button.foreground",             TEXT_PRIMARY);
    UIManager.put("Button.font",                   dialogFont);        
    UIManager.put("Button.border",
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
    UIManager.put("CheckBox.background",           BG_PANEL);
    UIManager.put("CheckBox.foreground",           TEXT_PRIMARY);
    UIManager.put("CheckBox.font",                 dialogFont);        
    UIManager.put("RadioButton.background",        BG_PANEL);
    UIManager.put("RadioButton.foreground",        TEXT_PRIMARY);
    UIManager.put("RadioButton.font",              dialogFont);        
    UIManager.put("Spinner.background",            BG_CARD);
    UIManager.put("Spinner.foreground",            TEXT_PRIMARY);
    UIManager.put("List.background",               BG_CARD);
    UIManager.put("List.foreground",               TEXT_PRIMARY);
    UIManager.put("List.font",                     dialogFont);        
    UIManager.put("List.selectionBackground",      NAV_SELECT);
    UIManager.put("List.selectionForeground",      TEXT_PRIMARY);
    UIManager.put("PasswordField.background",      BG_CARD);
    UIManager.put("PasswordField.foreground",      TEXT_PRIMARY);
    UIManager.put("PasswordField.font",            dialogFont);        
}

}