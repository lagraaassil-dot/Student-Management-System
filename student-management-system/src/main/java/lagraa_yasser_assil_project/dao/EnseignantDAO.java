package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.Enums.Specialite;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EnseignantDAO — CRUD for the ENSEIGNANT table.
 *
 * Table assumed:
 *   ENSEIGNANT(idEnseignant INT PK IDENTITY, nom VARCHAR, specialite VARCHAR)
 *
 * The Specialite enum is stored as its name() string (e.g. "GL", "IA").
 * toString() is the display label — we persist name() so we can safely
 * round-trip back to the enum with Specialite.valueOf().
 */
public class EnseignantDAO {

    // ------------------------------------------------------------------ CREATE

    /**
     * Inserts a teacher and writes the generated PK back into the object.
     * @return true on success
     */
    public boolean addEnseignant(Enseignant t) {
        String sql = "INSERT INTO ENSEIGNANT (nom, specialite) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getNom());
            ps.setString(2, t.getSpecialite().name()); // store enum constant name

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setIdEnseignant(keys.getInt(1));
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
     * Returns all teachers — used to populate a JComboBox when assigning
     * a teacher to a module.
     */
    public List<Enseignant> getAllEnseignants() {
        List<Enseignant> list = new ArrayList<>();
        String sql = "SELECT idEnseignant, nom, specialite FROM ENSEIGNANT";
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
     * Returns a single teacher by PK, or null if not found.
     */
    public Enseignant getEnseignantById(int id) {
        String sql = "SELECT idEnseignant, nom, specialite FROM ENSEIGNANT WHERE idEnseignant = ?";
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

    // ------------------------------------------------------------------ UPDATE

    /**
     * Updates nom and specialite for an existing teacher.
     * @return true if a row was modified
     */
    public boolean updateEnseignant(Enseignant t) {
        String sql = "UPDATE ENSEIGNANT SET nom = ?, specialite = ? WHERE idEnseignant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getNom());
            ps.setString(2, t.getSpecialite().name());
            ps.setInt(3, t.getIdEnseignant());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ DELETE

    /**
     * Removes a teacher by PK.
     * Modules that reference this teacher will have their FK set to NULL
     * if the DB is configured with ON DELETE SET NULL; otherwise handle
     * the cascade at DB level before calling this.
     * @return true if a row was deleted
     */
    public boolean deleteEnseignant(int id) {
        String sql = "DELETE FROM ENSEIGNANT WHERE idEnseignant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ HELPER

private Enseignant mapRow(ResultSet rs) throws SQLException {
    String specStr = rs.getString("specialite");
    Specialite spec = null;
    if (specStr != null) {
        try {
            spec = Specialite.valueOf(specStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown specialite value in DB: " + specStr);
            // spec stays null — handle gracefully downstream
        }
    }
    Enseignant e = new Enseignant(rs.getString("nom"), spec);
    e.setIdEnseignant(rs.getInt("idEnseignant"));
    return e;
}
}
