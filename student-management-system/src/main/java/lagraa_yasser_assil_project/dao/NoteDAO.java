package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Note;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NoteDAO — CRUD for the NOTE table + "best grade retained" logic.
 *
 * Note types (typeNote):
 *   0 → CC (Contrôle Continu)
 *   1 → Exam (session normale)
 *   2 → Rattrapage
 *
 * Table assumed:
 *   NOTE(idNote INT PK IDENTITY, valeur FLOAT, typeNote INT,
 *        idInscription INT FK → INSCRIPTION)
 *
 * The {@code Session} boolean in the constructor is unused in the DB schema
 * (the spec encodes session via typeNote), so we don't persist it separately.
 */
public class NoteDAO {

    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    // ------------------------------------------------------------------ CREATE

    /**
     * Inserts a note and writes the generated PK back into the object.
     *
     * Only one note of each type (CC, Exam, Rattrapage) is allowed per enrollment.
     * Returns false if a note of the same type already exists for that enrollment.
     *
     * @return true on success
     */
    public boolean addNote(Note n) {
        int insId    = n.getEnrolment().getIdInscription();
        int typeNote = n.getTypeNote();

        if (noteTypeExists(insId, typeNote)) return false; // duplicate guard

        String sql = "INSERT INTO NOTE (idInscription, valeur, typeNote) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, insId);
            ps.setDouble(2, n.getValeur());
            ps.setInt(3, typeNote);

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setIdNote(keys.getInt(1));
                }
            }
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ READ

    /**
     * Returns all notes (CC, Exam, Rattrapage) for a given enrollment.
     * Use this to display a student's grades for one module.
     */
    public List<Note> getNotesByInscription(int inscriptionId) {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT idNote, valeur, typeNote FROM NOTE WHERE idInscription = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inscriptionId);
            Inscription inscription = inscriptionDAO.getInscriptionById(inscriptionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, inscription));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Returns all notes for all enrollments of a student —
     * useful for a full grade report view.
     */
    public List<Note> getNotesByStudent(int studentId) {
        List<Note> list = new ArrayList<>();
        String sql = "SELECT n.idNote, n.valeur, n.typeNote, n.idInscription "
                   + "FROM NOTE n "
                   + "JOIN INSCRIPTION i ON n.idInscription = i.idInscription "
                   + "WHERE i.idEtudiant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int insId = rs.getInt("idInscription");
                    Inscription inscription = inscriptionDAO.getInscriptionById(insId);
                    list.add(mapRow(rs, inscription));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Returns the note value for a specific type in an enrollment,
     * or -1 if the note doesn't exist yet.
     *
     * @param typeNote 0 = CC, 1 = Exam, 2 = Rattrapage
     */
    public double getNoteValue(int inscriptionId, int typeNote) {
        String sql = "SELECT valeur FROM NOTE WHERE idInscription = ? AND typeNote = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inscriptionId);
            ps.setInt(2, typeNote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("valeur");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Implements the "best grade retained" rule.
     *
     * Compares the original exam note (type 1) with the resit note (type 2)
     * and returns whichever is higher.  If the resit note doesn't exist yet,
     * returns the original exam note.  Returns -1 if neither exists.
     *
     * This value is what should be used in the after-resit average calculation
     * (though InscriptionDAO.calculateAndSaveAverageAfterResit() does this
     * automatically — this method is exposed for display purposes in the UI).
     *
     * @param inscriptionId PK of the INSCRIPTION row
     * @return the best exam-type note, or -1 if unavailable
     */
    public double getFinalNote(int inscriptionId) {
        double exam      = getNoteValue(inscriptionId, 1);
        double rattrapage = getNoteValue(inscriptionId, 2);

        if (exam < 0) return -1;               // no exam note at all
        if (rattrapage < 0) return exam;        // no resit — keep exam
        return Math.max(exam, rattrapage);      // best retained
    }

    // ------------------------------------------------------------------ UPDATE

    /**
     * Updates the value of an existing note by its PK.
     * Validation (0–20) is enforced by the Note setter.
     *
     * @return true if a row was modified
     */
    public boolean updateNote(int idNote, double newValeur) {
        if (newValeur < 0 || newValeur > 20) return false;
        String sql = "UPDATE NOTE SET valeur = ? WHERE idNote = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newValeur);
            ps.setInt(2, idNote);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ DELETE

    /**
     * Removes a note by PK.
     * After deletion you should re-call InscriptionDAO.calculateAndSaveAverage()
     * to re-evaluate the enrollment status.
     *
     * @return true if a row was deleted
     */
    public boolean deleteNote(int idNote) {
        String sql = "DELETE FROM NOTE WHERE idNote = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idNote);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ HELPERS

    /**
     * Returns true if a note of the given type already exists for the enrollment.
     * Prevents inserting duplicate CC, Exam, or Rattrapage notes.
     */
    private boolean noteTypeExists(int inscriptionId, int typeNote) {
        String sql = "SELECT COUNT(*) FROM NOTE WHERE idInscription = ? AND typeNote = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inscriptionId);
            ps.setInt(2, typeNote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Maps the current ResultSet row to a Note object.
     * The {@code Session} boolean is derived from typeNote:
     *   type 2 → resit session, otherwise normal session.
     */
    private Note mapRow(ResultSet rs, Inscription enrolment) throws SQLException {
        int    typeNote = rs.getInt("typeNote");
        double valeur   = rs.getDouble("valeur");
        boolean isResit = (typeNote == 2);

        Note n = new Note(valeur, typeNote, isResit, enrolment);
        n.setIdNote(rs.getInt("idNote"));
        return n;
    }
}