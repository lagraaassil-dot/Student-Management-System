package lagraa_yasser_assil_project.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Single point of entry for database connections.
public class DBConnection {

    private static final String URL  = "jdbc:sqlserver://localhost;databaseName=GestionEtudiants;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "YasserAssil_admin";
    private static final String PASS = "INFO2A";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(URL, USER, PASS);
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
