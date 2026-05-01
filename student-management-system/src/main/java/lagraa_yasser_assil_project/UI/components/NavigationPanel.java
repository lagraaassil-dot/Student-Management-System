package lagraa_yasser_assil_project.UI.components;

import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController.Section;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * NavigationPanel — the permanent left sidebar.
 *
 * Renders one button per Section. The active section button is highlighted
 * with a left accent bar + light navy fill. Hovering shows a subtle highlight.
 *
 * Width is fixed at 210px. The panel cannot be closed or resized.
 */
public class NavigationPanel extends JPanel {

    private static final int NAV_WIDTH = 210;

    private final NavigationController              controller;
    private final Map<Section, NavButton>           buttons = new EnumMap<>(Section.class);

    public NavigationPanel(NavigationController controller) {
        this.controller = controller;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(MainFrame.BG_DEEP);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, MainFrame.BORDER_SUBTLE));
        setPreferredSize(new Dimension(NAV_WIDTH, 0));
        setMinimumSize (new Dimension(NAV_WIDTH, 0));
        setMaximumSize (new Dimension(NAV_WIDTH, Integer.MAX_VALUE));

        // Logo / branding strip
        add(buildBrandStrip());

        // Divider
        add(buildDivider());

        // Nav section label
        JLabel navLabel = new JLabel("  NAVIGATION");
        navLabel.setFont(new Font("Courier New", Font.BOLD, 10));
        navLabel.setForeground(MainFrame.TEXT_SECONDARY);
        navLabel.setBorder(BorderFactory.createEmptyBorder(16, 12, 8, 0));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(navLabel);

        // Buttons
        for (Section s : Section.values()) {
            NavButton btn = new NavButton(s);
            buttons.put(s, btn);
            add(btn);
        }

        add(Box.createVerticalGlue());

        // Listen for section changes from the controller to update highlight
        controller.addSectionListener(this::setActiveSection);
    }

    // ── Brand strip ───────────────────────────────────────────────────────────

    private JPanel buildBrandStrip() {
        JPanel strip = new JPanel();
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setBackground(MainFrame.BG_DEEP);
        strip.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));
        strip.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel("♦");
        logo.setFont(new Font("Courier New", Font.BOLD, 28));
        logo.setForeground(MainFrame.ACCENT_GOLD);
        strip.add(logo);

        JLabel name = new JLabel("GestAcad");
        name.setFont(new Font("Courier New", Font.BOLD, 16));
        name.setForeground(MainFrame.TEXT_PRIMARY);
        strip.add(Box.createVerticalStrut(4));
        strip.add(name);

        JLabel ver = new JLabel("v1.0 — USTHB");
        ver.setFont(new Font("Courier New", Font.PLAIN, 10));
        ver.setForeground(MainFrame.TEXT_SECONDARY);
        strip.add(ver);

        return strip;
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(MainFrame.BORDER_SUBTLE);
        sep.setBackground(MainFrame.BG_DEEP);
        sep.setMaximumSize(new Dimension(NAV_WIDTH, 1));
        return sep;
    }

    // ── Active state ──────────────────────────────────────────────────────────

    private void setActiveSection(Section active) {
        buttons.forEach((s, btn) -> btn.setActive(s == active));
        revalidate();
        repaint();
    }

    // ── Inner NavButton ───────────────────────────────────────────────────────

    private class NavButton extends JPanel {
        private final Section section;
        private boolean active  = false;
        private boolean hovered = false;

        NavButton(Section section) {
            this.section = section;
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(MainFrame.BG_DEEP);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(NAV_WIDTH, 48));
            setPreferredSize(new Dimension(NAV_WIDTH, 48));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            // Left accent bar (visible only when active)
            JPanel accent = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (active) {
                        g.setColor(MainFrame.ACCENT_GOLD);
                        g.fillRect(0, 6, 3, getHeight() - 12);
                    }
                }
            };
            accent.setOpaque(false);
            accent.setPreferredSize(new Dimension(6, 48));
            add(accent, BorderLayout.WEST);

            // Label
            JLabel lbl = new JLabel(" " + section.getLabel());
            lbl.setFont(MainFrame.FONT_LABEL);
            lbl.setForeground(MainFrame.TEXT_PRIMARY);
            add(lbl, BorderLayout.CENTER);

            // Mouse interactions
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e)  { hovered = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  {
                    controller.navigateTo(section);
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (active) {
                g2.setColor(MainFrame.NAV_SELECT);
            } else if (hovered) {
                g2.setColor(new Color(0x1A, 0x2E, 0x4A));
            } else {
                g2.setColor(MainFrame.BG_DEEP);
            }
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
