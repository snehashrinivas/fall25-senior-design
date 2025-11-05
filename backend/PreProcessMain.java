package backend;

import backend.DatabaseManager;

public class PreProcessMain {
    public static void main(String[] args) {
        try {
            // 1. Create backend.backend.DatabaseManager
            //  backend.backend.DatabaseManager dbManager = new backend.backend.DatabaseManager(conn);
            DatabaseManager dbManager = DatabaseManager.getInstance();
           // Connection conn = dbManager.getConnection();


            // 3. Check connection
            if (dbManager.isConnected()) {
                System.out.println("Database connected successfully!");

                // 4. Run TextPreProcessor
                UpdatedPreProcessing processor = new UpdatedPreProcessing(dbManager);//conn, dbManager);
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