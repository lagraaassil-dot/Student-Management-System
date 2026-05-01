package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.Enums.Departement;
import lagraa_yasser_assil_project.Enums.Specialite;
import lagraa_yasser_assil_project.dao.EnseignantDAO;
import lagraa_yasser_assil_project.dao.ModuleDAO;
import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController;
import lagraa_yasser_assil_project.UI.utils.SearchableDropdown;
import lagraa_yasser_assil_project.UI.utils.UIValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// Panel for managing teachers.
public class TeacherPanel extends JPanel {

    private final EnseignantDAO enseignantDAO = new EnseignantDAO();
    private final ModuleDAO     moduleDAO     = new ModuleDAO();

    private final CardLayout cardLayout = new CardLayout();

    private static final String CARD_LEVEL1  = "LEVEL1";
    private static final String CARD_ADD     = "ADD";
    private static final String CARD_REMOVE  = "REMOVE";
    private static final String CARD_MODIFY  = "MODIFY";
    private static final String CARD_ASSIGN  = "ASSIGN";
    private static final String CARD_LIST    = "LIST";

    private NavigationController controller;
    private Enseignant selectedTeacher;

    public TeacherPanel() {
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);

        add(buildLevel1Panel(), CARD_LEVEL1);
        add(buildAddPanel(),    CARD_ADD);
        add(buildRemovePanel(), CARD_REMOVE);
        add(buildModifyPanel(), CARD_MODIFY);
        add(buildAssignPanel(), CARD_ASSIGN);
        add(buildListPanel(),   CARD_LIST);

        cardLayout.show(this, CARD_LEVEL1);
    }

    public void setController(NavigationController c) { this.controller = c; }

    public void reset() {
        selectedTeacher = null;
        cardLayout.show(this, CARD_LEVEL1);
        if (controller != null)
            controller.clearDirty(NavigationController.Section.TEACHERS);
    }

    private void markDirty() {
        if (controller != null)
            controller.markDirty(NavigationController.Section.TEACHERS);
    }

    
    
    

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

        JLabel title = sectionTitle("[>] Gestion des Enseignants");
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        JLabel sub = smallLabel("Sélectionnez une action");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(32));

        Object[][] actions = {
            {"[+] Ajouter un enseignant",   CARD_ADD,    false},
            {"[-] Supprimer un enseignant",  CARD_REMOVE, false},
            {"[*] Modifier un enseignant",   CARD_MODIFY, false},
            {"[~] Assigner des modules",     CARD_ASSIGN, false},
            {"[=] Afficher la liste",        CARD_LIST,   true},
        };

        for (Object[] a : actions) {
            JButton btn = makeActionButton((String) a[0]);
            String card2 = (String) a[1];
            boolean isList = (boolean) a[2];
            btn.addActionListener(e -> {
                markDirty();
                if (isList) refreshListPanel();
                else        refreshDropdowns(card2);
                cardLayout.show(this, card2);
            });
            card.add(btn);
            card.add(Box.createVerticalStrut(12));
        }

        p.add(card);
        return p;
    }

    
    
    

    private JPanel buildAddPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(MainFrame.BG_CARD);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        outer.setPreferredSize(new Dimension(600, 520));

        
        JPanel phase1 = new JPanel(new GridBagLayout());
        phase1.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title1 = sectionTitle("[+] Ajouter — Nom & Departement");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        phase1.add(title1, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        JTextField nomField = styledField();
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        phase1.add(fieldLabel("Nom *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        phase1.add(nomField, gbc);

        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(16, 8, 4, 8);
        phase1.add(fieldLabel("Département *"), gbc);
        gbc.insets = new Insets(4, 8, 4, 8);

        ButtonGroup deptGroup = new ButtonGroup();
        Map<JCheckBox, Departement> deptCheckboxes = new LinkedHashMap<>();
        JPanel deptPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        deptPanel.setOpaque(false);

        for (Departement d : Departement.values()) {
            JCheckBox cb = new JCheckBox(d.toString());
            cb.setFont(MainFrame.FONT_BODY);
            cb.setForeground(MainFrame.TEXT_PRIMARY);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            deptGroup.add(cb);
            deptCheckboxes.put(cb, d);
            deptPanel.add(cb);
        }
        gbc.gridy = 3;
        phase1.add(deptPanel, gbc);

        JLabel errLbl1 = new JLabel(" ");
        errLbl1.setForeground(MainFrame.DANGER_RED);
        errLbl1.setFont(MainFrame.FONT_LABEL);
        gbc.gridy = 4; gbc.insets = new Insets(8, 8, 0, 8);
        phase1.add(errLbl1, gbc);

        JPanel btns1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns1.setOpaque(false);
        JButton cancel1 = makeSecondaryButton("Annuler");
        JButton next1   = makePrimaryButton("Suivant →");
        cancel1.addActionListener(e -> reset());

        
        @SuppressWarnings("unchecked")
        SearchableDropdown<Specialite>[] specDropRef = new SearchableDropdown[1];
        specDropRef[0] = new SearchableDropdown<>(
            List.of(),
            Specialite::toString,
            s -> s.name()
        );

        final Departement[] chosenDept = {null};

        next1.addActionListener(e -> {
            errLbl1.setText(" ");
            if (!UIValidator.notBlank(nomField.getText())) {
                errLbl1.setText("Le nom est requis."); return;
            }
            chosenDept[0] = deptCheckboxes.entrySet().stream()
                .filter(en -> en.getKey().isSelected())
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
            if (chosenDept[0] == null) {
                errLbl1.setText("Sélectionnez un département."); return;
            }
            
            outer.putClientProperty("nom", nomField.getText().trim());
            outer.putClientProperty("dept", chosenDept[0]);
            List<Specialite> specs = Arrays.stream(Specialite.values())
                .filter(s -> s.getDepartement() == chosenDept[0])
                .collect(Collectors.toList());
            specDropRef[0].setItems(specs);
            specDropRef[0].clearSelection();
            ((CardLayout)outer.getLayout()).show(outer, "PHASE2");
        });
        btns1.add(cancel1); btns1.add(next1);
        gbc.gridy = 5; gbc.insets = new Insets(12, 0, 0, 0);
        phase1.add(btns1, gbc);

        
        JPanel phase2 = new JPanel(new BorderLayout(0, 16));
        phase2.setOpaque(false);
        phase2.add(sectionTitle("[+] Ajouter — Specialite"), BorderLayout.NORTH);

        phase2.add(specDropRef[0], BorderLayout.CENTER);

        JLabel errLbl2 = new JLabel(" ");
        errLbl2.setForeground(MainFrame.DANGER_RED);
        errLbl2.setFont(MainFrame.FONT_LABEL);

        JPanel south2 = new JPanel(new BorderLayout());
        south2.setOpaque(false);
        south2.add(errLbl2, BorderLayout.NORTH);

        JPanel btns2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns2.setOpaque(false);
        JButton back2   = makeSecondaryButton("← Retour");
        JButton confirm = makePrimaryButton("Confirmer");
        back2.addActionListener(e -> ((CardLayout)outer.getLayout()).show(outer, "PHASE1"));
        confirm.addActionListener(e -> {
            errLbl2.setText(" ");
            Specialite spec = specDropRef[0].getSelectedItem();
            if (spec == null) { errLbl2.setText("Sélectionnez une spécialité."); return; }

            String nom = (String) outer.getClientProperty("nom");
            Enseignant t = new Enseignant(nom, spec);
            boolean ok = enseignantDAO.addEnseignant(t);
            if (ok) {
                showSuccess("Enseignant ajouté avec succès !");
                nomField.setText("");
                deptGroup.clearSelection();
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.TEACHERS);
                reset();
            } else {
                errLbl2.setText("Erreur lors de l'ajout.");
            }
        });
        btns2.add(back2); btns2.add(confirm);
        south2.add(btns2, BorderLayout.SOUTH);
        phase2.add(south2, BorderLayout.SOUTH);

        

        outer.add(phase1, "PHASE1");
        outer.add(phase2, "PHASE2");

        wrapper.add(outer);
        return wrapper;
    }

    
    
    

    private SearchableDropdown<Enseignant> removeDropdown;

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
        card.add(sectionTitle("[-] Supprimer un Enseignant"), BorderLayout.NORTH);

        removeDropdown = new SearchableDropdown<>(
            List.of(),
            e -> e.getNom() + "  [" + e.getSpecialite() + "]",
            e -> String.valueOf(e.getIdEnseignant())
        );
        removeDropdown.setOnSelect(e -> selectedTeacher = e);
        card.add(removeDropdown, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        JLabel infoLbl = new JLabel("Les modules assignés deviendront sans enseignant.");
        infoLbl.setFont(MainFrame.FONT_LABEL);
        infoLbl.setForeground(MainFrame.TEXT_SECONDARY);
        south.add(infoLbl, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        btns.setOpaque(false);
        JButton cancelBtn  = makeSecondaryButton("Annuler");
        JButton confirmBtn = makeDangerButton("Supprimer");
        cancelBtn.addActionListener(e -> reset());
        confirmBtn.addActionListener(e -> {
            if (selectedTeacher == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un enseignant.",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int res = JOptionPane.showConfirmDialog(this,
                "Supprimer " + selectedTeacher.getNom() + " ?\n"
                + "Ses modules seront désassignés (idEnseignant = NULL).",
                "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res != JOptionPane.YES_OPTION) return;

            
            for (ModuleEtude m : moduleDAO.getModulesByTeacher(selectedTeacher.getIdEnseignant())) {
                m.setEnseignant(null);
                moduleDAO.updateModule(m);
            }

            boolean ok = enseignantDAO.deleteEnseignant(selectedTeacher.getIdEnseignant());
            if (ok) {
                showSuccess("Enseignant supprimé.");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.TEACHERS);
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

    
    
    

    private SearchableDropdown<Enseignant> modifyDropdown;

    private JPanel buildModifyPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(MainFrame.BG_CARD);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        outer.setPreferredSize(new Dimension(600, 520));

        
        JPanel selectPhase = new JPanel(new BorderLayout(0, 16));
        selectPhase.setOpaque(false);
        selectPhase.add(sectionTitle("[*] Modifier — Selectionner"), BorderLayout.NORTH);

        modifyDropdown = new SearchableDropdown<>(
            List.of(),
            e -> e.getNom() + "  [" + e.getSpecialite() + "]",
            e -> String.valueOf(e.getIdEnseignant())
        );
        modifyDropdown.setOnSelect(e -> selectedTeacher = e);
        selectPhase.add(modifyDropdown, BorderLayout.CENTER);

        JPanel selBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        selBtns.setOpaque(false);
        
        JTextField nomField = styledField();
        ButtonGroup deptGroup = new ButtonGroup();
        Map<JCheckBox, Departement> deptCBs = new LinkedHashMap<>();
        for (Departement d : Departement.values()) {
            JCheckBox cb = new JCheckBox(d.toString());
            cb.setFont(MainFrame.FONT_BODY);
            cb.setForeground(MainFrame.TEXT_PRIMARY);
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            deptGroup.add(cb);
            deptCBs.put(cb, d);
        }
        @SuppressWarnings("unchecked")
        SearchableDropdown<Specialite>[] editSpecRef = new SearchableDropdown[1];
        editSpecRef[0] = new SearchableDropdown<>(List.of(), Specialite::toString, s -> s.name());

        JButton cancelSel = makeSecondaryButton("Annuler");
        JButton nextSel   = makePrimaryButton("Modifier →");
        cancelSel.addActionListener(e -> reset());
        nextSel.addActionListener(e -> {
            if (selectedTeacher == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un enseignant.",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            nomField.setText(selectedTeacher.getNom());
            Departement curDept = selectedTeacher.getSpecialite().getDepartement();
            deptCBs.forEach((cb, d) -> cb.setSelected(d == curDept));
            List<Specialite> specs = Arrays.stream(Specialite.values())
                .filter(s -> s.getDepartement() == curDept)
                .collect(Collectors.toList());
            editSpecRef[0].setItems(specs);
            editSpecRef[0].setSelectedItem(selectedTeacher.getSpecialite());
            outer.putClientProperty("nom", selectedTeacher.getNom());
            ((CardLayout)outer.getLayout()).show(outer, "EDIT");
        });
        selBtns.add(cancelSel); selBtns.add(nextSel);
        selectPhase.add(selBtns, BorderLayout.SOUTH);

        
        JPanel editPhase = new JPanel(new GridBagLayout());
        editPhase.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 16, 0);
        editPhase.add(sectionTitle("[*] Modifier l'Enseignant"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        editPhase.add(fieldLabel("Nom *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        editPhase.add(nomField, gbc);

        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(12, 8, 4, 8);
        editPhase.add(fieldLabel("Département"), gbc);
        gbc.insets = new Insets(4, 8, 4, 8);

        
        JPanel deptPanel = new JPanel(new GridLayout(0, 2, 6, 4));
        deptPanel.setOpaque(false);
        for (Map.Entry<JCheckBox, Departement> ent : deptCBs.entrySet()) {
            deptPanel.add(ent.getKey());
        }
        gbc.gridy = 3;
        editPhase.add(deptPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(12, 8, 4, 8);
        editPhase.add(fieldLabel("Spécialité"), gbc);
        gbc.insets = new Insets(4, 8, 4, 8);

        
        gbc.gridy = 5;
        editPhase.add(editSpecRef[0], gbc);

        
        for (Map.Entry<JCheckBox, Departement> en : deptCBs.entrySet()) {
            en.getKey().addActionListener(e -> {
                List<Specialite> specs = Arrays.stream(Specialite.values())
                    .filter(s -> s.getDepartement() == en.getValue())
                    .collect(Collectors.toList());
                editSpecRef[0].setItems(specs);
            });
        }

        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(MainFrame.DANGER_RED);
        errLbl.setFont(MainFrame.FONT_LABEL);
        gbc.gridy = 6; gbc.insets = new Insets(8, 8, 0, 8);
        editPhase.add(errLbl, gbc);

        JPanel editBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        editBtns.setOpaque(false);
        JButton backBtn = makeSecondaryButton("← Retour");
        JButton saveBtn = makePrimaryButton("Enregistrer");

        

        backBtn.addActionListener(e -> ((CardLayout)outer.getLayout()).show(outer, "SELECT"));
        saveBtn.addActionListener(e -> {
            errLbl.setText(" ");
            String nom = nomField.getText().trim();
            if (!UIValidator.notBlank(nom)) { errLbl.setText("Le nom est requis."); return; }
            Specialite spec = editSpecRef[0].getSelectedItem();
            if (spec == null) { errLbl.setText("Sélectionnez une spécialité."); return; }

            selectedTeacher.setNom(nom);
            selectedTeacher.setSpecialite(spec);
            boolean ok = enseignantDAO.updateEnseignant(selectedTeacher);
            if (ok) {
                showSuccess("Enseignant modifié avec succès !");
                if (controller != null)
                    controller.resetAllDirtyExcept(NavigationController.Section.TEACHERS);
                reset();
            } else {
                errLbl.setText("Erreur lors de la mise à jour.");
            }
        });
        editBtns.add(backBtn); editBtns.add(saveBtn);
        gbc.gridy = 7; gbc.insets = new Insets(12, 0, 0, 0);
        editPhase.add(editBtns, gbc);

        outer.add(selectPhase, "SELECT");
        outer.add(editPhase, "EDIT");

        wrapper.add(outer);
        return wrapper;
    }

    
    
    

    private SearchableDropdown<Enseignant>     assignTeacherDropdown;
    private SearchableDropdown<ModuleEtude>    assignModuleDropdown;

    private JPanel buildAssignPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(MainFrame.BG_PANEL);

        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(MainFrame.BG_CARD);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        outer.setPreferredSize(new Dimension(580, 500));

        
        JPanel selPhase = new JPanel(new BorderLayout(0, 16));
        selPhase.setOpaque(false);
        selPhase.add(sectionTitle("[~] Assigner Modules — Enseignant"), BorderLayout.NORTH);

        assignTeacherDropdown = new SearchableDropdown<>(
            List.of(),
            e -> e.getNom() + "  [" + e.getSpecialite() + "]",
            e -> String.valueOf(e.getIdEnseignant())
        );
        assignTeacherDropdown.setOnSelect(e -> selectedTeacher = e);
        selPhase.add(assignTeacherDropdown, BorderLayout.CENTER);

        JPanel selBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        selBtns.setOpaque(false);
        JButton cancelA = makeSecondaryButton("Annuler");
        JButton nextA   = makePrimaryButton("Suivant →");
        cancelA.addActionListener(e -> reset());
        nextA.addActionListener(e -> {
            if (selectedTeacher == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un enseignant.",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            List<ModuleEtude> unassigned = moduleDAO.getAllModules().stream()
                .filter(m -> m.getEnseignant() == null)
                .collect(Collectors.toList());
            assignModuleDropdown.setItems(unassigned);
            assignModuleDropdown.clearSelection();
            ((CardLayout)outer.getLayout()).show(outer, "MODULES");
        });
        selBtns.add(cancelA); selBtns.add(nextA);
        selPhase.add(selBtns, BorderLayout.SOUTH);

        
        JPanel modPhase = new JPanel(new BorderLayout(0, 16));
        modPhase.setOpaque(false);
        modPhase.add(sectionTitle("[~] Modules sans enseignant"), BorderLayout.NORTH);

        assignModuleDropdown = new SearchableDropdown<>(
            List.of(),
            m -> m.getNomModule() + "  [coeff:" + m.getCoefficient() + "]",
            m -> String.valueOf(m.getIdModule())
        );
        assignModuleDropdown.setMultiSelect(true);
        modPhase.add(assignModuleDropdown, BorderLayout.CENTER);

        JLabel infoLbl = new JLabel("Cliquez sur plusieurs modules pour les sélectionner.");
        infoLbl.setFont(MainFrame.FONT_LABEL);
        infoLbl.setForeground(MainFrame.TEXT_SECONDARY);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(infoLbl, BorderLayout.NORTH);

        JPanel modBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        modBtns.setOpaque(false);
        JButton backM    = makeSecondaryButton("← Retour");
        JButton confirmM = makePrimaryButton("Assigner");

        backM.addActionListener(e -> ((CardLayout)outer.getLayout()).show(outer, "SELECT"));
        confirmM.addActionListener(e -> {
            if (selectedTeacher == null) {
                JOptionPane.showMessageDialog(this, "Aucun enseignant sélectionné.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<ModuleEtude> chosen = assignModuleDropdown.getSelectedItems();
            if (chosen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sélectionnez au moins un module.",
                    "Aucun module", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            List<String> alreadyAssigned = chosen.stream()
                .filter(m -> m.getEnseignant() != null)
                .map(m -> m.getNomModule() + " (déjà assigné à " + m.getEnseignant().getNom() + ")")
                .collect(java.util.stream.Collectors.toList());
            if (!alreadyAssigned.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Les modules suivants ont déjà un enseignant assigné :\n"
                    + String.join("\n", alreadyAssigned),
                    "Assignation impossible", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (ModuleEtude m : chosen) {
                m.setEnseignant(selectedTeacher);
                moduleDAO.updateModule(m);
            }
            showSuccess(chosen.size() + " module(s) assigné(s) à " + selectedTeacher.getNom() + ".");
            if (controller != null)
                controller.resetAllDirtyExcept(NavigationController.Section.TEACHERS);
            reset();
        });
        modBtns.add(backM); modBtns.add(confirmM);
        south.add(modBtns, BorderLayout.SOUTH);
        modPhase.add(south, BorderLayout.SOUTH);

        outer.add(selPhase, "SELECT");
        outer.add(modPhase, "MODULES");

        wrapper.add(outer);
        return wrapper;
    }

    
    
    

    private DefaultTableModel listTableModel;

    private JPanel buildListPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(MainFrame.BG_PANEL);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        wrapper.add(sectionTitle("[=] Liste des Enseignants"), BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Spécialité", "Département"};
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
        JButton back = makeSecondaryButton("← Retour");
        back.addActionListener(e -> reset());
        south.add(back);
        wrapper.add(south, BorderLayout.SOUTH);

        return wrapper;
    }

    private void refreshListPanel() {
        if (listTableModel == null) return;
        listTableModel.setRowCount(0);
        for (Enseignant e : enseignantDAO.getAllEnseignants()) {
            listTableModel.addRow(new Object[]{
                e.getIdEnseignant(),
                e.getNom(),
                e.getSpecialite().toString(),
                e.getSpecialite().getDepartement().toString()
            });
        }
    }

    

    private void refreshDropdowns(String card) {
        List<Enseignant> all = enseignantDAO.getAllEnseignants();
        selectedTeacher = null;
        switch (card) {
            case CARD_REMOVE: if (removeDropdown  != null) removeDropdown.setItems(all); break;
            case CARD_MODIFY: if (modifyDropdown  != null) { modifyDropdown.setItems(all); modifyDropdown.clearSelection(); } break;
            case CARD_ASSIGN: if (assignTeacherDropdown != null) assignTeacherDropdown.setItems(all); break;
        }
    }

    
    
    

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(MainFrame.FONT_DISPLAY);
        l.setForeground(MainFrame.ACCENT_GOLD);
        return l;
    }

    private JLabel smallLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(MainFrame.FONT_LABEL);
        l.setForeground(MainFrame.TEXT_SECONDARY);
        return l;
    }

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
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
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(300, 42));
        b.setPreferredSize(new Dimension(300, 42));
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

    private JButton makePrimaryButton(String t) {
        JButton b = new JButton(t);
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

    private JButton makeSecondaryButton(String t) {
        JButton b = new JButton(t);
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

    private JButton makeDangerButton(String t) {
        JButton b = new JButton(t);
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

    private void styleTable(JTable t) {
        t.setFont(MainFrame.FONT_BODY);
        t.setRowHeight(28);
        t.setBackground(MainFrame.BG_CARD);
        t.setForeground(MainFrame.TEXT_PRIMARY);
        t.setGridColor(MainFrame.BORDER_SUBTLE);
        t.setSelectionBackground(MainFrame.NAV_SELECT);
        t.setSelectionForeground(MainFrame.TEXT_PRIMARY);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(MainFrame.BG_PANEL);
        t.getTableHeader().setForeground(MainFrame.ACCENT_GOLD);
        t.getTableHeader().setFont(MainFrame.FONT_LABEL);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}