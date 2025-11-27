package backend;

/**
 *  Main method that creates an instance of DatabaseManager and BigramProcessor and runs it
 *
 *  Written by Ezzah, Khushi, Andersen
 */

import backend.BigramProcessor;
import backend.DatabaseManager;

public class TextProcessorMain {
    public static void main(String[] args) {
        try {
            // create instance of Database Manager
            DatabaseManager dbManager = new DatabaseManager();

            System.out.println("Database connected successfully!");

            // Run TextPreProcessor
            BigramProcessor.run();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}