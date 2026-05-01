package lagraa_yasser_assil_project.UI;
import javax.swing.plaf.FontUIResource;
import lagraa_yasser_assil_project.UI.components.NavigationPanel;
import lagraa_yasser_assil_project.UI.components.ContentPanel;
import lagraa_yasser_assil_project.UI.components.NavigationController;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.*;

/**
 * MainFrame — The primary application window.
 *
 * Layout:
 *   WEST  → NavigationPanel  (permanent sidebar)
 *   CENTER → ContentPanel    (CardLayout — keeps panels in memory for context saving)
 *
 * Design language: dark academic / institutional — deep navy background,
 * warm gold accents, monospaced headers. Refined & data-dense without
 * feeling like a generic CRUD form.
 */
public class MainFrame extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DEEP        = new Color(0x0F, 0x1A, 0x2E); // dark navy
    public static final Color BG_PANEL       = new Color(0x16, 0x24, 0x3C); // slightly lighter navy
    public static final Color BG_CARD        = new Color(0x1C, 0x2D, 0x48); // card surface
    public static final Color ACCENT_GOLD    = new Color(0xD4, 0xAF, 0x37); // warm gold
    public static final Color ACCENT_BLUE    = new Color(0x4A, 0x9E, 0xFF); // highlight blue
    public static final Color NAV_SELECT     = new Color(0x1E, 0x45, 0x7A); // nav selected bg
    public static final Color TEXT_PRIMARY   = new Color(0xEA, 0xEA, 0xEA);
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x9B, 0xB4);
    public static final Color SUCCESS_GREEN  = new Color(0x2E, 0xCC, 0x71);
    public static final Color DANGER_RED     = new Color(0xE7, 0x4C, 0x3C);
    public static final Color BORDER_SUBTLE  = new Color(0x2A, 0x3F, 0x5F);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    /** Used for section titles, nav labels */
    public static final Font  FONT_TITLE     = new Font("Courier New", Font.BOLD, 13);
    /** Used for table headers, field labels */
    public static final Font  FONT_LABEL     = new Font("Courier New", Font.PLAIN, 12);
    /** Used for body text, input fields */
    public static final Font  FONT_BODY      = new Font("SansSerif", Font.PLAIN, 13);
    /** Larger display font */
    public static final Font  FONT_DISPLAY   = new Font("Courier New", Font.BOLD, 18);

    private final NavigationController controller;
    private final NavigationPanel      navPanel;
    private final ContentPanel         contentPanel;

    public MainFrame() {
        super("Système de Gestion Académique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setPreferredSize(new Dimension(1280, 800));

        // Global look-and-feel tweaks
        applyGlobalUI();

        // Build components
        contentPanel = new ContentPanel();
        controller   = new NavigationController(contentPanel);
        navPanel     = new NavigationPanel(controller);

        // Layout
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DEEP);
        add(navPanel,     BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Title bar area (top strip)
        add(buildTitleBar(), BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────

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

    // ── Global Swing defaults ─────────────────────────────────────────────────

private void applyGlobalUI() {
    FontUIResource dialogFont = new FontUIResource("Segoe UI", Font.PLAIN, 13);

    UIManager.put("Panel.background",              BG_PANEL);
    UIManager.put("Label.foreground",              TEXT_PRIMARY);
    UIManager.put("Label.font",                    dialogFont);        // changed
    UIManager.put("TextField.background",          BG_CARD);
    UIManager.put("TextField.foreground",          TEXT_PRIMARY);
    UIManager.put("TextField.font",                dialogFont);        // added
    UIManager.put("TextField.caretForeground",     ACCENT_GOLD);
    UIManager.put("TextField.border",
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    UIManager.put("ComboBox.background",           BG_CARD);
    UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
    UIManager.put("ComboBox.font",                 dialogFont);        // added
    UIManager.put("ComboBox.selectionBackground",  NAV_SELECT);
    UIManager.put("ComboBox.selectionForeground",  TEXT_PRIMARY);
    UIManager.put("Table.background",              BG_CARD);
    UIManager.put("Table.foreground",              TEXT_PRIMARY);
    UIManager.put("Table.font",                    dialogFont);        // added
    UIManager.put("Table.gridColor",               BORDER_SUBTLE);
    UIManager.put("Table.selectionBackground",     NAV_SELECT);
    UIManager.put("Table.selectionForeground",     TEXT_PRIMARY);
    UIManager.put("TableHeader.background",        BG_PANEL);
    UIManager.put("TableHeader.foreground",        ACCENT_GOLD);
    UIManager.put("TableHeader.font",              dialogFont);        // added
    UIManager.put("ScrollPane.background",         BG_PANEL);
    UIManager.put("Viewport.background",           BG_CARD);
    UIManager.put("ScrollBar.background",          BG_PANEL);
    UIManager.put("ScrollBar.thumb",               BORDER_SUBTLE);
    UIManager.put("OptionPane.background",         BG_PANEL);
    UIManager.put("OptionPane.messageForeground",  TEXT_PRIMARY);
    UIManager.put("Button.background",             NAV_SELECT);
    UIManager.put("Button.foreground",             TEXT_PRIMARY);
    UIManager.put("Button.font",                   dialogFont);        // added
    UIManager.put("Button.border",
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
    UIManager.put("CheckBox.background",           BG_PANEL);
    UIManager.put("CheckBox.foreground",           TEXT_PRIMARY);
    UIManager.put("CheckBox.font",                 dialogFont);        // added
    UIManager.put("RadioButton.background",        BG_PANEL);
    UIManager.put("RadioButton.foreground",        TEXT_PRIMARY);
    UIManager.put("RadioButton.font",              dialogFont);        // added
    UIManager.put("Spinner.background",            BG_CARD);
    UIManager.put("Spinner.foreground",            TEXT_PRIMARY);
    UIManager.put("List.background",               BG_CARD);
    UIManager.put("List.foreground",               TEXT_PRIMARY);
    UIManager.put("List.font",                     dialogFont);        // added
    UIManager.put("List.selectionBackground",      NAV_SELECT);
    UIManager.put("List.selectionForeground",      TEXT_PRIMARY);
    UIManager.put("PasswordField.background",      BG_CARD);
    UIManager.put("PasswordField.foreground",      TEXT_PRIMARY);
    UIManager.put("PasswordField.font",            dialogFont);        // added
}


}