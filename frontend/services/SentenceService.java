// define package for this class
package frontend.services;

// import classes from other packages
import backend.BigramProcessor;
import java.sql.SQLException;
import java.util.List;

/**
 * SentenceService class serves as the main link between the frontend and backend.
 * It utilizes BigramProcessor to generate sentences using the user's inputted word and DatabaseManager to
 * access the parsed data. The class is implemented as a Singleton — only one instance exists throughout the
 * application.
 *
 * Written by khushi
 */
public class SentenceService {
    private static BigramProcessor processor;
    private static SentenceService instance;

    // Private constructor for singleton pattern
    private SentenceService(BigramProcessor processor) {
        this.processor = processor;
    }

    /**
     * Initialize the service with database connection
     * Call this once when the application starts
     * Written by Khushi Dubey
     */
    public static void initialize() throws SQLException {
        if (instance == null) {
            // BigramProcessor handles its own database connections, no instance of dbmanager
            processor = new BigramProcessor();
            instance = new SentenceService(processor);
            System.out.println("SentenceService initialized successfully!");
        }
    }

    /**
     * Get the singleton instance
     * Written by Khushi Dubey
     */
    public static SentenceService getInstance() {
        // see if instance is null
        if (instance == null) {
            throw new IllegalStateException("SentenceService not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Generate a sentence starting with the given prefix word
     * @param prefix The first word of the sentence
     * @return Generated sentence as a String
     * Written by Khushi Dubey
     */
    public String generateSentence(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "Error: Please enter a starting word.";
        }

        try {
            // Clean up the prefix (trim whitespace, convert to proper format)
            String cleanPrefix = prefix.trim();

            // Generate sentence with max 10 words, using smoothing
            String result = BigramProcessor.generateSentenceTopOne(cleanPrefix, 10, true);

            // Check if generation was successful
            if (result == null || result.trim().isEmpty()) {
                return "Error: Could not generate sentence. Word might not be in database.";
            }

            return result.trim();

        } catch (Exception e) {
            System.err.println("Error generating sentence: " + e.getMessage());
            e.printStackTrace();
            return "Error: Failed to generate sentence. Please try another word.";
        }
    }

    /**
     * Get a sorted list of next word suggestions for the current sentence.
     * Uses the same bigram logic that BigramProcessor uses to generate sentences.
     * Written by Rida Basit
     */
    public List<String> getNextWordSuggestions(String currentSentence) {
        // If the sentence is completely null, return an empty list
        if (currentSentence == null) {
            return List.of();
        }
        // Remove extra spaces at the start and end of the sentence
        String clean = currentSentence.trim();
        // If the cleaned sentence is now empty, return an empty list
        if (clean.isEmpty()) {
            return List.of();
        }
        try {
            // Ask the BigramProcessor for suggestions using Laplace smoothing (true)
            // This will return a list of next-word options based on the last word in the sentence
            return BigramProcessor.getNextWordSuggestions(clean, true);
        } catch (Exception e) {
            // If anything goes wrong, print the error message to the console
            System.err.println("Error getting next-word suggestions: " + e.getMessage());
            e.printStackTrace();
            // And return an empty list so the UI can handle "no suggestions"
            return List.of();
        }
    }


    /**
     * Clean up database connections when application closes
     * Written by Khushi Dubey
     */
    public static void shutdown() {
        System.out.println("SentenceService shut down.");
        instance = null;
    }
}
