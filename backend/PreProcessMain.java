package backend;

/**
 * Main program that runs the preprocessing of text
 * creates an instance of database manager and updatedpreprocessing
 * written by Ezzah and Khushi
 */

public class PreProcessMain {
    public static void main(String[] args) {
        try {
            // Create DatabaseManager instance
            DatabaseManager dbManager = new DatabaseManager();

            // Run a processing instance
            UpdatedPreProcessing processor = new UpdatedPreProcessing(dbManager);
            UpdatedPreProcessing.run();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}