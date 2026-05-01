package lagraa_yasser_assil_project.UI.components;

import lagraa_yasser_assil_project.UI.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * NavigationController — the "brain" of the UI.
 *
 * Responsibilities:
 *  1. Switching which card is visible in ContentPanel.
 *  2. Implementing "Context Saving":
 *     - If the user navigates to another SECTION (e.g. Students → Teachers)
 *       WITHOUT having started an action, the previous panel is left untouched.
 *     - If the user navigates away, the previous panel KEEPS its state (form values,
 *       selected list items, partial data) — UNLESS they START AN ACTION inside
 *       the new section, at which point the old panel is reset().
 *  3. Tracking isDirty per section: a panel sets dirty=true when the user
 *     begins typing / selecting for a multi-step action.
 *  4. Providing a clearDirty(section) method that panels call on confirm/cancel.
 *
 * "Context is saved if the user just navigated away.
 *  Context is lost only when they CONFIRM or INITIATE an action in another section."
 *
 * This is implemented via:
 *   - dirtyMap: tracks whether each section has uncommitted work.
 *   - resetCallbacks: each panel registers a Runnable that fully resets it to Level-1.
 *   - The controller asks the ContentPanel to show the chosen card, and IF the old
 *     section was NOT dirty, nothing changes on it. If it WAS dirty and the user
 *     STARTS an action somewhere new, the dirty panel gets reset.
 */
public class NavigationController {

    /** The section keys — must match the card names registered in ContentPanel. */
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

    /** The section currently visible in the ContentPanel. */
    private Section currentSection = null;

    /** Listeners notified when the active section changes (NavigationPanel uses this). */
    private final java.util.List<Consumer<Section>> sectionListeners = new java.util.ArrayList<>();

    public NavigationController(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
        for (Section s : Section.values()) {
            dirtyMap.put(s, false);
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Called when the user clicks a nav item.
     * Switches the visible card. Does NOT reset the destination panel —
     * that panel's state is preserved (context saving).
     */
    public void navigateTo(Section target) {
        currentSection = target;
        contentPanel.showCard(target.name());
        sectionListeners.forEach(l -> l.accept(target));
    }

    // ── Dirty tracking ────────────────────────────────────────────────────────

    /**
     * A panel calls this as soon as the user starts a multi-step action
     * (e.g. clicks "Add Student", filling a form).
     */
    public void markDirty(Section section) {
        dirtyMap.put(section, true);
    }

    /**
     * A panel calls this when the action is completed (confirmed or cancelled)
     * and the panel has returned to Level-1 (action selection).
     */
    public void clearDirty(Section section) {
        dirtyMap.put(section, false);
    }

    public boolean isDirty(Section section) {
        return Boolean.TRUE.equals(dirtyMap.get(section));
    }

    // ── Reset callbacks ───────────────────────────────────────────────────────

    /**
     * Panels register their reset() method here during construction.
     * The controller calls reset() when needed (e.g. user initiates an action
     * in a different section while this one was dirty).
     */
    public void registerReset(Section section, Runnable resetCallback) {
        resetCallbacks.put(section, resetCallback);
    }

    /**
     * Forces a panel back to its Level-1 state and clears its dirty flag.
     * Called when the user starts a new action in a different section while
     * the old section still has uncommitted work.
     */
    public void resetSection(Section section) {
        Runnable cb = resetCallbacks.get(section);
        if (cb != null) cb.run();
        clearDirty(section);
    }

    /**
     * Resets ALL sections that are currently dirty (except the active one).
     * Call this when the user CONFIRMS or STARTS an action in the current section.
     */
    public void resetAllDirtyExcept(Section except) {
        for (Section s : Section.values()) {
            if (s != except && isDirty(s)) {
                resetSection(s);
            }
        }
    }

    // ── Listener management ───────────────────────────────────────────────────

    public void addSectionListener(Consumer<Section> listener) {
        sectionListeners.add(listener);
    }

    public Section getCurrentSection() { return currentSection; }
}