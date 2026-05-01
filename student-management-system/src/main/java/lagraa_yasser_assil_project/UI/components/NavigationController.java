package lagraa_yasser_assil_project.UI.components;

import lagraa_yasser_assil_project.UI.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// Routes navigation and tracks dirty state so panel context is preserved.
public class NavigationController {

    
    public enum Section {
        STUDENTS    ("[E] Etudiants"),
        MODULES     ("[M] Modules"),
        TEACHERS    ("[P] Enseignants"),
        ENROLLMENT  ("[I] Inscriptions"),
        GRADES      ("[N] Notes"),
        RESULTS     ("[R] Resultats"),
        DIPLOMA     ("[D] Diplome");

        private final String label;
        Section(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final ContentPanel                  contentPanel;
    private final Map<Section, Boolean>         dirtyMap      = new HashMap<>();
    private final Map<Section, Runnable>        resetCallbacks = new HashMap<>();
    private final Map<Section, Consumer<Void>>  actionStartedCallbacks = new HashMap<>();

    
    private Section currentSection = null;

    
    private final java.util.List<Consumer<Section>> sectionListeners = new java.util.ArrayList<>();

    public NavigationController(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
        for (Section s : Section.values()) {
            dirtyMap.put(s, false);
        }
    }

    

    
    public void navigateTo(Section target) {
        currentSection = target;
        contentPanel.showCard(target.name());
        sectionListeners.forEach(l -> l.accept(target));
    }

    

    
    public void markDirty(Section section) {
        dirtyMap.put(section, true);
    }

    
    public void clearDirty(Section section) {
        dirtyMap.put(section, false);
    }

    public boolean isDirty(Section section) {
        return Boolean.TRUE.equals(dirtyMap.get(section));
    }

    

    
    public void registerReset(Section section, Runnable resetCallback) {
        resetCallbacks.put(section, resetCallback);
    }

    
    public void resetSection(Section section) {
        Runnable cb = resetCallbacks.get(section);
        if (cb != null) cb.run();
        clearDirty(section);
    }

    
    public void resetAllDirtyExcept(Section except) {
        for (Section s : Section.values()) {
            if (s != except && isDirty(s)) {
                resetSection(s);
            }
        }
    }

    

    public void addSectionListener(Consumer<Section> listener) {
        sectionListeners.add(listener);
    }

    public Section getCurrentSection() { return currentSection; }
}