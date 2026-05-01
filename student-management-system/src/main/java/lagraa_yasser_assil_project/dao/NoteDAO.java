package lagraa_yasser_assil_project.dao;

import lagraa_yasser_assil_project.models.Inscription;
import lagraa_yasser_assil_project.models.Note;
import lagraa_yasser_assil_project.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for grade (NOTE) records.
public class NoteDAO {

    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();

    

    
    public boolean addNote(Note n) {
        int insId    = n.getEnrolment().getIdInscription();
        int typeNote = n.getTypeNote();

        if (noteTypeExists(insId, typeNote)) return false; 

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

    
  public List<Note> getNotesByStudent(int studentId) {
    List<Note> list = new ArrayList<>();
    String sql = "SELECT n.idNote, n.valeur, n.typeNote, n.idInscription "
               + "FROM NOTE n "
               + "JOIN INSCRIPTION i ON n.idInscription = i.idInscription "
               + "WHERE i.idEtudiant = ?";

    
    List<int[]> rows = new ArrayList<>(); 
    List<Double> valeurs = new ArrayList<>();
    
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, studentId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new int[]{ rs.getInt("idNote"), rs.getInt("typeNote"), rs.getInt("idInscription") });
                valeurs.add(rs.getDouble("valeur"));
            }
        }
    } catch (SQLException ex) { ex.printStackTrace(); return list; }

    
    for (int i = 0; i < rows.size(); i++) {
        int[] r = rows.get(i);
        Inscription inscription = inscriptionDAO.getInscriptionById(r[2]);
        int typeNote = r[1];
        Note n = new Note(valeurs.get(i), typeNote, typeNote == 2, inscription);
        n.setIdNote(r[0]);
        list.add(n);
    }
    return list;
}

    
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

    
    public double getFinalNote(int inscriptionId) {
        double exam      = getNoteValue(inscriptionId, 1);
        double rattrapage = getNoteValue(inscriptionId, 2);

        if (exam < 0) return -1;               
        if (rattrapage < 0) return exam;        
        return Math.max(exam, rattrapage);      
    }

    

    
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

    
    private Note mapRow(ResultSet rs, Inscription enrolment) throws SQLException {
        int    typeNote = rs.getInt("typeNote");
        double valeur   = rs.getDouble("valeur");
        boolean isResit = (typeNote == 2);

        Note n = new Note(valeur, typeNote, isResit, enrolment);
        n.setIdNote(rs.getInt("idNote"));
        return n;
    }
}