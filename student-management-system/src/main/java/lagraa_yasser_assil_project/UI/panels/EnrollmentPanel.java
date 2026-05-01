package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.dao.ModuleDAO;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController;
import lagraa_yasser_assil_project.UI.utils.SearchableDropdown;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

// Panel for adding and removing student-module enrollments.
public class EnrollmentPanel extends JPanel {

    
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final ModuleDAO      moduleDAO      = new ModuleDAO();

    
    private final CardLayout cardLayout = new CardLayout();
    private static final String CARD_LEVEL1 = "LEVEL1";
    private static final String CARD_ADD    = "ADD";
    private static final String CARD_REMOVE = "REMOVE";
    private static final String CARD_LIST   = "LIST";

    private NavigationController controller;

    
    private Etudiant   selectedStudent;
    private ModuleEtude selectedModule;
    private SearchableDropdown<Etudiant>    studentDropdown;
    private SearchableDropdown<ModuleEtude> moduleDropdown;
    private JLabel     selectedStudentLabel;
    private JLabel     selectedModuleLabel;
    private JButton    confirmAddBtn;

    
    private SearchableDropdown<Inscription> removeDropdown;
    private JLabel removeSelectedLabel;
    private JButton confirmRemoveBtn;
    private Inscription selectedEnrollment;

    
    private DefaultTableModel listTableModel;

    public EnrollmentPanel() {
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);
        add(buildLevel1Panel(), CARD_LEVEL1);
        add(buildAddPanel(),    CARD_ADD);
        add(buildRemovePanel(), CARD_REMOVE);
        add(buildListPanel(),   CARD_LIST);
        cardLayout.show(this, CARD_LEVEL1);
    }

    public void setController(NavigationController controller) {
        this.controller = controller;
    }

    

    public void reset() {
        selectedStudent  = null;
        selectedModule   = null;
        selectedEnrollment = null;
        if (selectedStudentLabel != null) selectedStudentLabel.setText("Aucun étudiant sélectionné");
        if (selectedModuleLabel  != null) selectedModuleLabel.setText("Aucun module sélectionné");
        if (confirmAddBtn        != null) confirmAddBtn.setEnabled(false);
        if (removeSelectedLabel  != null) removeSelectedLabel.setText("Aucune inscription sélectionnée");
        if (confirmRemoveBtn     != null) confirmRemoveBtn.setEnabled(false);
        cardLayout.show(this, CARD_LEVEL1);
        if (controller != null) controller.clearDirty(NavigationController.Section.ENROLLMENT);
    }

    

    private JPanel buildLevel1Panel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.BG_PANEL);

        JLabel title = new JLabel("[I] Inscriptions");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);

        JButton btnAdd    = makeActionButton("[+] Nouvelle inscription");
        JButton btnRemove = makeActionButton("[-] Supprimer une inscription");
        JButton btnList   = makeActionButton("[=] Afficher la liste");

        btnAdd.addActionListener(e -> showAdd());
        btnRemove.addActionListener(e -> showRemove());
        btnList.addActionListener(e -> showList());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(12, 0, 12, 0); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0; p.add(title, gbc);
        gbc.gridy = 1; p.add(btnAdd, gbc);
        gbc.gridy = 2; p.add(btnRemove, gbc);
        gbc.gridy = 3; p.add(btnList, gbc);

        return p;
    }

    private JButton makeActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(MainFrame.FONT_LABEL);
        btn.setBackground(MainFrame.BG_CARD);
        btn.setForeground(MainFrame.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 48));
        return btn;
    }

    

    private JPanel buildAddPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[+] Nouvelle inscription");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        
        JPanel cols = new JPanel(new GridLayout(1, 2, 32, 0));
        cols.setBackground(MainFrame.BG_PANEL);
        cols.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        
        JPanel studentCol = new JPanel(new BorderLayout(0, 8));
        studentCol.setBackground(MainFrame.BG_PANEL);
        JLabel sLabel = new JLabel("1. Choisir un étudiant");
        sLabel.setFont(MainFrame.FONT_TITLE);
        sLabel.setForeground(MainFrame.ACCENT_BLUE);
        selectedStudentLabel = new JLabel("Aucun étudiant sélectionné");
        selectedStudentLabel.setFont(MainFrame.FONT_LABEL);
        selectedStudentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        studentDropdown = new SearchableDropdown<>(
            etudiantDAO.getAllEtudiants(),
            e -> e.getNom() + " " + e.getPrenom() + "  (#" + e.getIdEtudiant() + ")",
            e -> e.getEmail());
        studentDropdown.setOnSelect(student -> {
            selectedStudent = student;
            selectedStudentLabel.setText("[OK] " + student.getNom() + " " + student.getPrenom());
            selectedStudentLabel.setForeground(MainFrame.SUCCESS_GREEN);
            refreshModuleDropdown();
            updateConfirmButton();
        });
        studentCol.add(sLabel, BorderLayout.NORTH);
        studentCol.add(selectedStudentLabel, BorderLayout.SOUTH);
        JPanel sdWrap = new JPanel(new BorderLayout());
        sdWrap.setBackground(MainFrame.BG_PANEL);
        sdWrap.add(studentDropdown, BorderLayout.NORTH);
        studentCol.add(sdWrap, BorderLayout.CENTER);

        
        JPanel moduleCol = new JPanel(new BorderLayout(0, 8));
        moduleCol.setBackground(MainFrame.BG_PANEL);
        JLabel mLabel = new JLabel("2. Choisir un module (avec enseignant)");
        mLabel.setFont(MainFrame.FONT_TITLE);
        mLabel.setForeground(MainFrame.ACCENT_BLUE);
        selectedModuleLabel = new JLabel("Aucun module sélectionné");
        selectedModuleLabel.setFont(MainFrame.FONT_LABEL);
        selectedModuleLabel.setForeground(MainFrame.TEXT_SECONDARY);
        moduleDropdown = new SearchableDropdown<>(
            getModulesWithTeacher(),
            m -> m.getNomModule() + "  (coeff. " + m.getCoefficient() + ")",
            m -> m.getEnseignant() != null ? m.getEnseignant().getNom() : "");
        moduleDropdown.setOnSelect(module -> {
            selectedModule = module;
            String teacher = module.getEnseignant() != null ? module.getEnseignant().getNom() : "—";
            selectedModuleLabel.setText("[OK] " + module.getNomModule() + " — " + teacher);
            selectedModuleLabel.setForeground(MainFrame.SUCCESS_GREEN);
            updateConfirmButton();
        });
        moduleCol.add(mLabel, BorderLayout.NORTH);
        moduleCol.add(selectedModuleLabel, BorderLayout.SOUTH);
        JPanel mdWrap = new JPanel(new BorderLayout());
        mdWrap.setBackground(MainFrame.BG_PANEL);
        mdWrap.add(moduleDropdown, BorderLayout.NORTH);
        moduleCol.add(mdWrap, BorderLayout.CENTER);

        cols.add(studentCol);
        cols.add(moduleCol);
        outer.add(cols, BorderLayout.CENTER);

        
        confirmAddBtn = new JButton("[OK] Confirmer l'inscription");
        confirmAddBtn.setFont(MainFrame.FONT_TITLE);
        confirmAddBtn.setBackground(new Color(0x1A, 0x5C, 0x3A));
        confirmAddBtn.setForeground(Color.WHITE);
        confirmAddBtn.setEnabled(false);
        confirmAddBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmAddBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        confirmAddBtn.addActionListener(e -> confirmAdd());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(MainFrame.BG_PANEL);
        south.add(confirmAddBtn);
        outer.add(south, BorderLayout.SOUTH);

        return outer;
    }

    private void showAdd() {
        if (controller != null) {
            controller.markDirty(NavigationController.Section.ENROLLMENT);
            controller.resetAllDirtyExcept(NavigationController.Section.ENROLLMENT);
        }
        studentDropdown.setItems(etudiantDAO.getAllEtudiants());
        studentDropdown.clearSelection();
        selectedStudent = null;
        selectedModule  = null;
        selectedStudentLabel.setText("Aucun étudiant sélectionné");
        selectedStudentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        selectedModuleLabel.setText("Aucun module sélectionné");
        selectedModuleLabel.setForeground(MainFrame.TEXT_SECONDARY);
        moduleDropdown.setItems(getModulesWithTeacher());
        confirmAddBtn.setEnabled(false);
        cardLayout.show(this, CARD_ADD);
    }

    private void refreshModuleDropdown() {
        List<ModuleEtude> mods = getModulesWithTeacher();
        if (selectedStudent != null) {
            List<Integer> enrolled = inscriptionDAO.getInscriptionsByStudent(selectedStudent.getIdEtudiant())
                .stream().map(i -> i.getModule().getIdModule()).collect(Collectors.toList());
            mods = mods.stream().filter(m -> !enrolled.contains(m.getIdModule())).collect(Collectors.toList());
        }
        moduleDropdown.setItems(mods);
        moduleDropdown.clearSelection();
        selectedModule = null;
        selectedModuleLabel.setText("Aucun module sélectionné");
        selectedModuleLabel.setForeground(MainFrame.TEXT_SECONDARY);
    }

    private void updateConfirmButton() {
        confirmAddBtn.setEnabled(selectedStudent != null && selectedModule != null);
    }

    private void confirmAdd() {
        if (selectedStudent == null || selectedModule == null) return;
        if (inscriptionDAO.isAlreadyEnrolled(selectedStudent.getIdEtudiant(), selectedModule.getIdModule())) {
            showMsg("Cet étudiant est déjà inscrit à ce module.", "Doublon", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = inscriptionDAO.enrollStudent(selectedStudent, selectedModule);
        if (ok) {
            showMsg("Inscription créée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            if (controller != null) controller.resetAllDirtyExcept(NavigationController.Section.ENROLLMENT);
            reset();
        } else {
            showMsg("Erreur lors de la création de l'inscription.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    

    private JPanel buildRemovePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[-] Supprimer une inscription");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        JLabel lbl = new JLabel("Sélectionner une inscription à supprimer :");
        lbl.setFont(MainFrame.FONT_TITLE);
        lbl.setForeground(MainFrame.ACCENT_BLUE);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 0, 8, 0));

        removeDropdown = new SearchableDropdown<>(
            List.of(),
            i -> i.getEtudiant().getNom() + " " + i.getEtudiant().getPrenom()
               + "  ->  " + i.getModule().getNomModule(),
            i -> String.valueOf(i.getIdInscription()));
        removeDropdown.setOnSelect(ins -> {
            selectedEnrollment = ins;
            removeSelectedLabel.setText("[OK] " + ins.getEtudiant().getNom() + " " + ins.getEtudiant().getPrenom()
                + "  ->  " + ins.getModule().getNomModule());
            removeSelectedLabel.setForeground(MainFrame.DANGER_RED);
            confirmRemoveBtn.setEnabled(true);
        });

        removeSelectedLabel = new JLabel("Aucune inscription sélectionnée");
        removeSelectedLabel.setFont(MainFrame.FONT_LABEL);
        removeSelectedLabel.setForeground(MainFrame.TEXT_SECONDARY);

        confirmRemoveBtn = new JButton("[-] Confirmer la suppression");
        confirmRemoveBtn.setFont(MainFrame.FONT_TITLE);
        confirmRemoveBtn.setBackground(new Color(0x7A, 0x1A, 0x1A));
        confirmRemoveBtn.setForeground(Color.WHITE);
        confirmRemoveBtn.setEnabled(false);
        confirmRemoveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmRemoveBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        confirmRemoveBtn.addActionListener(e -> confirmRemove());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(MainFrame.BG_PANEL);
        center.add(lbl);
        center.add(removeDropdown);
        center.add(Box.createVerticalStrut(12));
        center.add(removeSelectedLabel);
        outer.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(MainFrame.BG_PANEL);
        south.add(confirmRemoveBtn);
        outer.add(south, BorderLayout.SOUTH);

        return outer;
    }

    private void showRemove() {
        if (controller != null) {
            controller.markDirty(NavigationController.Section.ENROLLMENT);
            controller.resetAllDirtyExcept(NavigationController.Section.ENROLLMENT);
        }
        
        List<Inscription> all = getAllInscriptions();
        removeDropdown.setItems(all);
        removeDropdown.clearSelection();
        selectedEnrollment = null;
        removeSelectedLabel.setText("Aucune inscription sélectionnée");
        removeSelectedLabel.setForeground(MainFrame.TEXT_SECONDARY);
        confirmRemoveBtn.setEnabled(false);
        cardLayout.show(this, CARD_REMOVE);
    }

    private void confirmRemove() {
        if (selectedEnrollment == null) return;
        int choice = JOptionPane.showConfirmDialog(this,
            "Supprimer l'inscription de « " + selectedEnrollment.getEtudiant().getNom()
            + " » au module « " + selectedEnrollment.getModule().getNomModule()
            + " » ?\n\nToutes les notes liées seront supprimées.",
            "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        boolean ok = inscriptionDAO.deleteEnrolment(selectedEnrollment.getIdInscription());
        if (ok) {
            showMsg("Inscription supprimée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            if (controller != null) controller.resetAllDirtyExcept(NavigationController.Section.ENROLLMENT);
            reset();
        } else {
            showMsg("Erreur lors de la suppression.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    

    private JPanel buildListPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[=] Liste des inscriptions");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        String[] cols = {"#", "Étudiant", "Module", "Enseignant", "Date", "Statut"};
        listTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(listTableModel);
        table.setBackground(MainFrame.BG_CARD);
        table.setForeground(MainFrame.TEXT_PRIMARY);
        table.setFont(MainFrame.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(MainFrame.FONT_TITLE);
        table.getTableHeader().setBackground(MainFrame.BG_PANEL);
        table.getTableHeader().setForeground(MainFrame.ACCENT_GOLD);
        table.setSelectionBackground(MainFrame.NAV_SELECT);

        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String status = (String) t.getModel().getValueAt(row, 5);
                    if ("Validé".equals(status))        c.setBackground(new Color(0x1A, 0x3A, 0x2A));
                    else if ("Ajourné".equals(status))  c.setBackground(new Color(0x3A, 0x1A, 0x1A));
                    else                                 c.setBackground(row % 2 == 0 ? MainFrame.BG_CARD : new Color(0x18, 0x28, 0x40));
                    c.setForeground(MainFrame.TEXT_PRIMARY);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE));
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        outer.add(scroll, BorderLayout.CENTER);

        return outer;
    }

    private void showList() {
        if (controller != null) controller.markDirty(NavigationController.Section.ENROLLMENT);
        listTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Inscription ins : getAllInscriptions()) {
            String teacher = ins.getModule().getEnseignant() != null
                ? ins.getModule().getEnseignant().getNom() : "—";
            String status;
            Boolean val = ins.getIsValidated();
            if (val == null)       status = "En cours";
            else if (val)          status = "Validé";
            else                   status = "Ajourné";
            listTableModel.addRow(new Object[]{
                ins.getIdInscription(),
                ins.getEtudiant().getNom() + " " + ins.getEtudiant().getPrenom(),
                ins.getModule().getNomModule(),
                teacher,
                sdf.format(ins.getDateInscription()),
                status
            });
        }
        cardLayout.show(this, CARD_LIST);
    }

    

    
    private List<ModuleEtude> getModulesWithTeacher() {
        return moduleDAO.getAllModules().stream()
            .filter(m -> m.getEnseignant() != null)
            .collect(Collectors.toList());
    }

    
    private List<Inscription> getAllInscriptions() {
        return etudiantDAO.getAllEtudiants().stream()
            .flatMap(e -> inscriptionDAO.getInscriptionsByStudent(e.getIdEtudiant()).stream())
            .collect(Collectors.toList());
    }

    private JButton makeBackButton() {
        JButton btn = new JButton("← Retour");
        btn.setFont(MainFrame.FONT_LABEL);
        btn.setBackground(MainFrame.BG_CARD);
        btn.setForeground(MainFrame.TEXT_SECONDARY);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
}