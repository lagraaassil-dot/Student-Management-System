package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EtudiantDAO — CRUD + diploma logic for the ETUDIANT table.
 *
 * Table assumed:
 *   ETUDIANT(idEtudiant INT PK IDENTITY, nom VARCHAR, prenom VARCHAR,
 *            dateNaissance DATE, email VARCHAR, isDiplome BIT DEFAULT 0)
 */
public class EtudiantDAO {

    // ------------------------------------------------------------------ CREATE

    /**
     * Inserts a new student and writes the generated PK back into the object.
     * @return true on success
     */
    public boolean addEtudiant(Etudiant e) {
        String sql = "INSERT INTO ETUDIANT (nom, prenom, dateNaissance, email, isDiplome) "
                   + "VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setDate(3, new java.sql.Date(e.getDateNaissance().getTime()));
            ps.setString(4, e.getEmail());

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));   // push generated ID into the object
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
     * Returns every student in the database — suitable for populating the main Swing table.
     */
    public List<Etudiant> getAllEtudiants() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome FROM ETUDIANT";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Returns a single student by PK, or null if not found.
     */
    public Etudiant getEtudiantById(int id) {
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome "
                   + "FROM ETUDIANT WHERE idEtudiant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all students who have graduated (isDiplome = 1).
     * Use this to populate the "Étudiants diplômés" panel.
     */
    public List<Etudiant> getGraduatedStudents() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome "
                   + "FROM ETUDIANT WHERE isDiplome = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE

    /**
     * Updates nom, prenom, and email for an existing student.
     * @return true if a row was actually modified
     */
    public boolean updateEtudiant(Etudiant e) {
        String sql = "UPDATE ETUDIANT SET nom = ?, prenom = ?, email = ? WHERE idEtudiant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.setInt(4, e.getIdEtudiant());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ DELETE

    /**
     * Removes a student by PK.
     * Note: foreign-key constraints on INSCRIPTION / NOTE must be handled
     * (cascade delete or manual cleanup) at the DB level.
     * @return true if a row was deleted
     */
    public boolean deleteEtudiant(int id) {
        String sql = "DELETE FROM ETUDIANT WHERE idEtudiant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ DIPLOMA

    /**
     * Checks whether every enrollment for this student is validated.
     * If so, flips isDiplome = 1 in the DB and updates the in-memory object.
     *
     * Call this after every grade entry or average calculation.
     *
     * @param etudiant the student to check (must have a valid idEtudiant)
     * @return true if the student just graduated (or was already graduated)
     */
    public boolean checkAndSetDiploma(Etudiant etudiant) {
        int studentId = etudiant.getIdEtudiant();

        // A student with no enrollments cannot graduate
        String countSql  = "SELECT COUNT(*) FROM INSCRIPTION WHERE idEtudiant = ?";
        String checkSql  = "SELECT COUNT(*) FROM INSCRIPTION "
                         + "WHERE idEtudiant = ? AND (isValidated IS NULL OR isValidated = 0)";
        String updateSql = "UPDATE ETUDIANT SET isDiplome = 1 WHERE idEtudiant = ?";

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Make sure the student has at least one enrollment
            int totalEnrollments;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalEnrollments = rs.getInt(1);
                }
            }
            if (totalEnrollments == 0) return false;

            // 2. Count unvalidated enrollments
            int unvalidated;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    unvalidated = rs.getInt(1);
                }
            }

            if (unvalidated > 0) return false; // still has failing modules

            // 3. All validated — grant diploma
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, studentId);
                ps.executeUpdate();
            }
            etudiant.setDiplome(true);
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ HELPER

    /** Maps the current ResultSet row to an Etudiant object. */
    private Etudiant mapRow(ResultSet rs) throws SQLException {
        Etudiant e = new Etudiant(
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getDate("dateNaissance"),
            rs.getString("email")
        );
        e.setId(rs.getInt("idEtudiant"));
        e.setDiplome(rs.getBoolean("isDiplome"));
        return e;
    }
}
