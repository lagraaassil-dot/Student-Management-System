package lagraa_yasser_assil_project.UI.components;

import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.panels.*;
import lagraa_yasser_assil_project.UI.components.NavigationController.Section;

import javax.swing.*;
import java.awt.*;

// CardLayout wrapper that keeps all section panels alive in memory.
public class ContentPanel extends JPanel {

    private final CardLayout cardLayout;

    
    private StudentPanel    studentPanel;
    private ModulePanel     modulePanel;
    private TeacherPanel    teacherPanel;
    private EnrollmentPanel enrollmentPanel;
    private GradesPanel     gradesPanel;
    private ResultsPanel    resultsPanel;
    private DiplomaPanel    diplomaPanel;

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(MainFrame.BG_PANEL);

        
        studentPanel    = new StudentPanel();
        modulePanel     = new ModulePanel();
        teacherPanel    = new TeacherPanel();
        enrollmentPanel = new EnrollmentPanel();
        gradesPanel     = new GradesPanel();
        resultsPanel    = new ResultsPanel();
        diplomaPanel    = new DiplomaPanel();

        add(studentPanel,    Section.STUDENTS.name());
        add(modulePanel,     Section.MODULES.name());
        add(teacherPanel,    Section.TEACHERS.name());
        add(enrollmentPanel, Section.ENROLLMENT.name());
        add(gradesPanel,     Section.GRADES.name());
        add(resultsPanel,    Section.RESULTS.name());
        add(diplomaPanel,    Section.DIPLOMA.name());

        
        showWelcome();
    }

    
    public void showCard(String key) {
        cardLayout.show(this, key);

        
        if (Section.RESULTS.name().equals(key))  resultsPanel.refresh();
        if (Section.DIPLOMA.name().equals(key))  diplomaPanel.refresh();
    }

    

    public void registerResets(NavigationController controller) {
        controller.registerReset(Section.STUDENTS,   studentPanel::reset);
        controller.registerReset(Section.MODULES,    modulePanel::reset);
        controller.registerReset(Section.TEACHERS,   teacherPanel::reset);
        controller.registerReset(Section.ENROLLMENT, enrollmentPanel::reset);
        controller.registerReset(Section.GRADES,     gradesPanel::reset);
    }

    

    public StudentPanel    getStudentPanel()    { return studentPanel; }
    public ModulePanel     getModulePanel()     { return modulePanel; }
    public TeacherPanel    getTeacherPanel()    { return teacherPanel; }
    public EnrollmentPanel getEnrollmentPanel() { return enrollmentPanel; }
    public GradesPanel     getGradesPanel()     { return gradesPanel; }

    

    private void showWelcome() {
        JPanel welcome = new JPanel(new GridBagLayout());
        welcome.setBackground(MainFrame.BG_PANEL);

        JLabel icon = new JLabel("♦");
        icon.setFont(new Font("Courier New", Font.BOLD, 64));
        icon.setForeground(new Color(0x25, 0x3B, 0x5C));

        JLabel msg = new JLabel("Sélectionnez une section dans le menu de navigation.");
        msg.setFont(new Font("Courier New", Font.PLAIN, 14));
        msg.setForeground(MainFrame.TEXT_SECONDARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; welcome.add(icon, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(16, 0, 0, 0);
        welcome.add(msg, gbc);

        add(welcome, "WELCOME");
        cardLayout.show(this, "WELCOME");
    }
}
