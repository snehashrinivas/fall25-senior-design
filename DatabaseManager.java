import java.sql.Connection;
import java.sql.*;

public class DatabaseManager {
    private  Connection conn;
    private final String url;
    private final String username;
    private final String password;

    public DatabaseManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(strConnect, "root", "SQL1");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC Driver not found: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new SQLException("Database connection error: " + ex.getMessage());
        }
    }

    // function to check if db is connected

    // function to stop connection

    // need to put get id and countWords in here

}
