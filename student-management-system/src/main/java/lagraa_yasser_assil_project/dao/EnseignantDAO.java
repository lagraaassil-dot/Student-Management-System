package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.Enums.Specialite;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// CRUD for the ENSEIGNANT table.
// Specialite is persisted as its enum name() so it round-trips cleanly.
public class EnseignantDAO {

    public boolean addEnseignant(Enseignant t) {
        String sql = "INSERT INTO ENSEIGNANT (nom, specialite) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getNom());
            ps.setString(2, t.getSpecialite().name());

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setIdEnseignant(keys.getInt(1));
            }
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Enseignant> getAllEnseignants() {
        List<Enseignant> list = new ArrayList<>();
        String sql = "SELECT idEnseignant, nom, specialite FROM ENSEIGNANT";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

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

    // Modules referencing this teacher should handle the FK via ON DELETE SET NULL.
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

    private Enseignant mapRow(ResultSet rs) throws SQLException {
        String specStr = rs.getString("specialite");
        Specialite spec = null;
        if (specStr != null) {
            try {
                spec = Specialite.valueOf(specStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown specialite in DB: " + specStr);
            }
        }
        Enseignant e = new Enseignant(rs.getString("nom"), spec);
        e.setIdEnseignant(rs.getInt("idEnseignant"));
        return e;
    }
}
