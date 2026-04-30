package lagraa_yasser_assil_project.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:sqlserver://localhost;databaseName=GestionEtudiants;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "YasserAssil_admin"; 
    private static final String PASS = "INFO2A"; 

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load the driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connexion réussie à la base de données !");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver JDBC non trouvé.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Impossible de se connecter à SQL Server.");
            e.printStackTrace();
        }
        return conn;
    }
}