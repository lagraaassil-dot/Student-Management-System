package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.dao.NoteDAO;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Note;
import lagraa_yasser_assil_project.UI.MainFrame;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultsPanel — read-only panel showing student averages.
 *
 * Columns: Student | Average | Session | Remark
 *
 * Color coding:
 *   Green row  → average ≥ 10 (Admis)
 *   Red row    → average < 10  (Ajourné)
 *
 * Remarks:
 *   "Admis (session normale)"    → passed, no type-2 grade in validated modules
 *   "Admis (session rattrapage)" → passed, at least one type-2 grade
 *   "Ajourné"                    → average < 10
 *
 * Only students who have at least one CC + Exam pair (type 0 + type 1)
 * appear in the table (incomplete data is excluded per spec).
 *
 * The table can be sorted ascending/descending by clicking the Average column header.
 *
 * refresh() is called automatically by ContentPanel whenever this card becomes visible.
 */
public class ResultsPanel extends JPanel {

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final NoteDAO        noteDAO        = new NoteDAO();

    // ── Table ─────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private boolean           sortAsc = true; // default ascending by average

    // ── Column indices ────────────────────────────────────────────────────────
    private static final int COL_STUDENT = 0;
    private static final int COL_AVERAGE = 1;
    private static final int COL_SESSION = 2;
    private static final int COL_REMARK  = 3;

    public ResultsPanel() {
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

        JLabel title = new JLabel("📈  Résultats");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = styledButton("↻  Actualiser");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        return header;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        String[] columns = { "Étudiant", "Moyenne", "Session", "Remarque" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == COL_AVERAGE) ? Double.class : String.class;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                // Determine row color from average value stored in model
                Double avg = (Double) tableModel.getValueAt(row, COL_AVERAGE);
                boolean passed = avg != null && avg >= 10.0;
                if (!isRowSelected(row)) {
                    c.setBackground(passed
                        ? new Color(0x1A, 0x3D, 0x27)   // dark green tint
                        : new Color(0x3D, 0x1A, 0x1A));  // dark red tint
                } else {
                    c.setBackground(MainFrame.NAV_SELECT);
                }
                c.setForeground(MainFrame.TEXT_PRIMARY);
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

        // Style the header
        JTableHeader th = table.getTableHeader();
        th.setBackground(MainFrame.BG_PANEL);
        th.setForeground(MainFrame.ACCENT_GOLD);
        th.setFont(MainFrame.FONT_TITLE);
        th.setPreferredSize(new Dimension(0, 36));

        // Column widths
        table.getColumnModel().getColumn(COL_STUDENT).setPreferredWidth(220);
        table.getColumnModel().getColumn(COL_AVERAGE).setPreferredWidth(100);
        table.getColumnModel().getColumn(COL_SESSION).setPreferredWidth(200);
        table.getColumnModel().getColumn(COL_REMARK ).setPreferredWidth(250);

        // Custom renderer for the Average column — center + format
        table.getColumnModel().getColumn(COL_AVERAGE).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object value, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, value, sel, foc, r, c);
                    if (value instanceof Double d) {
                        setText(String.format("%.2f / 20", d));
                    }
                    setHorizontalAlignment(CENTER);
                    setBackground(sel ? MainFrame.NAV_SELECT
                        : ((Double) tableModel.getValueAt(r, COL_AVERAGE) >= 10.0
                            ? new Color(0x1A, 0x3D, 0x27)
                            : new Color(0x3D, 0x1A, 0x1A)));
                    setForeground(value instanceof Double d && d >= 10.0
                        ? MainFrame.SUCCESS_GREEN
                        : MainFrame.DANGER_RED);
                    return this;
                }
            }
        );

        // Sort by Average column on header click
        th.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == COL_AVERAGE) {
                    sortAsc = !sortAsc;
                    sortByAverage();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(MainFrame.BG_PANEL);
        sp.getViewport().setBackground(MainFrame.BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        return sp;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 10));
        footer.setBackground(MainFrame.BG_PANEL);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER_SUBTLE));

        // Legend
        JLabel lgGreen = legendDot(MainFrame.SUCCESS_GREEN, "Admis (≥ 10)");
        JLabel lgRed   = legendDot(MainFrame.DANGER_RED,   "Ajourné (< 10)");
        JLabel lgSort  = new JLabel("  ↕ Cliquer sur « Moyenne » pour trier");
        lgSort.setFont(MainFrame.FONT_LABEL);
        lgSort.setForeground(MainFrame.TEXT_SECONDARY);

        footer.add(lgGreen);
        footer.add(lgRed);
        footer.add(lgSort);
        return footer;
    }

    private JLabel legendDot(Color color, String text) {
        JLabel lbl = new JLabel("● " + text) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        lbl.setFont(MainFrame.FONT_LABEL);
        lbl.setForeground(color);
        return lbl;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    /**
     * Reloads data from the database and repopulates the table.
     * Called automatically when this card becomes visible.
     */
    public void refresh() {
        tableModel.setRowCount(0);

        List<Etudiant> students = etudiantDAO.getAllEtudiants();

        for (Etudiant etudiant : students) {
            ResultRow row = computeResultRow(etudiant);
            if (row == null) continue; // insufficient data — skip
            tableModel.addRow(new Object[]{
                etudiant.getNom() + " " + etudiant.getPrenom(),
                row.average,
                row.session,
                row.remark
            });
        }

        // Default sort: descending average (best first)
        sortAsc = false;
        sortByAverage();
    }

    // ── Computation helpers ───────────────────────────────────────────────────

    /**
     * Builds a ResultRow for a student.
     *
     * Algorithm:
     *   1. Collect all enrollments for the student.
     *   2. For each enrollment that has both CC (type 0) and Exam (type 1):
     *        - bestExam = max(exam, rattrapage) if rattrapage exists, else exam
     *        - moduleAvg = (CC × 40 + bestExam × 60) / 100
     *   3. Global average = weighted sum(moduleAvg × coefficient) / sum(coefficient)
     *   4. Session = "session normale"  if global ≥ 10 and no type-2 note was used
     *              = "session rattrapage" if global ≥ 10 and at least one type-2 note contributed
     *              = "Ajourné"            if global < 10
     *
     * Returns null if no enrollment has both CC + Exam (cannot compute).
     */
    private ResultRow computeResultRow(Etudiant etudiant) {
        List<Inscription> inscriptions = inscriptionDAO.getInscriptionsByStudent(
                etudiant.getIdEtudiant());

        double weightedSum  = 0;
        int    totalCoeff   = 0;
        boolean hasAnyData  = false;
        boolean usedResit   = false;

        for (Inscription ins : inscriptions) {
            List<Note> notes = noteDAO.getNotesByInscription(ins.getIdInscription());

            double cc          = -1;
            double exam        = -1;
            double rattrapage  = -1;

            for (Note n : notes) {
                if (n.getTypeNote() == 0) cc         = n.getValeur();
                if (n.getTypeNote() == 1) exam       = n.getValeur();
                if (n.getTypeNote() == 2) rattrapage = n.getValeur();
            }

            if (cc < 0 || exam < 0) continue; // incomplete — skip this module

            hasAnyData = true;

            double bestExam = (rattrapage >= 0) ? Math.max(exam, rattrapage) : exam;
            if (rattrapage >= 0 && bestExam >= exam) {
                // rattrapage note was used and it was ≥ original exam
                // (even if it didn't improve, the student took the resit — counts)
                usedResit = true;
            }
            if (rattrapage >= 0) usedResit = true; // any resit taken = session rattrapage

            double moduleAvg = (cc * 40 + bestExam * 60) / 100.0;
            int coeff        = ins.getModule().getCoefficient();

            weightedSum += moduleAvg * coeff;
            totalCoeff  += coeff;
        }

        if (!hasAnyData || totalCoeff == 0) return null;

        double globalAvg = weightedSum / totalCoeff;

        String session;
        String remark;
        if (globalAvg >= 10.0) {
            session = usedResit ? "Session rattrapage" : "Session normale";
            remark  = usedResit
                ? "✅ Admis (session rattrapage)"
                : "✅ Admis (session normale)";
        } else {
            session = "—";
            remark  = "❌ Ajourné";
        }

        return new ResultRow(globalAvg, session, remark);
    }

    // ── Sorting ───────────────────────────────────────────────────────────────

    private void sortByAverage() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object[] row = new Object[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                row[j] = tableModel.getValueAt(i, j);
            }
            rows.add(row);
        }
        rows.sort((a, b) -> {
            double da = (double) a[COL_AVERAGE];
            double db = (double) b[COL_AVERAGE];
            return sortAsc ? Double.compare(da, db) : Double.compare(db, da);
        });
        tableModel.setRowCount(0);
        for (Object[] row : rows) tableModel.addRow(row);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

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

    // ── Inner data record ──────────────────────────────────────────────────────

    private static class ResultRow {
        final double average;
        final String session;
        final String remark;

        ResultRow(double average, String session, String remark) {
            this.average = average;
            this.session = session;
            this.remark  = remark;
        }
    }
}
