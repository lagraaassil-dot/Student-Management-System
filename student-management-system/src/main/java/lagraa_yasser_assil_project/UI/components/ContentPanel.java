package lagraa_yasser_assil_project.UI.components;

import lagraa_yasser_assil_project.UI.MainFrame;
import lagraa_yasser_assil_project.UI.panels.*;
import lagraa_yasser_assil_project.UI.components.NavigationController.Section;

import javax.swing.*;
import java.awt.*;

/**
 * ContentPanel — a CardLayout wrapper that holds all functional panels.
 *
 * CRITICAL for Context Saving: because CardLayout only hides panels rather than
 * destroying them, every panel retains its in-memory state when the user switches
 * sections. The NavigationController decides when to explicitly call reset().
 *
 * Each card is keyed by Section.name() (e.g. "STUDENTS", "MODULES", …).
 */
public class ContentPanel extends JPanel {

    private final CardLayout cardLayout;

    // Functional panels — kept as fields so the controller can call reset() on them
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

        // Build all panels eagerly so their state is available immediately
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

        // Show a welcome/placeholder by default
        showWelcome();
    }

    /** Shows the card registered under the given key. */
    public void showCard(String key) {
        cardLayout.show(this, key);

        // Refresh read-only panels whenever they become visible
        if (Section.RESULTS.name().equals(key))  resultsPanel.refresh();
        if (Section.DIPLOMA.name().equals(key))  diplomaPanel.refresh();
    }

    // ── Reset passthrough (called by NavigationController) ───────────────────

    public void registerResets(NavigationController controller) {
        controller.registerReset(Section.STUDENTS,   studentPanel::reset);
        controller.registerReset(Section.MODULES,    modulePanel::reset);
        controller.registerReset(Section.TEACHERS,   teacherPanel::reset);
        controller.registerReset(Section.ENROLLMENT, enrollmentPanel::reset);
        controller.registerReset(Section.GRADES,     gradesPanel::reset);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public StudentPanel    getStudentPanel()    { return studentPanel; }
    public ModulePanel     getModulePanel()     { return modulePanel; }
    public TeacherPanel    getTeacherPanel()    { return teacherPanel; }
    public EnrollmentPanel getEnrollmentPanel() { return enrollmentPanel; }
    public GradesPanel     getGradesPanel()     { return gradesPanel; }

    // ── Welcome card ──────────────────────────────────────────────────────────

    private void showWelcome() {
        JPanel welcome = new JPanel(new GridBagLayout());
        welcome.setBackground(MainFrame.BG_PANEL);

        JLabel icon = new JLabel("◈");
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
