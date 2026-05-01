package lagraa_yasser_assil_project.UI.panels;

import lagraa_yasser_assil_project.dao.EtudiantDAO;
import lagraa_yasser_assil_project.dao.InscriptionDAO;
import lagraa_yasser_assil_project.dao.NoteDAO;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Note;
import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.components.NavigationController;
import lagraa_yasser_assil_project.UI.utils.SearchableDropdown;
import lagraa_yasser_assil_project.UI.utils.UIValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Panel for adding, editing, and deleting grades.
public class GradesPanel extends JPanel {

    
    private final EtudiantDAO    etudiantDAO    = new EtudiantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final NoteDAO        noteDAO        = new NoteDAO();

    
    private final CardLayout cardLayout = new CardLayout();
    private static final String CARD_LEVEL1 = "LEVEL1";
    private static final String CARD_ADD    = "ADD";
    private static final String CARD_MODIFY = "MODIFY";
    private static final String CARD_DELETE = "DELETE";
    private static final String CARD_LIST   = "LIST";

    private NavigationController controller;

    
    private SearchableDropdown<Inscription> addEnrollmentDropdown;
    private JLabel addEnrollmentLabel;
    private JLabel addTypeLabel;
    private JTextField addGradeField;
    private JButton confirmAddBtn;
    private Inscription addSelectedEnrollment;
    private int addNoteType = -1;

    
    private SearchableDropdown<Inscription> modEnrollmentDropdown;
    private JLabel modEnrollmentLabel;
    private JPanel modNotesPanel;
    private JButton confirmModBtn;
    private Inscription modSelectedEnrollment;
    private List<Note> modNotes = new ArrayList<>();
    private List<JTextField> modFields = new ArrayList<>();

    
    private SearchableDropdown<Inscription> delEnrollmentDropdown;
    private JLabel delEnrollmentLabel;
    private JComboBox<String> delTypeCombo;
    private JButton confirmDelBtn;
    private Inscription delSelectedEnrollment;

    
    private DefaultTableModel listModel;
    private JTable listTable;
    private boolean listSortAsc = true;

    public GradesPanel() {
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);
        add(buildLevel1Panel(), CARD_LEVEL1);
        add(buildAddPanel(),    CARD_ADD);
        add(buildModifyPanel(), CARD_MODIFY);
        add(buildDeletePanel(), CARD_DELETE);
        add(buildListPanel(),   CARD_LIST);
        cardLayout.show(this, CARD_LEVEL1);
    }

    public void setController(NavigationController controller) { this.controller = controller; }

    public void reset() {
        addSelectedEnrollment = null;
        modSelectedEnrollment = null;
        delSelectedEnrollment = null;
        cardLayout.show(this, CARD_LEVEL1);
        if (controller != null) controller.clearDirty(NavigationController.Section.GRADES);
    }

    

    private JPanel buildLevel1Panel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.BG_PANEL);

        JLabel title = new JLabel("[#] Notes");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);

        JButton btnAdd    = makeActionButton("[+] Ajouter une note");
        JButton btnMod    = makeActionButton("[*] Modifier une note");
        JButton btnDel    = makeActionButton("[-] Supprimer une note");
        JButton btnList   = makeActionButton("[=] Afficher toutes les notes");

        btnAdd.addActionListener(e  -> showAdd());
        btnMod.addActionListener(e  -> showModify());
        btnDel.addActionListener(e  -> showDelete());
        btnList.addActionListener(e -> showList());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 0, 10, 0); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0; p.add(title, gbc);
        gbc.gridy = 1; p.add(btnAdd, gbc);
        gbc.gridy = 2; p.add(btnMod, gbc);
        gbc.gridy = 3; p.add(btnDel, gbc);
        gbc.gridy = 4; p.add(btnList, gbc);
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
        btn.setPreferredSize(new Dimension(300, 48));
        return btn;
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

    

    private JPanel buildAddPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[+] Ajouter une note");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(MainFrame.BG_PANEL);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel ddLabel = new JLabel("Choisir une inscription :");
        ddLabel.setFont(MainFrame.FONT_TITLE);
        ddLabel.setForeground(MainFrame.ACCENT_BLUE);
        ddLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addEnrollmentDropdown = new SearchableDropdown<>(List.of(), this::enrollmentDisplay, i -> "");
        addEnrollmentDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        addEnrollmentDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        addEnrollmentLabel = new JLabel("Aucune inscription sélectionnée");
        addEnrollmentLabel.setFont(MainFrame.FONT_LABEL);
        addEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        addEnrollmentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addTypeLabel = new JLabel("Type de note : —");
        addTypeLabel.setFont(MainFrame.FONT_TITLE);
        addTypeLabel.setForeground(MainFrame.ACCENT_BLUE);
        addTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel gradeLabel = new JLabel("Valeur de la note (ex: 12.50) :");
        gradeLabel.setFont(MainFrame.FONT_LABEL);
        gradeLabel.setForeground(MainFrame.TEXT_PRIMARY);
        gradeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addGradeField = new JTextField(10);
        addGradeField.setMaximumSize(new Dimension(200, 36));
        addGradeField.setAlignmentX(Component.LEFT_ALIGNMENT);

        addEnrollmentDropdown.setOnSelect(ins -> {
            addSelectedEnrollment = ins;
            addNoteType = determineNextNoteType(ins);
            addEnrollmentLabel.setText("[OK] " + enrollmentDisplay(ins));
            addEnrollmentLabel.setForeground(MainFrame.SUCCESS_GREEN);
            String typeStr = addNoteType == 0 ? "CC (Contrôle Continu)"
                           : addNoteType == 1 ? "Examen (session normale)"
                           : "Rattrapage";
            addTypeLabel.setText("Type de note : " + typeStr);
            addGradeField.setText("");
            confirmAddBtn.setEnabled(true);
        });

        center.add(ddLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(addEnrollmentDropdown);
        center.add(Box.createVerticalStrut(10));
        center.add(addEnrollmentLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(addTypeLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(gradeLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(addGradeField);
        outer.add(center, BorderLayout.CENTER);

        confirmAddBtn = new JButton("[OK] Enregistrer la note");
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
            controller.markDirty(NavigationController.Section.GRADES);
            controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
        }
        addSelectedEnrollment = null;
        addNoteType = -1;
        addEnrollmentLabel.setText("Aucune inscription sélectionnée");
        addEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        addTypeLabel.setText("Type de note : —");
        addGradeField.setText("");
        confirmAddBtn.setEnabled(false);
        addEnrollmentDropdown.setItems(getEnrollmentsEligibleForNewNote());
        addEnrollmentDropdown.clearSelection();
        cardLayout.show(this, CARD_ADD);
    }

    private void confirmAdd() {
        if (addSelectedEnrollment == null || addNoteType < 0) return;
        String raw = addGradeField.getText().trim();
        String err = UIValidator.gradeErrorMessage(raw);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Note invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double val = UIValidator.parseGrade(raw);
        Note n = new Note(val, addNoteType, addNoteType == 2, addSelectedEnrollment);
        boolean ok = noteDAO.addNote(n);
        if (ok) {
            
            int insId = addSelectedEnrollment.getIdInscription();
            double avg;
            if (addNoteType == 2) {
                avg = inscriptionDAO.calculateAndSaveAverageAfterResit(insId);
            } else {
                avg = inscriptionDAO.calculateAndSaveAverage(insId);
            }
            if (avg >= 0) {
                etudiantDAO.checkAndSetDiploma(addSelectedEnrollment.getEtudiant());
            }
            JOptionPane.showMessageDialog(this, "Note ajoutée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            if (controller != null) controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
            reset();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout de la note.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    

    private JPanel buildModifyPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[*] Modifier une note");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(MainFrame.BG_PANEL);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel ddLabel = new JLabel("Inscription avec notes :");
        ddLabel.setFont(MainFrame.FONT_TITLE);
        ddLabel.setForeground(MainFrame.ACCENT_BLUE);
        ddLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modEnrollmentDropdown = new SearchableDropdown<>(List.of(), this::enrollmentDisplay, i -> "");
        modEnrollmentDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        modEnrollmentDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        modEnrollmentLabel = new JLabel("Aucune inscription sélectionnée");
        modEnrollmentLabel.setFont(MainFrame.FONT_LABEL);
        modEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        modEnrollmentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modNotesPanel = new JPanel();
        modNotesPanel.setLayout(new BoxLayout(modNotesPanel, BoxLayout.Y_AXIS));
        modNotesPanel.setBackground(MainFrame.BG_PANEL);
        modNotesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modEnrollmentDropdown.setOnSelect(ins -> {
            modSelectedEnrollment = ins;
            modEnrollmentLabel.setText("[OK] " + enrollmentDisplay(ins));
            modEnrollmentLabel.setForeground(MainFrame.SUCCESS_GREEN);
            buildModNoteFields(ins);
            confirmModBtn.setEnabled(true);
        });

        center.add(ddLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(modEnrollmentDropdown);
        center.add(Box.createVerticalStrut(8));
        center.add(modEnrollmentLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(modNotesPanel);
        outer.add(center, BorderLayout.CENTER);

        confirmModBtn = new JButton("[OK] Enregistrer les modifications");
        confirmModBtn.setFont(MainFrame.FONT_TITLE);
        confirmModBtn.setBackground(new Color(0x1A, 0x3A, 0x5C));
        confirmModBtn.setForeground(Color.WHITE);
        confirmModBtn.setEnabled(false);
        confirmModBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmModBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        confirmModBtn.addActionListener(e -> confirmModify());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(MainFrame.BG_PANEL);
        south.add(confirmModBtn);
        outer.add(south, BorderLayout.SOUTH);

        return outer;
    }

    private void buildModNoteFields(Inscription ins) {
        modNotesPanel.removeAll();
        modNotes = noteDAO.getNotesByInscription(ins.getIdInscription());
        modFields.clear();
        for (Note note : modNotes) {
            String typeStr = note.getTypeNote() == 0 ? "CC" : note.getTypeNote() == 1 ? "Examen" : "Rattrapage";
            JLabel lbl = new JLabel(typeStr + " : ");
            lbl.setFont(MainFrame.FONT_LABEL);
            lbl.setForeground(MainFrame.TEXT_PRIMARY);
            JTextField field = new JTextField(String.valueOf(note.getValeur()), 8);
            field.setMaximumSize(new Dimension(160, 32));
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(MainFrame.BG_PANEL);
            row.add(lbl);
            row.add(field);
            modNotesPanel.add(row);
            modFields.add(field);
        }
        modNotesPanel.revalidate();
        modNotesPanel.repaint();
    }

    private void showModify() {
        if (controller != null) {
            controller.markDirty(NavigationController.Section.GRADES);
            controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
        }
        modSelectedEnrollment = null;
        modEnrollmentLabel.setText("Aucune inscription sélectionnée");
        modEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        modNotesPanel.removeAll();
        modNotes.clear();
        modFields.clear();
        confirmModBtn.setEnabled(false);
        modEnrollmentDropdown.setItems(getEnrollmentsWithNotes());
        modEnrollmentDropdown.clearSelection();
        cardLayout.show(this, CARD_MODIFY);
    }

    private void confirmModify() {
        if (modSelectedEnrollment == null || modNotes.isEmpty()) return;
        for (int i = 0; i < modNotes.size(); i++) {
            String raw = modFields.get(i).getText().trim();
            String err = UIValidator.gradeErrorMessage(raw);
            if (err != null) {
                JOptionPane.showMessageDialog(this,
                    "Note invalide pour " + (modNotes.get(i).getTypeNote() == 0 ? "CC" : modNotes.get(i).getTypeNote() == 1 ? "Examen" : "Rattrapage")
                    + " : " + err, "Note invalide", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        for (int i = 0; i < modNotes.size(); i++) {
            double newVal = UIValidator.parseGrade(modFields.get(i).getText().trim());
            noteDAO.updateNote(modNotes.get(i).getIdNote(), newVal);
        }
        
        int insId = modSelectedEnrollment.getIdInscription();
        boolean hasResit = modNotes.stream().anyMatch(n -> n.getTypeNote() == 2);
        if (hasResit) inscriptionDAO.calculateAndSaveAverageAfterResit(insId);
        else          inscriptionDAO.calculateAndSaveAverage(insId);
        etudiantDAO.checkAndSetDiploma(modSelectedEnrollment.getEtudiant());

        JOptionPane.showMessageDialog(this, "Notes modifiées avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        if (controller != null) controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
        reset();
    }

    

    private JPanel buildDeletePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[-] Supprimer une note");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(MainFrame.BG_PANEL);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel ddLabel = new JLabel("Inscription avec notes :");
        ddLabel.setFont(MainFrame.FONT_TITLE);
        ddLabel.setForeground(MainFrame.ACCENT_BLUE);
        ddLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        delEnrollmentDropdown = new SearchableDropdown<>(List.of(), this::enrollmentDisplay, i -> "");
        delEnrollmentDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        delEnrollmentDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        delEnrollmentLabel = new JLabel("Aucune inscription sélectionnée");
        delEnrollmentLabel.setFont(MainFrame.FONT_LABEL);
        delEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        delEnrollmentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel typeLabel = new JLabel("Type de note à supprimer :");
        typeLabel.setFont(MainFrame.FONT_LABEL);
        typeLabel.setForeground(MainFrame.TEXT_PRIMARY);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        delTypeCombo = new JComboBox<>(new String[]{"Rattrapage (type 2)", "Examen (type 1)", "CC (type 0)"});
        delTypeCombo.setBackground(MainFrame.BG_CARD);
        delTypeCombo.setForeground(MainFrame.TEXT_PRIMARY);
        delTypeCombo.setFont(MainFrame.FONT_BODY);
        delTypeCombo.setMaximumSize(new Dimension(300, 36));
        delTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("[i] La suppression d'une note de type inférieur entraîne aussi la suppression des notes de type supérieur.");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        infoLabel.setForeground(MainFrame.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        delEnrollmentDropdown.setOnSelect(ins -> {
            delSelectedEnrollment = ins;
            delEnrollmentLabel.setText("[OK] " + enrollmentDisplay(ins));
            delEnrollmentLabel.setForeground(MainFrame.DANGER_RED);
            
            java.util.List<Note> existingNotes = noteDAO.getNotesByInscription(ins.getIdInscription());
            boolean hasCC   = existingNotes.stream().anyMatch(n -> n.getTypeNote() == 0);
            boolean hasExam = existingNotes.stream().anyMatch(n -> n.getTypeNote() == 1);
            boolean hasResit= existingNotes.stream().anyMatch(n -> n.getTypeNote() == 2);
            delTypeCombo.removeAllItems();
            if (hasResit) delTypeCombo.addItem("Rattrapage (type 2)");
            if (hasExam)  delTypeCombo.addItem("Examen (type 1)");
            if (hasCC)    delTypeCombo.addItem("CC (type 0)");
            confirmDelBtn.setEnabled(!existingNotes.isEmpty());
        });

        center.add(ddLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(delEnrollmentDropdown);
        center.add(Box.createVerticalStrut(8));
        center.add(delEnrollmentLabel);
        center.add(Box.createVerticalStrut(14));
        center.add(typeLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(delTypeCombo);
        center.add(Box.createVerticalStrut(10));
        center.add(infoLabel);
        outer.add(center, BorderLayout.CENTER);

        confirmDelBtn = new JButton("[-] Confirmer la suppression");
        confirmDelBtn.setFont(MainFrame.FONT_TITLE);
        confirmDelBtn.setBackground(new Color(0x7A, 0x1A, 0x1A));
        confirmDelBtn.setForeground(Color.WHITE);
        confirmDelBtn.setEnabled(false);
        confirmDelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmDelBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        confirmDelBtn.addActionListener(e -> confirmDelete());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(MainFrame.BG_PANEL);
        south.add(confirmDelBtn);
        outer.add(south, BorderLayout.SOUTH);

        return outer;
    }

    private void showDelete() {
        if (controller != null) {
            controller.markDirty(NavigationController.Section.GRADES);
            controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
        }
        delSelectedEnrollment = null;
        delEnrollmentLabel.setText("Aucune inscription sélectionnée");
        delEnrollmentLabel.setForeground(MainFrame.TEXT_SECONDARY);
        confirmDelBtn.setEnabled(false);
        delEnrollmentDropdown.setItems(getEnrollmentsWithNotes());
        delEnrollmentDropdown.clearSelection();
        cardLayout.show(this, CARD_DELETE);
    }

    private void confirmDelete() {
        if (delSelectedEnrollment == null) return;
        int insId = delSelectedEnrollment.getIdInscription();
        
        String selItem = (String) delTypeCombo.getSelectedItem();
        if (selItem == null) return;
        int targetType = selItem.contains("type 2") ? 2 : selItem.contains("type 1") ? 1 : 0;

        List<Note> existingNotes = noteDAO.getNotesByInscription(insId);
        List<Integer> typesPresent = existingNotes.stream().map(Note::getTypeNote).collect(Collectors.toList());

        
        if (targetType == 1 && typesPresent.contains(2)) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Attention : supprimer l'Examen entraîne aussi la suppression du Rattrapage.\n\nContinuer ?",
                "Suppression en cascade", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        if (targetType == 0 && (typesPresent.contains(1) || typesPresent.contains(2))) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Attention : supprimer le CC entraîne aussi la suppression de l'Examen et du Rattrapage (s'ils existent).\n\nContinuer ?",
                "Suppression en cascade", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        
        for (int t = 2; t >= targetType; t--) {
            final int type = t;
            existingNotes.stream()
                .filter(n -> n.getTypeNote() == type)
                .forEach(n -> noteDAO.deleteNote(n.getIdNote()));
        }

        
        List<Note> remaining = noteDAO.getNotesByInscription(insId);
        boolean hasCC   = remaining.stream().anyMatch(n -> n.getTypeNote() == 0);
        boolean hasExam = remaining.stream().anyMatch(n -> n.getTypeNote() == 1);
        if (hasCC && hasExam) {
            inscriptionDAO.calculateAndSaveAverage(insId);
        } else {
            
            resetInscriptionValidated(insId);
        }

        
        revokeDiplomaIfNeeded(delSelectedEnrollment.getEtudiant());

        JOptionPane.showMessageDialog(this, "Note(s) supprimée(s).", "Succès", JOptionPane.INFORMATION_MESSAGE);
        if (controller != null) controller.resetAllDirtyExcept(NavigationController.Section.GRADES);
        reset();
    }

    

    private JPanel buildListPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(MainFrame.BG_PANEL);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.BG_PANEL);
        JLabel title = new JLabel("[=] Toutes les notes");
        title.setFont(MainFrame.FONT_DISPLAY);
        title.setForeground(MainFrame.ACCENT_GOLD);
        JButton back = makeBackButton();
        back.addActionListener(e -> reset());
        header.add(title, BorderLayout.WEST);
        header.add(back,  BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);

        String[] cols = {"Module", "Étudiant", "Note", "Type"};
        listModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        listTable = new JTable(listModel);
        listTable.setBackground(MainFrame.BG_CARD);
        listTable.setForeground(MainFrame.TEXT_PRIMARY);
        listTable.setFont(MainFrame.FONT_BODY);
        listTable.setRowHeight(28);
        listTable.getTableHeader().setFont(MainFrame.FONT_TITLE);
        listTable.getTableHeader().setBackground(MainFrame.BG_PANEL);
        listTable.getTableHeader().setForeground(MainFrame.ACCENT_GOLD);
        listTable.setSelectionBackground(MainFrame.NAV_SELECT);

        
        listTable.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = listTable.columnAtPoint(e.getPoint());
                if (col == 2) {
                    listSortAsc = !listSortAsc;
                    sortListByGrade();
                }
            }
        });

        
        listTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    try {
                        double grade = Double.parseDouble(t.getModel().getValueAt(row, 2).toString());
                        c.setBackground(grade >= 10
                            ? new Color(0x1A, 0x3A, 0x2A)
                            : new Color(0x3A, 0x1A, 0x1A));
                    } catch (Exception ex) {
                        c.setBackground(row % 2 == 0 ? MainFrame.BG_CARD : new Color(0x18, 0x28, 0x40));
                    }
                    c.setForeground(MainFrame.TEXT_PRIMARY);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(listTable);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE));
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        outer.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  ^v Cliquez sur l'en-tête « Note » pour trier");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(MainFrame.TEXT_SECONDARY);
        outer.add(hint, BorderLayout.SOUTH);

        return outer;
    }

    private void showList() {
        if (controller != null) controller.markDirty(NavigationController.Section.GRADES);
        listModel.setRowCount(0);
        listSortAsc = true;
        
        List<Note> allNotes = new ArrayList<>();
        for (Etudiant e : etudiantDAO.getAllEtudiants()) {
            allNotes.addAll(noteDAO.getNotesByStudent(e.getIdEtudiant()));
        }
        for (Note n : allNotes) {
            String typeStr = n.getTypeNote() == 0 ? "CC" : n.getTypeNote() == 1 ? "Examen" : "Rattrapage";
            Inscription ins = n.getEnrolment();
            listModel.addRow(new Object[]{
                ins.getModule().getNomModule(),
                ins.getEtudiant().getNom() + " " + ins.getEtudiant().getPrenom(),
                n.getValeur(),
                typeStr
            });
        }
        cardLayout.show(this, CARD_LIST);
    }

    private void sortListByGrade() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < listModel.getRowCount(); i++) {
            Object[] row = new Object[listModel.getColumnCount()];
            for (int j = 0; j < listModel.getColumnCount(); j++) row[j] = listModel.getValueAt(i, j);
            rows.add(row);
        }
        rows.sort((a, b) -> {
            double da = (double) a[2], db = (double) b[2];
            return listSortAsc ? Double.compare(da, db) : Double.compare(db, da);
        });
        listModel.setRowCount(0);
        for (Object[] row : rows) listModel.addRow(row);
    }

    

    
    private List<Inscription> getEnrollmentsEligibleForNewNote() {
        List<Inscription> result = new ArrayList<>();
        for (Etudiant e : etudiantDAO.getAllEtudiants()) {
            for (Inscription ins : inscriptionDAO.getInscriptionsByStudent(e.getIdEtudiant())) {
                if (ins.getModule().getEnseignant() == null) continue; 
                List<Note> notes = noteDAO.getNotesByInscription(ins.getIdInscription());
                boolean hasCC     = notes.stream().anyMatch(n -> n.getTypeNote() == 0);
                boolean hasExam   = notes.stream().anyMatch(n -> n.getTypeNote() == 1);
                boolean hasResit  = notes.stream().anyMatch(n -> n.getTypeNote() == 2);
                
                Boolean val = ins.getIsValidated();
                if (Boolean.TRUE.equals(val) && !hasResit) continue; 
                if (hasResit) continue; 
                result.add(ins);
            }
        }
        return result;
    }

    
    private List<Inscription> getEnrollmentsWithNotes() {
        List<Inscription> result = new ArrayList<>();
        for (Etudiant e : etudiantDAO.getAllEtudiants()) {
            for (Inscription ins : inscriptionDAO.getInscriptionsByStudent(e.getIdEtudiant())) {
                List<Note> notes = noteDAO.getNotesByInscription(ins.getIdInscription());
                if (!notes.isEmpty()) result.add(ins);
            }
        }
        return result;
    }

    
    private int determineNextNoteType(Inscription ins) {
        List<Note> notes = noteDAO.getNotesByInscription(ins.getIdInscription());
        boolean hasCC   = notes.stream().anyMatch(n -> n.getTypeNote() == 0);
        boolean hasExam = notes.stream().anyMatch(n -> n.getTypeNote() == 1);
        if (!hasCC)           return 0;
        if (!hasExam)         return 1;
        return 2; 
    }

    
    private void resetInscriptionValidated(int insId) {
        try (java.sql.Connection conn = lagraa_yasser_assil_project.utils.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE INSCRIPTION SET isValidated = NULL WHERE idInscription = ?")) {
            ps.setInt(1, insId);
            ps.executeUpdate();
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
    }

    
    private void revokeDiplomaIfNeeded(Etudiant etudiant) {
        try (java.sql.Connection conn = lagraa_yasser_assil_project.utils.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE ETUDIANT SET isDiplome = 0 WHERE idEtudiant = ?")) {
            ps.setInt(1, etudiant.getIdEtudiant());
            ps.executeUpdate();
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String enrollmentDisplay(Inscription ins) {
        return ins.getEtudiant().getNom() + " " + ins.getEtudiant().getPrenom()
             + "  →  " + ins.getModule().getNomModule();
    }
}