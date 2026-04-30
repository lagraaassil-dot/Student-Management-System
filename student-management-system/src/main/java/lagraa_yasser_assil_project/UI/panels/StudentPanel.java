package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.dao.NoteDAO;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController;
import lagraa_yasser_assil_project.UI.utils.SearchableDropdown;
import lagraa_yasser_assil_project.UI.utils.UIValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * StudentPanel — manages Level 1 (action menu) and Level 2 (forms / lists)
 * for the Students section.
 *
 * Actions: Add · Remove · Modify · Show list
 *
 * Context-saving contract:
 *   • markDirty()  is called as soon as the user picks an action (Level 2 is shown).
 *   • reset()      returns the panel to Level 1 and clears the dirty flag.
 *   • The NavigationController calls reset() on this panel only when the user
 *     CONFIRMS or STARTS an action in a different section.
 */
public class StudentPanel extends JPanel {

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final NoteDAO        noteDAO        = new NoteDAO();

    // ── Layout ─────────────────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private static final String CARD_LEVEL1   = "LEVEL1";
    private static final String CARD_ADD      = "ADD";
    private static final String CARD_REMOVE   = "REMOVE";
    private static final String CARD_MODIFY   = "MODIFY";
    private static final String CARD_LIST     = "LIST";

    // ── Controller reference (set after construction) ─────────────────────
    private NavigationController controller;

    // ── Currently selected student (for modify / remove) ─────────────────
    private Etudiant selectedStudent;

    // ── "Modify" form fields (kept as fields so reset() can clear them) ───
    private JTextField modNomField, modPrenomField, modEmailField;

    public StudentPanel() {
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);

        add(buildLevel1Panel(),  CARD_LEVEL1);
        add(buildAddPanel(),     CARD_ADD);
        add(buildRemovePanel(),  CARD_REMOVE);
        add(buildModifyPanel(),  CARD_MODIFY);
        add(buildListPanel(),    CARD_LIST);

        cardLayout.show(this, CARD_LEVEL1);
    }

    // ── Controller injection ──────────────────────────────────────────────

    public void setController(NavigationController controller) {
        this.controller = controller;
    }

    // ── Context-saving reset ──────────────────────────────────────────────

    /** Returns this panel to Level-1 and clears any in-progress data. */
    public void reset() {
        selectedStudent = null;
        cardLayout.show(this, CARD_LEVEL1);
        if (controller != null)
            controller.clearDirty(NavigationController.Section.STUDENTS);
    }

    // =========================================================================
    // LEVEL 1 — Action selector
    // =========================================================================

    private JPanel buildLevel1Panel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.BG_PANEL);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(MainFrame.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(32, 48, 32, 48)
        ));

        JLabel title = new JLabel("👤  Gestion des Étudiants");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("Sélectionnez une action");
        sub.setFont(MainFrame.FONT_LABEL);
        sub.setForeground(MainFrame.TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(32));

        String[][] actions = {
            {"➕  Ajouter un étudiant",     CARD_ADD},
            {"🗑  Supprimer un étudiant",   CARD_REMOVE},
            {"✏️  Modifier un étudiant",    CARD_MODIFY},
            {"📋  Afficher la liste",       CARD_LIST},
        };

        for (String[] a : actions) {
            JButton btn = makeActionButton(a[0]);
            btn.addActionListener(e -> {
                markDirty();
                if (CARD_LIST.equals(a[1])) refreshListPanel();
                cardLayout.show(this, a[1]);
            });
            card.add(btn);
            card.add(Box.createVerticalStrut(12));
        }

        p.add(card);
        return p;
    }

    // =========================================================================
    // LEVEL 2 — Add student
    // =========================================================================

    private JPanel buildAddPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(MainFrame.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = sectionTitle("➕  Ajouter un Étudiant");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        // Fields
        JTextField nomField    = styledField();
        JTextField prenomField = styledField();
        JTextField emailField  = styledField();
        JTextField dateField   = styledField();
        dateField.putClientProperty("JTextField.placeholderText", "jj/MM/aaaa");

        String[][] rows = {
            {"Nom *", null},
            {"Prénom *", null},
            {"Email *", null},
            {"Date de naissance *", null},
        };
        JTextField[] fields = {nomField, prenomField, emailField, dateField};

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0;
            form.add(fieldLabel(rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            form.add(fields[i], gbc);
        }

        // Error label
        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(MainFrame.DANGER_RED);
        errorLbl.setFont(MainFrame.FONT_LABEL);
        gbc.gridx = 0; gbc.gridy = rows.length + 1; gbc.gridwidth = 2;
        form.add(errorLbl, gbc);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);
        JButton cancelBtn  = makeSecondaryButton("Annuler");
        JButton confirmBtn = makePrimaryButton("Confirmer");

        cancelBtn.addActionListener(e -> reset());

        confirmBtn.addActionListener(e -> {
            errorLbl.setText(" ");
            String nom    = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email  = emailField.getText().trim();
            String dateStr = dateField.getText().trim();

            if (!UIValidator.notBlank(nom))    { errorLbl.setText("Le nom est requis."); return; }
            if (!UIValidator.notBlank(prenom)) { errorLbl.setText("Le prénom est requis."); return; }
            if (!UIValidator.isValidEmail(email)) { errorLbl.setText("Email invalide (gmail/hotmail/usthb/yahoo)."); return; }

            Date dob;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                dob = sdf.parse(dateStr);
            } catch (ParseException ex) {
                errorLbl.setText("Date invalide. Format attendu: jj/MM/aaaa"); return;
            }

            Etudiant e2 = new Etudiant(nom, prenom, dob, email);
            boolean ok = etudiantDAO.addEtudiant(e2);
            if (ok) {
                nomField.setText(""); prenomField.setText("");
                emailField.setText(""); dateField.setText("");
                showSuccess("Étudiant ajouté avec succès !");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.STUDENTS);
                reset();
            } else {
                errorLbl.setText("Erreur lors de l'ajout. Vérifiez les données.");
            }
        });

        btns.add(cancelBtn); btns.add(confirmBtn);
        gbc.gridx = 0; gbc.gridy = rows.length + 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 0, 0);
        form.add(btns, gbc);

        wrapper.add(form);
        return wrapper;
    }

    // =========================================================================
    // LEVEL 2 — Remove student
    // =========================================================================

    private SearchableDropdown<Etudiant> removeDropdown;

    private JPanel buildRemovePanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(MainFrame.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        card.setPreferredSize(new Dimension(520, 420));

        card.add(sectionTitle("🗑  Supprimer un Étudiant"), BorderLayout.NORTH);

        removeDropdown = new SearchableDropdown<>(
            List.of(),
            e -> e.getNom() + " " + e.getPrenom() + "  [ID:" + e.getIdEtudiant() + "]",
            e -> e.getEmail()
        );

        JLabel infoLbl = new JLabel(" ");
        infoLbl.setForeground(MainFrame.TEXT_SECONDARY);
        infoLbl.setFont(MainFrame.FONT_LABEL);

        removeDropdown.setOnSelect(e -> {
            selectedStudent = e;
            infoLbl.setText("Sélectionné : " + e.getNom() + " " + e.getPrenom());
        });

        card.add(removeDropdown, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(infoLbl, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        btns.setOpaque(false);
        JButton cancelBtn  = makeSecondaryButton("Annuler");
        JButton confirmBtn = makeDangerButton("Supprimer");

        cancelBtn.addActionListener(e -> reset());
        confirmBtn.addActionListener(e -> {
            if (selectedStudent == null) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un étudiant.", "Aucune sélection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int res = JOptionPane.showConfirmDialog(this,
                "Supprimer " + selectedStudent.getNom() + " " + selectedStudent.getPrenom()
                + " ?\nToutes ses inscriptions et notes seront également supprimées.",
                "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION) return;

            boolean ok = etudiantDAO.deleteEtudiant(selectedStudent.getIdEtudiant());
            if (ok) {
                showSuccess("Étudiant supprimé.");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.STUDENTS);
                reset();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancelBtn); btns.add(confirmBtn);
        south.add(btns, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }

    /** Called when the Remove card becomes active — reloads the student list. */
    private void refreshRemoveDropdown() {
        if (removeDropdown != null)
            removeDropdown.setItems(etudiantDAO.getAllEtudiants());
    }

    // =========================================================================
    // LEVEL 2 — Modify student
    // =========================================================================

    private SearchableDropdown<Etudiant> modifyDropdown;
    private JPanel modifyFormCard;

    private JPanel buildModifyPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        // Two-phase card
        JPanel outerCard = new JPanel(new CardLayout());
        outerCard.setBackground(MainFrame.BG_CARD);
        outerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        outerCard.setPreferredSize(new Dimension(560, 480));

        // Phase A: select student
        JPanel selectCard = new JPanel(new BorderLayout(0, 16));
        selectCard.setOpaque(false);
        selectCard.add(sectionTitle("✏️  Modifier — Sélectionner l'étudiant"), BorderLayout.NORTH);

        modifyDropdown = new SearchableDropdown<>(
            List.of(),
            e -> e.getNom() + " " + e.getPrenom() + "  [ID:" + e.getIdEtudiant() + "]",
            e -> e.getEmail()
        );

        JButton nextBtn = makePrimaryButton("Modifier →");
        nextBtn.addActionListener(e -> {
            if (selectedStudent == null) {
                JOptionPane.showMessageDialog(this,
                    "Sélectionnez un étudiant.", "Aucune sélection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Pre-fill form
            modNomField.setText(selectedStudent.getNom());
            modPrenomField.setText(selectedStudent.getPrenom());
            modEmailField.setText(selectedStudent.getEmail());
            ((CardLayout)outerCard.getLayout()).show(outerCard, "FORM");
        });

        modifyDropdown.setOnSelect(e -> selectedStudent = e);

        JPanel selectSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        selectSouth.setOpaque(false);
        selectSouth.add(makeSecondaryButton("Annuler") {{
            addActionListener(ev -> reset());
        }});
        selectSouth.add(nextBtn);
        selectCard.add(modifyDropdown, BorderLayout.CENTER);
        selectCard.add(selectSouth, BorderLayout.SOUTH);

        // Phase B: edit form
        modifyFormCard = new JPanel(new GridBagLayout());
        modifyFormCard.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel formTitle = sectionTitle("✏️  Modifier l'Étudiant");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        modifyFormCard.add(formTitle, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        modNomField    = styledField();
        modPrenomField = styledField();
        modEmailField  = styledField();

        String[] labels = {"Nom *", "Prénom *", "Email *"};
        JTextField[] fields = {modNomField, modPrenomField, modEmailField};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0;
            modifyFormCard.add(fieldLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            modifyFormCard.add(fields[i], gbc);
        }

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(MainFrame.DANGER_RED);
        errorLbl.setFont(MainFrame.FONT_LABEL);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        modifyFormCard.add(errorLbl, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setOpaque(false);
        JButton backBtn    = makeSecondaryButton("← Retour");
        JButton saveBtn    = makePrimaryButton("Enregistrer");

        backBtn.addActionListener(e -> ((CardLayout)outerCard.getLayout()).show(outerCard, "SELECT"));
        saveBtn.addActionListener(e -> {
            errorLbl.setText(" ");
            String nom    = modNomField.getText().trim();
            String prenom = modPrenomField.getText().trim();
            String email  = modEmailField.getText().trim();

            if (!UIValidator.notBlank(nom))    { errorLbl.setText("Le nom est requis."); return; }
            if (!UIValidator.notBlank(prenom)) { errorLbl.setText("Le prénom est requis."); return; }
            if (!UIValidator.isValidEmail(email)) { errorLbl.setText("Email invalide."); return; }

            selectedStudent.setNom(nom);
            selectedStudent.setPrenom(prenom);
            selectedStudent.setEmail(email);

            boolean ok = etudiantDAO.updateEtudiant(selectedStudent);
            if (ok) {
                showSuccess("Étudiant modifié avec succès !");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.STUDENTS);
                reset();
            } else {
                errorLbl.setText("Erreur lors de la mise à jour.");
            }
        });

        btns.add(backBtn); btns.add(saveBtn);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(12, 0, 0, 0);
        modifyFormCard.add(btns, gbc);

        outerCard.add(selectCard, "SELECT");
        outerCard.add(modifyFormCard, "FORM");

        wrapper.add(outerCard);
        return wrapper;
    }

    private void refreshModifyDropdown() {
        if (modifyDropdown != null) {
            selectedStudent = null;
            modifyDropdown.setItems(etudiantDAO.getAllEtudiants());
        }
    }

    // =========================================================================
    // LEVEL 2 — Show list
    // =========================================================================

    private JTable listTable;
    private DefaultTableModel listTableModel;

    private JPanel buildListPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(MainFrame.BG_PANEL);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel title = sectionTitle("📋  Liste des Étudiants");
        wrapper.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Prénom", "Date Naissance", "Email", "Diplômé"};
        listTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        listTable = new JTable(listTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    boolean diplome = "Oui".equals(getValueAt(row, 5));
                    c.setBackground(diplome
                        ? new Color(0x1A, 0x3A, 0x2A)
                        : MainFrame.BG_CARD);
                }
                return c;
            }
        };
        styleTable(listTable);

        JScrollPane scroll = new JScrollPane(listTable);
        scroll.setBackground(MainFrame.BG_CARD);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE));
        wrapper.add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        JButton backBtn = makeSecondaryButton("← Retour");
        backBtn.addActionListener(e -> reset());
        south.add(backBtn);
        wrapper.add(south, BorderLayout.SOUTH);

        return wrapper;
    }

    private void refreshListPanel() {
        if (listTableModel == null) return;
        listTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Etudiant e : etudiantDAO.getAllEtudiants()) {
            listTableModel.addRow(new Object[]{
                e.getIdEtudiant(),
                e.getNom(),
                e.getPrenom(),
                e.getDateNaissance() != null ? sdf.format(e.getDateNaissance()) : "",
                e.getEmail(),
                e.isDiplome() ? "Oui" : "Non"
            });
        }
    }

    // =========================================================================
    // Overridden to intercept card switches for lazy data load
    // =========================================================================

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    /** Called by ContentPanel when this panel becomes the active card. */
    public void onActivate() {
        // Nothing by default; Level-1 is always shown first.
    }

    // ── Called before showing remove/modify to pre-load data ──────────────

    private void markDirty() {
        if (controller != null)
            controller.markDirty(NavigationController.Section.STUDENTS);
    }

    // =========================================================================
    // Helpers — UI factory methods
    // =========================================================================

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainFrame.FONT_DISPLAY);
        lbl.setForeground(MainFrame.ACCENT_GOLD);
        return lbl;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainFrame.FONT_LABEL);
        lbl.setForeground(MainFrame.TEXT_SECONDARY);
        return lbl;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(20);
        f.setBackground(MainFrame.BG_CARD);
        f.setForeground(MainFrame.TEXT_PRIMARY);
        f.setCaretColor(MainFrame.ACCENT_GOLD);
        f.setFont(MainFrame.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    private JButton makeActionButton(String text) {
        JButton b = new JButton(text);
        b.setFont(MainFrame.FONT_LABEL);
        b.setBackground(MainFrame.BG_PANEL);
        b.setForeground(MainFrame.TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(280, 42));
        b.setPreferredSize(new Dimension(280, 42));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(MainFrame.NAV_SELECT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(MainFrame.BG_PANEL); }
        });
        return b;
    }

    private JButton makePrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(MainFrame.FONT_LABEL);
        b.setBackground(MainFrame.ACCENT_BLUE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.ACCENT_BLUE),
            BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        return b;
    }

    private JButton makeSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(MainFrame.FONT_LABEL);
        b.setBackground(MainFrame.BG_PANEL);
        b.setForeground(MainFrame.TEXT_SECONDARY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        return b;
    }

    private JButton makeDangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(MainFrame.FONT_LABEL);
        b.setBackground(MainFrame.DANGER_RED);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.DANGER_RED),
            BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        return b;
    }

    private void styleTable(JTable table) {
        table.setFont(MainFrame.FONT_BODY);
        table.setRowHeight(28);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setGridColor(MainFrame.BORDER_SUBTLE);
        table.setSelectionBackground(MainFrame.NAV_SELECT);
        table.setSelectionForeground(MainFrame.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(MainFrame.BG_PANEL);
        table.getTableHeader().setForeground(MainFrame.ACCENT_GOLD);
        table.getTableHeader().setFont(MainFrame.FONT_LABEL);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}
