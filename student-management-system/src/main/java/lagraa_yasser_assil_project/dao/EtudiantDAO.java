package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// CRUD + diploma logic for the ETUDIANT table.
public class EtudiantDAO {

    public boolean addEtudiant(Etudiant e) {
        String sql = "INSERT INTO ETUDIANT (nom, prenom, dateNaissance, email, isDiplome) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setDate(3, new java.sql.Date(e.getDateNaissance().getTime()));
            ps.setString(4, e.getEmail());

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Etudiant> getAllEtudiants() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome FROM ETUDIANT";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Etudiant getEtudiantById(int id) {
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome FROM ETUDIANT WHERE idEtudiant = ?";
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

    public List<Etudiant> getGraduatedStudents() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT idEtudiant, nom, prenom, dateNaissance, email, isDiplome FROM ETUDIANT WHERE isDiplome = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

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

    // Deletes the student and all their inscriptions/grades in one transaction.
    public boolean deleteEtudiant(int id) {
        String deleteNotes = "DELETE FROM NOTE WHERE idInscription IN (SELECT idInscription FROM INSCRIPTION WHERE idEtudiant = ?)";
        String deleteInsc  = "DELETE FROM INSCRIPTION WHERE idEtudiant = ?";
        String deleteEt    = "DELETE FROM ETUDIANT WHERE idEtudiant = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteNotes)) { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement ps = conn.prepareStatement(deleteInsc))  { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement ps = conn.prepareStatement(deleteEt))    { ps.setInt(1, id); ps.executeUpdate(); }
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

    // Grants the diploma if every enrollment for this student is validated.
    // Call this after any grade change.
    public boolean checkAndSetDiploma(Etudiant etudiant) {
        int studentId = etudiant.getIdEtudiant();
        String countSql  = "SELECT COUNT(*) FROM INSCRIPTION WHERE idEtudiant = ?";
        String checkSql  = "SELECT COUNT(*) FROM INSCRIPTION WHERE idEtudiant = ? AND (isValidated IS NULL OR isValidated = 0)";
        String updateSql = "UPDATE ETUDIANT SET isDiplome = 1 WHERE idEtudiant = ?";

        try (Connection conn = DBConnection.getConnection()) {
            int totalEnrollments;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); totalEnrollments = rs.getInt(1); }
            }
            if (totalEnrollments == 0) return false;

            int unvalidated;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); unvalidated = rs.getInt(1); }
            }
            if (unvalidated > 0) return false;

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

    private Etudiant mapRow(ResultSet rs) throws SQLException {
        Etudiant e = new Etudiant(
            rs.getString("nom"), rs.getString("prenom"),
            rs.getDate("dateNaissance"), rs.getString("email")
        );
        e.setId(rs.getInt("idEtudiant"));
        e.setDiplome(rs.getBoolean("isDiplome"));
        return e;
    }
}
