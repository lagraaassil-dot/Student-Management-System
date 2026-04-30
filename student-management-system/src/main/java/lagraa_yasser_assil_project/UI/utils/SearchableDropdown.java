package lagraa_yasser_assil_project.UI.utils;

import lagraa_yasser_assil_project.UI.MainFrame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SearchableDropdown&lt;T&gt; — a reusable composite component used throughout
 * the application wherever a "search + pick one from list" interaction is needed.
 *
 * Layout:
 *   ┌─────────────────────────────────┐
 *   │  🔍  [  search field          ] │
 *   ├─────────────────────────────────┤
 *   │  item A                         │  ← scrollable JList (max ~6 visible rows)
 *   │  item B  (selected → blue)      │
 *   │  item C                         │
 *   └─────────────────────────────────┘
 *
 * Usage:
 * <pre>
 *   SearchableDropdown&lt;Etudiant&gt; dd = new SearchableDropdown&lt;&gt;(
 *       students,
 *       e -> e.getNom() + " " + e.getPrenom(),   // display label
 *       e -> String.valueOf(e.getIdEtudiant())    // secondary search key
 *   );
 *   dd.setOnSelect(etudiant -> { ... });
 * </pre>
 *
 * @param <T> The model type (Etudiant, ModuleEtude, Enseignant, Inscription …)
 */
public class SearchableDropdown<T> extends JPanel {

    private final List<T>           allItems   = new ArrayList<>();
    private final List<T>           filtered   = new ArrayList<>();

    private final Function<T, String> displayFn;  // shown in the list
    private final Function<T, String> searchFn;   // extra searchable text (e.g. ID)

    private final JTextField         searchField;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String>      jList;

    private Consumer<T> onSelect;
    private T           selectedItem = null;
    private boolean     multiSelect  = false;
    private final List<T> selectedItems = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * @param items     initial list of items to display
     * @param displayFn maps T → the string shown in the list row
     * @param searchFn  maps T → additional string matched by the search bar
     *                  (pass {@code null} to only search by displayFn)
     */
    public SearchableDropdown(List<T> items,
                               Function<T, String> displayFn,
                               Function<T, String> searchFn) {
        this.displayFn = displayFn;
        this.searchFn  = searchFn;

        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        // ── Search field ──────────────────────────────────────────────────────
        searchField = new JTextField();
        searchField.setBackground(MainFrame.BG_CARD);
        searchField.setForeground(MainFrame.TEXT_PRIMARY);
        searchField.setCaretColor(MainFrame.ACCENT_GOLD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "🔍  Rechercher…");
        searchField.setFont(MainFrame.FONT_BODY);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        add(searchField, BorderLayout.NORTH);

        // ── JList ─────────────────────────────────────────────────────────────
        jList = new JList<>(listModel);
        jList.setBackground(MainFrame.BG_CARD);
        jList.setForeground(MainFrame.TEXT_PRIMARY);
        jList.setSelectionBackground(MainFrame.NAV_SELECT);
        jList.setSelectionForeground(MainFrame.TEXT_PRIMARY);
        jList.setFont(MainFrame.FONT_BODY);
        jList.setFixedCellHeight(30);
        jList.setCellRenderer(new StripedRenderer());
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int idx = jList.locationToIndex(e.getPoint());
                if (idx < 0 || idx >= filtered.size()) return;
                T item = filtered.get(idx);

                if (multiSelect) {
                    if (selectedItems.contains(item)) {
                        selectedItems.remove(item);
                        jList.removeSelectionInterval(idx, idx);
                    } else {
                        selectedItems.add(item);
                        jList.addSelectionInterval(idx, idx);
                    }
                    if (onSelect != null) onSelect.accept(item);
                } else {
                    selectedItem = item;
                    jList.setSelectedIndex(idx);
                    if (onSelect != null) onSelect.accept(item);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(jList);
        scroll.setBackground(MainFrame.BG_CARD);
        scroll.getViewport().setBackground(MainFrame.BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER_SUBTLE));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(0, 180)); // ~6 rows
        add(scroll, BorderLayout.CENTER);

        setItems(items);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Replace the entire item list and refresh the display. */
    public void setItems(List<T> items) {
        allItems.clear();
        allItems.addAll(items);
        filter();
    }

    /** Appends additional items without clearing existing ones. */
    public void addItems(List<T> items) {
        allItems.addAll(items);
        filter();
    }

    /** Callback fired when the user clicks an item. */
    public void setOnSelect(Consumer<T> onSelect) { this.onSelect = onSelect; }

    /** Returns the single selected item, or null if nothing is selected. */
    public T getSelectedItem() { return selectedItem; }

    /** Returns all selected items (multi-select mode only). */
    public List<T> getSelectedItems() { return new ArrayList<>(selectedItems); }

    /** Clears the search field and selection. */
    public void clearSelection() {
        searchField.setText("");
        jList.clearSelection();
        selectedItem  = null;
        selectedItems.clear();
        filter();
    }

    /** Enables/disables multi-select mode. */
    public void setMultiSelect(boolean multi) {
        this.multiSelect = multi;
        jList.setSelectionMode(multi
            ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            : ListSelectionModel.SINGLE_SELECTION);
    }

    /** Removes a specific item from the list (e.g. after it has been selected). */
    public void removeItem(T item) {
        allItems.remove(item);
        selectedItems.remove(item);
        if (item == selectedItem) selectedItem = null;
        filter();
    }

    /** Manually sets the selected item and refreshes the UI selection. */
public void setSelectedItem(T item) {
    this.selectedItem = item;
    if (item == null) {
        jList.clearSelection();
    } else {
        // Find the index in the current filtered list to highlight it in the JList
        int index = filtered.indexOf(item);
        if (index != -1) {
            jList.setSelectedIndex(index);
            jList.ensureIndexIsVisible(index);
        }
    }
}

    // ── Filtering ─────────────────────────────────────────────────────────────

    private void filter() {
        String query = searchField.getText().trim().toLowerCase();
        filtered.clear();
        listModel.clear();

        for (T item : allItems) {
            String display = displayFn.apply(item).toLowerCase();
            String extra   = searchFn != null ? searchFn.apply(item).toLowerCase() : "";
            if (query.isEmpty() || display.contains(query) || extra.contains(query)) {
                filtered.add(item);
                listModel.addElement(displayFn.apply(item));
            }
        }

        // Re-sync multi-select highlights
        if (multiSelect) {
            for (int i = 0; i < filtered.size(); i++) {
                if (selectedItems.contains(filtered.get(i))) {
                    jList.addSelectionInterval(i, i);
                }
            }
        }
    }

    // ── Striped cell renderer ─────────────────────────────────────────────────

    private class StripedRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));
            lbl.setFont(MainFrame.FONT_BODY);

            if (multiSelect && index < filtered.size()
                    && selectedItems.contains(filtered.get(index))) {
                lbl.setBackground(MainFrame.NAV_SELECT);
                lbl.setForeground(MainFrame.ACCENT_BLUE);
            } else if (isSelected) {
                lbl.setBackground(MainFrame.NAV_SELECT);
                lbl.setForeground(MainFrame.TEXT_PRIMARY);
            } else {
                lbl.setBackground(index % 2 == 0 ? MainFrame.BG_CARD
                        : new Color(0x18, 0x28, 0x40));
                lbl.setForeground(MainFrame.TEXT_PRIMARY);
            }
            return lbl;
        }
    }
}
