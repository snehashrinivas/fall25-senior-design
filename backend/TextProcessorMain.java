package backend;

/**
 *  Main method that creates an instance of DatabaseManager and BigramProcessor to run
 *  Written by Ezzah, Khushi, Andersen
 **/

import backend.BigramProcessor;
import backend.DatabaseManager;

public class TextProcessorMain {
    public static void main(String[] args) {
        try {
            // create instance of Database Manager
            DatabaseManager dbManager = new DatabaseManager();

            System.out.println("Database connected successfully!");

            // create instance and fully load hashmaps from DB to avoid null pointer errors
            BigramProcessor processor = new BigramProcessor();

            // Run the instance
            BigramProcessor.run();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}