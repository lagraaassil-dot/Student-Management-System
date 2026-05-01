package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EnseignantDAO;
import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.dao.ModuleDAO;
import lagraa_yasser_assil_project.dao.NoteDAO;
import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController;
import lagraa_yasser_assil_project.UI.utils.SearchableDropdown;
import lagraa_yasser_assil_project.UI.utils.UIValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * ModulePanel — CRUD for modules.
 *
 * Add: name + coeff + volumeHoraire, optional teacher assignment (Skip button).
 * Remove: SearchableDropdown; cascades inscriptions & notes; rechecks isDiplome.
 * Modify: pick module → edit fields + change/unassign teacher.
 * Show list: table of all modules.
 */
public class ModulePanel extends JPanel {

    private final ModuleDAO      moduleDAO      = new ModuleDAO();
    private final EnseignantDAO  enseignantDAO  = new EnseignantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final NoteDAO        noteDAO        = new NoteDAO();
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();

    private final CardLayout cardLayout = new CardLayout();
    private static final String CARD_LEVEL1  = "LEVEL1";
    private static final String CARD_ADD     = "ADD";
    private static final String CARD_REMOVE  = "REMOVE";
    private static final String CARD_MODIFY  = "MODIFY";
    private static final String CARD_LIST    = "LIST";

    private NavigationController controller;
    private ModuleEtude selectedModule;

    public ModulePanel() {
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);
        add(buildLevel1Panel(), CARD_LEVEL1);
        add(buildAddPanel(),    CARD_ADD);
        add(buildRemovePanel(), CARD_REMOVE);
        add(buildModifyPanel(), CARD_MODIFY);
        add(buildListPanel(),   CARD_LIST);
        cardLayout.show(this, CARD_LEVEL1);
    }

    public void setController(NavigationController c) { this.controller = c; }

    public void reset() {
        selectedModule = null;
        cardLayout.show(this, CARD_LEVEL1);
        if (controller != null)
            controller.clearDirty(NavigationController.Section.MODULES);
    }

    private void markDirty() {
        if (controller != null)
            controller.markDirty(NavigationController.Section.MODULES);
    }

    // =========================================================================
    // LEVEL 1
    // =========================================================================

    private JPanel buildLevel1Panel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.BG_PANEL);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(MainFrame.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(32, 48, 32, 48)));

        JLabel title = sectionTitle("[>] Gestion des Modules");
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        JLabel sub = smallLabel("Sélectionnez une action");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(32));

        String[][] actions = {
            {"[+] Ajouter un module",   CARD_ADD},
            {"[-] Supprimer un module", CARD_REMOVE},
            {"[*] Modifier un module",  CARD_MODIFY},
            {"[=] Afficher la liste",   CARD_LIST},
        };

        for (String[] a : actions) {
            JButton btn = makeActionButton(a[0]);
            String target = a[1];
            btn.addActionListener(e -> {
                markDirty();
                if (CARD_LIST.equals(target)) refreshListPanel();
                else if (CARD_REMOVE.equals(target)) refreshRemoveDropdown();
                else if (CARD_MODIFY.equals(target)) refreshModifyDropdown();
                cardLayout.show(this, target);
            });
            card.add(btn);
            card.add(Box.createVerticalStrut(12));
        }

        p.add(card);
        return p;
    }

    // =========================================================================
    // LEVEL 2 — Add module (2-phase: info → optional teacher)
    // =========================================================================

    private JPanel buildAddPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(MainFrame.BG_CARD);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)));
        outer.setPreferredSize(new Dimension(600, 500));

        // Phase 1: module fields
        JPanel phase1 = new JPanel(new GridBagLayout());
        phase1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel t1 = sectionTitle("[+] Ajouter un Module");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0,0,20,0);
        phase1.add(t1, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6,8,6,8);

        JTextField nomField   = styledField();
        JTextField coeffField = styledField();
        JTextField vhField    = styledField();

        Object[][] rows = {{"Nom *", nomField}, {"Coefficient *", coeffField}, {"Volume horaire (min) *", vhField}};
        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i+1; gbc.weightx = 0;
            phase1.add(fieldLabel((String)rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            phase1.add((JTextField)rows[i][1], gbc);
        }

        JLabel errLbl1 = new JLabel(" ");
        errLbl1.setForeground(MainFrame.DANGER_RED);
        errLbl1.setFont(MainFrame.FONT_LABEL);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        phase1.add(errLbl1, gbc);

        JPanel btns1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns1.setOpaque(false);
        JButton cancel1 = makeSecondaryButton("Annuler");
        JButton next1   = makePrimaryButton("Suivant : Assigner un prof →");
        JButton skip1   = makeSecondaryButton("Ignorer (pas de prof)");
        cancel1.addActionListener(e -> reset());

        // store validated fields in outer client props
        ActionListener validateAndProceed = e -> {
            errLbl1.setText(" ");
            String nom = nomField.getText().trim();
            if (!UIValidator.notBlank(nom)) { errLbl1.setText("Le nom est requis."); return; }
            if (!UIValidator.isPositiveInt(coeffField.getText())) { errLbl1.setText("Coefficient invalide (entier > 0)."); return; }
            if (!UIValidator.isValidVolumeHoraire(vhField.getText())) { errLbl1.setText("Volume horaire invalide (0-1440)."); return; }
            outer.putClientProperty("nom",   nom);
            outer.putClientProperty("coeff", Integer.parseInt(coeffField.getText().trim()));
            outer.putClientProperty("vh",    Integer.parseInt(vhField.getText().trim()));
        };

        next1.addActionListener(e -> {
            validateAndProceed.actionPerformed(e);
            if (outer.getClientProperty("nom") == null) return;
            // Refresh teacher dropdown for phase2
            ((CardLayout)outer.getLayout()).show(outer, "PHASE2");
        });

        skip1.addActionListener(e -> {
            validateAndProceed.actionPerformed(e);
            if (outer.getClientProperty("nom") == null) return;
            // Create module without teacher
            ModuleEtude m = new ModuleEtude(
                (String) outer.getClientProperty("nom"),
                (int) outer.getClientProperty("coeff"),
                (int) outer.getClientProperty("vh")
            );
            boolean ok = moduleDAO.addModule(m);
            if (ok) {
                showSuccess("Module ajouté sans enseignant.");
                nomField.setText(""); coeffField.setText(""); vhField.setText("");
                outer.putClientProperty("nom", null);
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.MODULES);
                reset();
            } else {
                errLbl1.setText("Erreur lors de l'ajout.");
            }
        });

        btns1.add(cancel1); btns1.add(skip1); btns1.add(next1);
        gbc.gridy = 5; gbc.insets = new Insets(12,0,0,0);
        phase1.add(btns1, gbc);

        // Phase 2: assign teacher
        JPanel phase2 = new JPanel(new BorderLayout(0, 12));
        phase2.setOpaque(false);
        phase2.add(sectionTitle("[+] Ajouter — Assigner un Enseignant"), BorderLayout.NORTH);

        SearchableDropdown<Enseignant>[] teachDropRef = new SearchableDropdown[1];
        teachDropRef[0] = new SearchableDropdown<>(
            List.of(),
            t -> t.getNom() + "  [" + t.getSpecialite() + "]",
            t -> String.valueOf(t.getIdEnseignant())
        );
        phase2.add(teachDropRef[0], BorderLayout.CENTER);

        JLabel errLbl2 = new JLabel(" ");
        errLbl2.setForeground(MainFrame.DANGER_RED);
        errLbl2.setFont(MainFrame.FONT_LABEL);
        JPanel south2 = new JPanel(new BorderLayout());
        south2.setOpaque(false);
        south2.add(errLbl2, BorderLayout.NORTH);

        JPanel btns2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns2.setOpaque(false);
        JButton back2    = makeSecondaryButton("← Retour");
        JButton confirm2 = makePrimaryButton("Confirmer");
        back2.addActionListener(e -> ((CardLayout)outer.getLayout()).show(outer, "PHASE1"));
        confirm2.addActionListener(e -> {
            errLbl2.setText(" ");
            Enseignant ens = teachDropRef[0].getSelectedItem();
            if (ens == null) { errLbl2.setText("Sélectionnez un enseignant."); return; }
            ModuleEtude m = new ModuleEtude(
                (String) outer.getClientProperty("nom"),
                (int)    outer.getClientProperty("coeff"),
                (int)    outer.getClientProperty("vh"),
                ens
            );
            boolean ok = moduleDAO.addModule(m);
            if (ok) {
                showSuccess("Module ajouté avec succès !");
                nomField.setText(""); coeffField.setText(""); vhField.setText("");
                outer.putClientProperty("nom", null);
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.MODULES);
                reset();
            } else {
                errLbl2.setText("Erreur lors de l'ajout.");
            }
        });
        btns2.add(back2); btns2.add(confirm2);
        south2.add(btns2, BorderLayout.SOUTH);
        phase2.add(south2, BorderLayout.SOUTH);

        // Wire next1 to populate phase2 teacher list
        next1.addActionListener(e -> {
            teachDropRef[0].setItems(enseignantDAO.getAllEnseignants());
        });

        outer.add(phase1, "PHASE1");
        outer.add(phase2, "PHASE2");

        wrapper.add(outer);
        return wrapper;
    }

    // =========================================================================
    // LEVEL 2 — Remove module
    // =========================================================================

    private SearchableDropdown<ModuleEtude> removeDropdown;

    private JPanel buildRemovePanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(MainFrame.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)));
        card.setPreferredSize(new Dimension(520, 420));

        card.add(sectionTitle("[-] Supprimer un Module"), BorderLayout.NORTH);

        removeDropdown = new SearchableDropdown<>(
            List.of(),
            m -> m.getNomModule() + "  [ID:" + m.getIdModule() + "]",
            m -> m.getEnseignant() != null ? m.getEnseignant().getNom() : "sans prof"
        );

        JLabel infoLbl = new JLabel(" ");
        infoLbl.setForeground(MainFrame.TEXT_SECONDARY);
        infoLbl.setFont(MainFrame.FONT_LABEL);

        removeDropdown.setOnSelect(m -> {
            selectedModule = m;
            String prof = m.getEnseignant() != null ? m.getEnseignant().getNom() : "aucun";
            infoLbl.setText("Sélectionné : " + m.getNomModule() + " — Prof : " + prof);
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
            if (selectedModule == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un module.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int res = JOptionPane.showConfirmDialog(this,
                "Supprimer \"" + selectedModule.getNomModule() + "\" ?\n"
                + "Toutes les inscriptions et notes liées seront supprimées.\n"
                + "L'état isDiplome des étudiants concernés sera recalculé.",
                "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION) return;

            // Cascade: get all inscriptions for this module, delete notes, update diploma
            int modId = selectedModule.getIdModule();
            // We rely on DB cascade for notes, but need to recheck diplomas
            // Get all students enrolled in this module first
            List<Etudiant> affectedStudents = getStudentsEnrolledInModule(modId);

            boolean ok = moduleDAO.deleteModule(modId);
            if (ok) {
                // Recheck diploma for all affected students
                for (Etudiant stu : affectedStudents) {
                    etudiantDAO.checkAndSetDiploma(stu);
                }
                showSuccess("Module supprimé.");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.MODULES);
                reset();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancelBtn); btns.add(confirmBtn);
        south.add(btns, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }

    private List<Etudiant> getStudentsEnrolledInModule(int moduleId) {
        // Re-use InscriptionDAO to get students enrolled in this module before deletion
        java.util.List<Etudiant> students = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT e.idEtudiant, e.nom, e.prenom, e.dateNaissance, e.email, e.isDiplome "
                   + "FROM ETUDIANT e JOIN INSCRIPTION i ON e.idEtudiant = i.idEtudiant "
                   + "WHERE i.idModule = ?";
        try (java.sql.Connection conn = lagraa_yasser_assil_project.utils.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Etudiant e = new Etudiant(rs.getString("nom"), rs.getString("prenom"),
                        rs.getDate("dateNaissance"), rs.getString("email"));
                    e.setId(rs.getInt("idEtudiant"));
                    students.add(e);
                }
            }
        } catch (java.sql.SQLException ex) { ex.printStackTrace(); }
        return students;
    }

    private void refreshRemoveDropdown() {
        if (removeDropdown != null) {
            selectedModule = null;
            removeDropdown.setItems(moduleDAO.getAllModules());
        }
    }

    // =========================================================================
    // LEVEL 2 — Modify module
    // =========================================================================

    private SearchableDropdown<ModuleEtude> modifyDropdown;
    private JTextField modNomField, modCoeffField, modVhField;
    private SearchableDropdown<Enseignant> modTeacherDropdown;

    private JPanel buildModifyPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(MainFrame.BG_CARD);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)));
        outer.setPreferredSize(new Dimension(600, 520));

        // Phase A: select module
        JPanel selectCard = new JPanel(new BorderLayout(0, 16));
        selectCard.setOpaque(false);
        selectCard.add(sectionTitle("[*] Modifier — Selectionner le Module"), BorderLayout.NORTH);

        modifyDropdown = new SearchableDropdown<>(
            List.of(),
            m -> m.getNomModule() + "  [ID:" + m.getIdModule() + "]",
            m -> m.getEnseignant() != null ? m.getEnseignant().getNom() : ""
        );
        modifyDropdown.setOnSelect(m -> selectedModule = m);

        JButton nextMod = makePrimaryButton("Modifier →");
        nextMod.addActionListener(e -> {
            if (selectedModule == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un module.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            modNomField.setText(selectedModule.getNomModule());
            modCoeffField.setText(String.valueOf(selectedModule.getCoefficient()));
            modVhField.setText(String.valueOf(selectedModule.getVolumeHoraire()));
            modTeacherDropdown.setItems(enseignantDAO.getAllEnseignants());
            if (selectedModule.getEnseignant() != null)
                 modTeacherDropdown.setSelectedItem(selectedModule.getEnseignant());
            ((CardLayout)outer.getLayout()).show(outer, "FORM");
        });

        JPanel selectSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        selectSouth.setOpaque(false);
        JButton cancelSel = makeSecondaryButton("Annuler");
        cancelSel.addActionListener(e -> reset());
        selectSouth.add(cancelSel); selectSouth.add(nextMod);
        selectCard.add(modifyDropdown, BorderLayout.CENTER);
        selectCard.add(selectSouth, BorderLayout.SOUTH);

        // Phase B: edit form
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,8,6,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; gbc.insets=new Insets(0,0,20,0);
        formCard.add(sectionTitle("[*] Modifier le Module"), gbc);
        gbc.gridwidth=1; gbc.insets=new Insets(6,8,6,8);

        modNomField   = styledField();
        modCoeffField = styledField();
        modVhField    = styledField();

        String[] lbls = {"Nom *", "Coefficient *", "Volume horaire (min) *"};
        JTextField[] flds = {modNomField, modCoeffField, modVhField};
        for (int i = 0; i < lbls.length; i++) {
            gbc.gridx=0; gbc.gridy=i+1; gbc.weightx=0;
            formCard.add(fieldLabel(lbls[i]), gbc);
            gbc.gridx=1; gbc.weightx=1;
            formCard.add(flds[i], gbc);
        }

        // Teacher assignment sub-section
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; gbc.insets=new Insets(14,8,4,8);
        formCard.add(fieldLabel("Enseignant (optionnel — laisser vide pour désassigner)"), gbc);
        gbc.insets=new Insets(6,8,6,8);

        modTeacherDropdown = new SearchableDropdown<>(
            List.of(),
            t -> t.getNom() + "  [" + t.getSpecialite() + "]",
            t -> String.valueOf(t.getIdEnseignant())
        );
        gbc.gridy=5; gbc.gridwidth=2;
        formCard.add(modTeacherDropdown, gbc);

        JButton unassignBtn = makeSecondaryButton("Désassigner l'enseignant");
        unassignBtn.addActionListener(e -> modTeacherDropdown.clearSelection());
        gbc.gridy=6; gbc.gridwidth=2; gbc.insets=new Insets(4,8,4,8);
        formCard.add(unassignBtn, gbc);

        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(MainFrame.DANGER_RED);
        errLbl.setFont(MainFrame.FONT_LABEL);
        gbc.gridy=7; gbc.insets=new Insets(8,8,0,8);
        formCard.add(errLbl, gbc);

        JPanel btnsF = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnsF.setOpaque(false);
        JButton backBtn = makeSecondaryButton("← Retour");
        JButton saveBtn = makePrimaryButton("Enregistrer");
        backBtn.addActionListener(e -> ((CardLayout)outer.getLayout()).show(outer, "SELECT"));
        saveBtn.addActionListener(e -> {
            errLbl.setText(" ");
            String nom = modNomField.getText().trim();
            if (!UIValidator.notBlank(nom)) { errLbl.setText("Le nom est requis."); return; }
            if (!UIValidator.isPositiveInt(modCoeffField.getText())) { errLbl.setText("Coefficient invalide."); return; }
            if (!UIValidator.isValidVolumeHoraire(modVhField.getText())) { errLbl.setText("Volume horaire invalide."); return; }

            selectedModule.setNomModule(nom);
            selectedModule.setCoefficient(Integer.parseInt(modCoeffField.getText().trim()));
            selectedModule.setVolumeHoraire(Integer.parseInt(modVhField.getText().trim()));
            selectedModule.setEnseignant(modTeacherDropdown.getSelectedItem()); // null if unassigned

            boolean ok = moduleDAO.updateModule(selectedModule);
            if (ok) {
                showSuccess("Module modifié avec succès !");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.MODULES);
                reset();
            } else {
                errLbl.setText("Erreur lors de la mise à jour.");
            }
        });
        btnsF.add(backBtn); btnsF.add(saveBtn);
        gbc.gridy=8; gbc.insets=new Insets(12,0,0,0);
        formCard.add(btnsF, gbc);

        outer.add(selectCard, "SELECT");
        outer.add(formCard,   "FORM");
        wrapper.add(outer);
        return wrapper;
    }

    private void refreshModifyDropdown() {
        if (modifyDropdown != null) {
            selectedModule = null;
            modifyDropdown.setItems(moduleDAO.getAllModules());
        }
    }

    // =========================================================================
    // LEVEL 2 — Show list
    // =========================================================================

    private DefaultTableModel listTableModel;

    private JPanel buildListPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0,0));
        wrapper.setBackground(MainFrame.BG_PANEL);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24,32,24,32));

        wrapper.add(sectionTitle("[=] Liste des Modules"), BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Coefficient", "Vol. Horaire (min)", "Enseignant"};
        listTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(listTableModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
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
        for (ModuleEtude m : moduleDAO.getAllModules()) {
            String ensName = m.getEnseignant() != null ? m.getEnseignant().getNom() : "—";
            listTableModel.addRow(new Object[]{
                m.getIdModule(), m.getNomModule(), m.getCoefficient(),
                m.getVolumeHoraire(), ensName
            });
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MainFrame.FONT_DISPLAY);
        l.setForeground(MainFrame.ACCENT_GOLD);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MainFrame.FONT_LABEL);
        l.setForeground(MainFrame.TEXT_SECONDARY);
        return l;
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MainFrame.FONT_LABEL);
        l.setForeground(MainFrame.TEXT_SECONDARY);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(20);
        f.setBackground(MainFrame.BG_CARD);
        f.setForeground(MainFrame.TEXT_PRIMARY);
        f.setCaretColor(MainFrame.ACCENT_GOLD);
        f.setFont(MainFrame.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
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
            BorderFactory.createEmptyBorder(8,20,8,20)));
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
            BorderFactory.createEmptyBorder(7,18,7,18)));
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
            BorderFactory.createEmptyBorder(7,18,7,18)));
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
            BorderFactory.createEmptyBorder(7,18,7,18)));
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