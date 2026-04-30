package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ModuleDAO — CRUD for the MODULE table.
 *
 * Table assumed:
 *   MODULE(idModule INT PK IDENTITY, nomModule VARCHAR, coefficient INT,
 *          volumeHoraire INT, idEnseignant INT NULL FK → ENSEIGNANT)
 */
public class ModuleDAO {

    // We delegate teacher reconstruction to EnseignantDAO to stay DRY.
    private final EnseignantDAO enseignantDAO = new EnseignantDAO();

    // ------------------------------------------------------------------ CREATE

    /**
     * Inserts a module and writes the generated PK back into the object.
     * If the module has no teacher yet, idEnseignant is stored as NULL.
     * @return true on success
     */
    public boolean addModule(ModuleEtude m) {
        String sql = "INSERT INTO MODULE (nom, coefficient, volumeHoraire, idEnseignant) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNomModule());
            ps.setInt(2, m.getCoefficient());
            ps.setInt(3, m.getVolumeHoraire());
            setNullableTeacher(ps, 4, m.getEnseignant());

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setIdModule(keys.getInt(1));
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
     * Returns all modules with their teacher (if assigned).
     * Useful for the module list table and JComboBox in the enrollment panel.
     */
    public List<ModuleEtude> getAllModules() {
        List<ModuleEtude> list = new ArrayList<>();
        String sql = "SELECT idModule, nom, coefficient, volumeHoraire, idEnseignant FROM MODULE";
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
     * Returns a single module by PK, or null if not found.
     */
    public ModuleEtude getModuleById(int id) {
        String sql = "SELECT idModule, nom, coefficient, volumeHoraire, idEnseignant "
                   + "FROM MODULE WHERE idModule = ?";
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
     * Returns all modules taught by a specific teacher.
     * Useful in an "Enseignant detail" view.
     */
    public List<ModuleEtude> getModulesByTeacher(int teacherId) {
        List<ModuleEtude> list = new ArrayList<>();
        String sql = "SELECT idModule, nom, coefficient, volumeHoraire, idEnseignant "
                   + "FROM MODULE WHERE idEnseignant = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE

    /**
     * Updates all fields of an existing module, including teacher assignment.
     * Pass a module with enseignant = null to clear the teacher.
     * @return true if a row was modified
     */
    public boolean updateModule(ModuleEtude m) {
        String sql = "UPDATE MODULE SET nom = ?, coefficient = ?, volumeHoraire = ?, "
                   + "idEnseignant = ? WHERE idModule = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNomModule());
            ps.setInt(2, m.getCoefficient());
            ps.setInt(3, m.getVolumeHoraire());
            setNullableTeacher(ps, 4, m.getEnseignant());
            ps.setInt(5, m.getIdModule());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ DELETE

    /**
     * Removes a module by PK.
     * Ensure cascading deletes or manual cleanup of INSCRIPTION / NOTE rows
     * at the DB level before calling this.
     * @return true if a row was deleted
     */
    public boolean deleteModule(int id) {
        String sql = "DELETE FROM MODULE WHERE idModule = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------ HELPERS

    /**
     * Sets parameter at {@code index} to the teacher's PK, or SQL NULL
     * if no teacher is assigned.
     */
    private void setNullableTeacher(PreparedStatement ps, int index, Enseignant enseignant)
            throws SQLException {
        if (enseignant != null && enseignant.getIdEnseignant() != null) {
            ps.setInt(index, enseignant.getIdEnseignant());
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    /**
     * Maps the current ResultSet row to a Module object,
     * lazily loading the Enseignant by FK if present.
     */
    private ModuleEtude mapRow(ResultSet rs) throws SQLException {
        int idMod       = rs.getInt("idModule");
        String nom      = rs.getString("nom");
        int coeff       = rs.getInt("coefficient");
        int vh          = rs.getInt("volumeHoraire");
        int idEns       = rs.getInt("idEnseignant");
        boolean hasEns  = !rs.wasNull();

        Enseignant enseignant = hasEns ? enseignantDAO.getEnseignantById(idEns) : null;

        ModuleEtude m = (enseignant != null)
                ? new ModuleEtude(nom, coeff, vh, enseignant)
                : new ModuleEtude(nom, coeff, vh);
        m.setIdModule(idMod);
        return m;
    }
}