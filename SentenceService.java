/*package backend;
package frontend;

import java.sql.Connection;

public class SentenceService {
    private final backend.backend.BigramProcessor processor;
    private final backend.backend.DatabaseManager dbManager;

    public SentenceService() {
        // Setup backend dependencies internally
        this.dbManager = backend.backend.DatabaseManager.getInstance();  // assuming singleton pattern
        Connection conn = dbManager.getConnection();
        this.processor = new backend.backend.BigramProcessor(conn, dbManager);
    }

    public String generateSentence(String prefix) {
        return backend.backend.BigramProcessor.generateSentence(prefix, 10, true);
    }
}*/