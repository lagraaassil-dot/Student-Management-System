package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Enseignant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for the MODULE_ETUDE table.
public class ModuleDAO {

    
    private final EnseignantDAO enseignantDAO = new EnseignantDAO();

    

    
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

    

 
public boolean deleteModule(int id) {
    String deleteNotes = "DELETE FROM NOTE WHERE idInscription IN "
                       + "(SELECT idInscription FROM INSCRIPTION WHERE idModule = ?)";
    String deleteInsc  = "DELETE FROM INSCRIPTION WHERE idModule = ?";
    String deleteMod   = "DELETE FROM MODULE WHERE idModule = ?";

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(deleteNotes)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteInsc)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteMod)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            ex.printStackTrace();
            return false;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

    

    
    private void setNullableTeacher(PreparedStatement ps, int index, Enseignant enseignant)
            throws SQLException {
        if (enseignant != null && enseignant.getIdEnseignant() != null) {
            ps.setInt(index, enseignant.getIdEnseignant());
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    
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