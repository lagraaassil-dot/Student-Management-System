package lagraa_yasser_assil_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.utils.DBConnection;

public class App {
    public static void main(String[] args) {
        // 1. Create a dummy Etudiant object
        Etudiant testStudent = new Etudiant(4, "Zyad", "Assil", null, "test2@usthb.dz");

        // 2. Try to connect and insert into the DB
        String query = "INSERT INTO ETUDIANT (idEtudiant, nom, prenom, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn != null) {
                pstmt.setInt(1, testStudent.getIdEtudiant());
                pstmt.setString(2, testStudent.getNom());
                pstmt.setString(3, testStudent.getPrenom());
                pstmt.setString(4, testStudent.getEmail());

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("✅ Succès ! L'étudiant a été ajouté à la base de données.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}