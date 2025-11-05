/*package frontend.services;

import backend.BigramProcessor;
import backend.DatabaseManager;
import java.sql.SQLException;
import java.util.ArrayList;

import java.sql.Connection;

public class SentenceService {
   private BigramProcessor processor;
    private DatabaseManager dbManager;

    public SentenceService(DatabaseManager dbManager) throws SQLException {
        this.dbManager = dbManager;
        this.processor = new BigramProcessor(dbManager);
    }

    public String generateSentence(String prefix) {
        return BigramProcessor.generateSentence(prefix, 10, true);
    }
}*/