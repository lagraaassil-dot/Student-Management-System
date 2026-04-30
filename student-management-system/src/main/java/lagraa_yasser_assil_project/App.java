package lagraa_yasser_assil_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lagraa_yasser_assil_project.models.Etudiant;
import lagraa_yasser_assil_project.utils.DBConnection;

public class App {
    public static void main(String[] args) {
        // 1. Create a dummy Etudiant object
        Etudiant testStudent = new Etudiant( "Zyad", "Assill", null, "test2@usthb.dz");


       // 1. Remove idEtudiant from the columns list and the VALUES clause
String query = "INSERT INTO ETUDIANT (nom, prenom, email) VALUES (?, ?, ?)";

try (Connection conn = DBConnection.getConnection();
     // 2. Use RETURN_GENERATED_KEYS to get the ID back from the DB
     PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

    if (conn != null) {
        // 3. Notice the indices start at 1 now because idEtudiant is gone
        pstmt.setString(1, testStudent.getNom());
        pstmt.setString(2, testStudent.getPrenom());
        pstmt.setString(3, testStudent.getEmail());

        int rowsInserted = pstmt.executeUpdate();

        // 4. Retrieve the auto-generated ID to update your Java object
        if (rowsInserted > 0) {
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    testStudent.setId(generatedKeys.getInt(1));
                    System.out.println("Student saved with ID: " + testStudent.getIdEtudiant());
                }
            }
        }
    }
} catch (SQLException e) {
    e.printStackTrace();
}
    }
}