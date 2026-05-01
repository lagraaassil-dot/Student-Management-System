package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.models.ModuleEtude;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for enrollments; handles average calculation and validation.
public class InscriptionDAO {

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final ModuleDAO   moduleDAO   = new ModuleDAO();

    

    
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

    
    public boolean enrollStudent(Etudiant etudiant, ModuleEtude module) {
        return enrollStudent(etudiant.getIdEtudiant(), module.getIdModule());
    }

    

    
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

    

    
 public boolean deleteEnrolment(int inscriptionId) {
    String deleteNotes = "DELETE FROM NOTE WHERE idInscription = ?";
    String deleteInsc  = "DELETE FROM INSCRIPTION WHERE idInscription = ?";

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(deleteNotes)) {
                ps.setInt(1, inscriptionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteInsc)) {
                ps.setInt(1, inscriptionId);
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

    

    
    public double calculateAndSaveAverage(int inscriptionId) {
        
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

            if (cc < 0 || exam < 0) return -1; 

            double average = (cc * 40 + exam * 60) / 100.0;
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

    
    private Inscription mapRow(ResultSet rs, Etudiant etudiant) throws SQLException {
        int moduleId = rs.getInt("idModule");
        ModuleEtude module = moduleDAO.getModuleById(moduleId);

        
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
