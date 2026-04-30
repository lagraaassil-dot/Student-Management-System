package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.UI.MainFrame;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * DiplomaPanel — read-only panel listing all graduated students (isDiplome = true).
 *
 * Columns: Student | Email | Modules Validated
 *
 * Always refreshed when the card becomes visible (called by ContentPanel.showCard()).
 * Also refreshed implicitly by any grade or enrollment modification through
 * EtudiantDAO.checkAndSetDiploma().
 *
 * A student is considered graduated when ALL their enrollments have isValidated = true
 * and they have at least one enrollment (enforced at DAO level).
 */
public class DiplomaPanel extends JPanel {

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    // ── Table ─────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;

    // ── Summary counter ───────────────────────────────────────────────────────
    private JLabel countLabel;

    // ── Column indices ────────────────────────────────────────────────────────
    private static final int COL_STUDENT  = 0;
    private static final int COL_EMAIL    = 1;
    private static final int COL_MODULES  = 2;

    public DiplomaPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(MainFrame.BG_PANEL);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(20, 24, 16, 24)
        ));

        // Left: title block
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(MainFrame.BG_PANEL);

        JLabel title = new JLabel("🎓  Diplôme");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        titleBlock.add(title);

        JLabel subtitle = new JLabel("  Étudiants ayant validé tous leurs modules");
        subtitle.setFont(MainFrame.FONT_LABEL);
        subtitle.setForeground(MainFrame.TEXT_SECONDARY);
        titleBlock.add(subtitle);

        header.add(titleBlock, BorderLayout.WEST);

        // Right: refresh button
        JButton refreshBtn = styledButton("↻  Actualiser");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        return header;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        String[] columns = { "Étudiant", "Email", "Modules validés" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == COL_MODULES) ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    // All rows are graduates — use a warm gold-tinted dark row
                    c.setBackground(new Color(0x22, 0x1E, 0x10));
                    c.setForeground(MainFrame.TEXT_PRIMARY);
                } else {
                    c.setBackground(MainFrame.NAV_SELECT);
                    c.setForeground(MainFrame.TEXT_PRIMARY);
                }
                return c;
            }
        };

        table.setFont(MainFrame.FONT_BODY);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setGridColor(MainFrame.BORDER_SUBTLE);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setSelectionBackground(MainFrame.NAV_SELECT);
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Style header
        JTableHeader th = table.getTableHeader();
        th.setBackground(MainFrame.BG_PANEL);
        th.setForeground(MainFrame.ACCENT_GOLD);
        th.setFont(MainFrame.FONT_TITLE);
        th.setPreferredSize(new Dimension(0, 36));

        // Column widths
        table.getColumnModel().getColumn(COL_STUDENT).setPreferredWidth(250);
        table.getColumnModel().getColumn(COL_EMAIL  ).setPreferredWidth(280);
        table.getColumnModel().getColumn(COL_MODULES).setPreferredWidth(150);

        // Center-align the modules count column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, value, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                setBackground(sel ? MainFrame.NAV_SELECT : new Color(0x22, 0x1E, 0x10));
                setForeground(MainFrame.ACCENT_GOLD);
                if (value instanceof Integer i) {
                    setText(i + " module" + (i > 1 ? "s" : ""));
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(COL_MODULES).setCellRenderer(centerRenderer);

        // Gold accent on student name column
        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, value, sel, foc, r, c);
                setBackground(sel ? MainFrame.NAV_SELECT : new Color(0x22, 0x1E, 0x10));
                setForeground(sel ? MainFrame.TEXT_PRIMARY : MainFrame.ACCENT_GOLD);
                setFont(MainFrame.FONT_LABEL);
                if (value instanceof String s) {
                    setText("🎓  " + s);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(COL_STUDENT).setCellRenderer(nameRenderer);

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(MainFrame.BG_PANEL);
        sp.getViewport().setBackground(new Color(0x22, 0x1E, 0x10));
        sp.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        return sp;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 10));
        footer.setBackground(MainFrame.BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER_SUBTLE));

        countLabel = new JLabel("Aucun étudiant diplômé.");
        countLabel.setFont(MainFrame.FONT_LABEL);
        countLabel.setForeground(MainFrame.TEXT_SECONDARY);
        footer.add(countLabel);

        JLabel note = new JLabel("  ·  Ce tableau est mis à jour automatiquement après chaque modification de note.");
        note.setFont(MainFrame.FONT_LABEL);
        note.setForeground(new Color(0x55, 0x66, 0x77));
        footer.add(note);

        return footer;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    /**
     * Reloads the list of graduated students from the database.
     * Called automatically when this card becomes visible.
     */
    public void refresh() {
        tableModel.setRowCount(0);

        List<Etudiant> graduated = etudiantDAO.getGraduatedStudents();

        for (Etudiant etudiant : graduated) {
            int modulesValidated = countValidatedModules(etudiant);
            tableModel.addRow(new Object[]{
                etudiant.getNom() + " " + etudiant.getPrenom(),
                etudiant.getEmail(),
                modulesValidated
            });
        }

        int count = tableModel.getRowCount();
        if (count == 0) {
            countLabel.setText("Aucun étudiant diplômé pour le moment.");
            countLabel.setForeground(MainFrame.TEXT_SECONDARY);
        } else {
            countLabel.setText("Total : " + count + " étudiant" + (count > 1 ? "s" : "") + " diplômé" + (count > 1 ? "s" : "") + "  🎓");
            countLabel.setForeground(MainFrame.ACCENT_GOLD);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Counts the number of validated enrollments for a student.
     * A validated enrollment has isValidated = true.
     */
    private int countValidatedModules(Etudiant etudiant) {
        List<Inscription> inscriptions =
            inscriptionDAO.getInscriptionsByStudent(etudiant.getIdEtudiant());
        int count = 0;
        for (Inscription ins : inscriptions) {
            if (Boolean.TRUE.equals(ins.getIsValidated())) {
                count++;
            }
        }
        return count;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(MainFrame.FONT_LABEL);
        btn.setBackground(MainFrame.NAV_SELECT);
        btn.setForeground(MainFrame.ACCENT_GOLD);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        return btn;
    }
}
