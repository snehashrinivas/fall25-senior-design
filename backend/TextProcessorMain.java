package backend;

import backend.BigramProcessor;
import backend.DatabaseManager;

public class TextProcessorMain {
    public static void main(String[] args) {
        try {
            // Create backend.backend.DatabaseManager
            //backend.backend.DatabaseManager dbManager = new backend.backend.DatabaseManager(conn);
            //DatabaseManager dbManager = DatabaseManager.getInstance();
           // Connection conn = dbManager.getConnection();
            DatabaseManager dbManager = new DatabaseManager();

            // 3. Check connection
           // if (dbManager.isConnected()) {
                System.out.println("Database connected successfully!");

                // 4. Run TextPreProcessor
                BigramProcessor processor = new BigramProcessor(dbManager); //conn, dbManager);
                BigramProcessor.run();
            //} else {
              //  System.err.println("Failed to connect to database");
            //}

            // 5. Disconnect when done
          //  dbManager.disconnect();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}