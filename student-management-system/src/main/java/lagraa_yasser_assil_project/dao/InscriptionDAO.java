package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InscriptionDAO — the "engine" of the project.
 *
 * Handles enrollment, average computation, validation, and resit queries.
 *
 * Table assumed:
 *   INSCRIPTION(idInscription INT PK IDENTITY,
 *               idEtudiant    INT FK → ETUDIANT,
 *               idModule      INT FK → MODULE,
 *               dateInscription DATE,
 *               isValidated   BIT NULL)   -- NULL = not yet evaluated
 *
 * Business rule (from project spec):
 *   Average = (CC × 40 + Exam × 60) / 100
 *   ≥ 10  → validated (isValidated = 1)
 *   < 10  → resit required (isValidated = 0)
 */
public class InscriptionDAO {

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final ModuleDAO   moduleDAO   = new ModuleDAO();

    // ------------------------------------------------------------------ CREATE

    /**
     * Enrolls a student in a module.
     * Enforces the "only one enrollment per module" rule — returns false
     * if the student is already enrolled.
     *
     * @return true on success, false if already enrolled or on SQL error
     */
    public boolean enrollStudent(int studentId, int moduleId) {
        if (isAlreadyEnrolled(studentId, moduleId)) return false;

        String sql = "INSERT INTO INSCRIPTION (idEtudiant, idModule, dateInscription, isValidated) "
                   + "VALUES (?, ?, GETDATE(), NULL)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, moduleId);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Overload that accepts model objects directly — handy from the Swing layer.
     */
    public boolean enrollStudent(Etudiant etudiant, ModuleEtude module) {
        return enrollStudent(etudiant.getIdEtudiant(), module.getIdModule());
    }

    // ------------------------------------------------------------------ READ

    /**
     * Returns all enrollments for a given student (one row per module).
     */
    public List<Inscription> getInscriptionsByStudent(int studentId) {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT idInscription, idEtudiant, idModule, dateInscription, isValidated "
                   + "FROM INSCRIPTION WHERE idEtudiant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                Etudiant etudiant = etudiantDAO.getEtudiantById(studentId);
                while (rs.next()) {
                    list.add(mapRow(rs, etudiant));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Returns a single enrollment by PK, or null if not found.
     */
    public Inscription getInscriptionById (int inscriptionId) {
        String sql = "SELECT idInscription, idEtudiant, idModule, dateInscription, isValidated "
                   + "FROM INSCRIPTION WHERE idInscription = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Etudiant etudiant = etudiantDAO.getEtudiantById(rs.getInt("idEtudiant"));
                    return mapRow(rs, etudiant);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all students who failed at least one module (isValidated = 0).
     * Used to populate the "Étudiants en rattrapage" panel.
     */
    public List<Etudiant> getStudentsInResit() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT DISTINCT e.idEtudiant, e.nom, e.prenom, e.dateNaissance, e.email, e.isDiplome "
                   + "FROM ETUDIANT e "
                   + "JOIN INSCRIPTION i ON e.idEtudiant = i.idEtudiant "
                   + "WHERE i.isValidated = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Etudiant et = new Etudiant(
                    rs.getString("nom"), rs.getString("prenom"),
                    rs.getDate("dateNaissance"), rs.getString("email"));
                et.setId(rs.getInt("idEtudiant"));
                et.setDiplome(rs.getBoolean("isDiplome"));
                list.add(et);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------ DELETE

    /**
     * Removes an enrollment (and consequently all notes cascade-deleted at DB).
     */
    public boolean deleteEnrolment(int inscriptionId) {
        String sql = "DELETE FROM INSCRIPTION WHERE idInscription = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, inscriptionId);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ AVERAGE & VALIDATION

    /**
     * Computes the student's average for the given enrollment using the formula:
     *   Average = (CC × 40 + Exam × 60) / 100
     *
     * Then updates isValidated accordingly and returns the computed average.
     * Returns -1 if CC or Exam note is missing (cannot compute yet).
     *
     * After calling this, consider calling EtudiantDAO.checkAndSetDiploma()
     * to see if the student has now completed all modules.
     *
     * @param inscriptionId PK of the INSCRIPTION row
     * @return the computed average, or -1 if data is incomplete
     */
    public double calculateAndSaveAverage(int inscriptionId) {
        // Fetch CC (type 0) and Exam (type 1) notes for this inscription
        String fetchSql = "SELECT typeNote, valeur FROM NOTE "
                        + "WHERE idInscription = ? AND typeNote IN (0, 1)";

        double cc   = -1;
        double exam = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(fetchSql)) {

            ps.setInt(1, inscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int type = rs.getInt("typeNote");
                    if (type == 0) cc   = rs.getDouble("valeur");
                    if (type == 1) exam = rs.getDouble("valeur");
                }
            }

            if (cc < 0 || exam < 0) return -1; // one or both notes missing

            double average = (cc * 40 + exam * 60) / 100.0;
            boolean validated = average >= 10;

            // Persist the validation result
            String updateSql = "UPDATE INSCRIPTION SET isValidated = ? WHERE idInscription = ?";
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setBoolean(1, validated);
                upd.setInt(2, inscriptionId);
                upd.executeUpdate();
            }

            return average;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     * Computes the average after the resit session.
     *
     * The final exam note = max(original exam, resit note).
     * Uses the same formula: Average = (CC × 40 + BestExam × 60) / 100
     *
     * Updates isValidated accordingly and returns the new average.
     * Returns -1 if the required notes are not present.
     */
    public double calculateAndSaveAverageAfterResit(int inscriptionId) {
        String fetchSql = "SELECT typeNote, valeur FROM NOTE WHERE idInscription = ?";

        double cc        = -1;
        double exam      = -1;
        double rattrapage = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(fetchSql)) {

            ps.setInt(1, inscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int type = rs.getInt("typeNote");
                    if (type == 0) cc          = rs.getDouble("valeur");
                    if (type == 1) exam        = rs.getDouble("valeur");
                    if (type == 2) rattrapage  = rs.getDouble("valeur");
                }
            }

            if (cc < 0 || exam < 0) return -1;

            // Best exam note retained
            double bestExam = (rattrapage >= 0) ? Math.max(exam, rattrapage) : exam;
            double average  = (cc * 40 + bestExam * 60) / 100.0;
            boolean validated = average >= 10;

            String updateSql = "UPDATE INSCRIPTION SET isValidated = ? WHERE idInscription = ?";
            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                upd.setBoolean(1, validated);
                upd.setInt(2, inscriptionId);
                upd.executeUpdate();
            }

            return average;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    // ------------------------------------------------------------------ HELPERS

    /** Checks if a student is already enrolled in a module. */
    public boolean isAlreadyEnrolled(int studentId, int moduleId) {
        String sql = "SELECT COUNT(*) FROM INSCRIPTION WHERE idEtudiant = ? AND idModule = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, moduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a ResultSet row into an Enrolments object.
     * The Etudiant is passed in to avoid re-querying it on every row
     * when iterating through a student's enrollments.
     */
    private Inscription mapRow(ResultSet rs, Etudiant etudiant) throws SQLException {
        int moduleId = rs.getInt("idModule");
        ModuleEtude module = moduleDAO.getModuleById(moduleId);

        // Determine which constructor to call based on isValidated nullability
        Object validatedObj = rs.getObject("isValidated");
        Inscription inscription;

        if (validatedObj == null) {
            inscription = new Inscription(etudiant, module);
        } else {
            inscription = new Inscription(etudiant, module, rs.getBoolean("isValidated"));
        }

        inscription.setIdInscription(rs.getInt("idInscription"));
        inscription.setDateInscription(rs.getDate("dateInscription"));
        return inscription;
    }
}
