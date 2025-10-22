import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Create database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/SentenceBuilder",
                    "root",
                    "your_new_password"  // Replace with your actual password
            );

            // 2. Create DatabaseManager
            DatabaseManager dbManager = new DatabaseManager(conn);

            // 3. Check connection
            if (dbManager.isConnected()) {
                System.out.println("Database connected successfully!");

                // 4. Run TextPreProcessor
                UpdatedPreProcessing processor = new UpdatedPreProcessing(conn);
                UpdatedPreProcessing.run();
            } else {
                System.err.println("Failed to connect to database");
            }

            // 5. Disconnect when done
            dbManager.disconnect();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}