package backend;
/**
 * This class manages database operations for the Sentence Builder
 * It handles inserting words and their relationships into a MySQL database to track:
 *   - Word frequencies (how often each word appears)
 *   - Starting word occurrences (how often a word begins a sentence)
 *   - Ending word occurrences (how often a word ends a sentence)
 *   - Word relationships (which words commonly follow other words)
 *
 * The class uses prepared statements for SQL queries and manages the db connection through
 * connect/disconnect methods.
 * written by Ezzah, Khushi, Sneha, and Andersen
 */

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {
    // Database connection details --> change credentials here
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SentenceBuilder";
    // "jdbc:mysql://localhost:3306/sentencebuilderdb" khushi's url
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_new_password";
    //"" Khushi's password
    // "password" Sneha's password

    /**
     * Default constructor
     * No initialization needed since connections are created per-method
     */
    public DatabaseManager() { }

    /**
     * Helper method to create a new database connection
     * @return a new Connection object
     * @throws SQLException if connection fails
     *
     * Written by Ezzah
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Create and return a new connection to the database
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @param word String - word whose ID should be retrieved
     * @throws SQLException if the word is not found or database access fails
     * Written by Ezzah Qureshi
     */
    public static int getWordId(String word) {
        // try to get db connection
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT word_id FROM Words WHERE word = ?;")) {

            // Set the word parameter in the prepared statement and execute query
            stmt.setString(1, word);
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    // Return the word_id from the result
                    return rs.getInt("word_id");
                }
            }
            System.err.println("Word not found: " + word);
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * Inserts file metadata into the Files table using a Document object
     * @param document the Document object containing file metadata
     * @return the generated file_id
     * @throws SQLException if database access fails
     *
     * Written by Ezzah and Khushi
     */
    public int insertFileMetadata(Document document) throws SQLException {
        // define query to insert file metadata into Files db
        // use CURRENT_TIMESTAMP to record when the file was inserted
        String insertFileSQL = """
                INSERT INTO Files (filename, file_word_count, import_date)
                VALUES (?, ?, CURRENT_TIMESTAMP);
            """;

        // send the SQL command to the database and generate file_id
        try (   Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertFileSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, document.getFileName());
            stmt.setInt(2, document.getWordCount());
            // use INSERT command to store data in db
            stmt.executeUpdate();

            // Retrieve the auto-generated file_id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                // check if insertion was successful
                if (rs.next()) {
                    int fileId = rs.getInt(1);
                    System.out.println("File metadata inserted successfully. File ID: " + fileId);
                    return fileId;
                } else {
                    // throw an error if file metadata was not stored correctly
                    throw new SQLException("Failed to retrieve generated file_id");
                }
            }
        }
    }

    /**
     * Returns the top N starting words ordered by starting_word_occurences
     * (and word_frequency as a tiebreaker).
     * Used by the frontend to display the top 10 words in the HomeView
     *
     * @param limit maximum number of words to return
     * @return an ArrayList of starting words
     * Written by Sneha Shrinivas
     */
    public static ArrayList<String> getTopStartingWords(int limit) {
        ArrayList<String> words = new ArrayList<>();

        String sql = """
            SELECT word
            FROM Words
            WHERE starting_word_occurences > 0
            ORDER BY starting_word_occurences DESC, word_frequency DESC
            LIMIT ?
            """;

        try (   Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    words.add(rs.getString("word"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting top starting words: " + ex.getMessage());
        }

        return words;
    }

    /**
     * inserts word object fields like frequency, start and ending word frequency, into the database table
     * @param wordPart Instance of word object
     * @throws SQLException
     * Written by Ezzah Qureshi, Khushi Dubey, and Andersen Breyel
     */
    public void insertWord(Word wordPart) throws SQLException {
        String insertWordSQL = """
                    INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        word_frequency = word_frequency + VALUES(word_frequency),
                        starting_word_occurences = starting_word_occurences + VALUES(starting_word_occurences),
                        ending_word_occurences = ending_word_occurences + VALUES(ending_word_occurences);
                """;

        // try to get db connection
        try (   Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertWordSQL)) {

            // pass in values into their associated parameters in the prepared statement
            stmt.setString(1, wordPart.getWordText());
            stmt.setInt(2, wordPart.getFrequency());
            stmt.setInt(3, wordPart.getStartWordCount());
            stmt.setInt(4, wordPart.getEndWordCount());
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a bigram relationship into the Relationships table.
     * Retrieves the word IDs for both the current word and next word using the getWordId() helper function,
     * then inserts or updates the relationship record. Uses ON DUPLICATE KEY UPDATE to increment the
     * combination_count if the bigram relationship already exists in the database.
     * This method is essential for tracking which words commonly follow other words in the documents.
     *
     * @param bigram instance of relationships expert class
     * @throws SQLException
     *
     * Written by Ezzah Qureshi, Khushi Dubey, and Andersen Breyel
     */
    public void insertBigram(Relationship bigram) throws SQLException {
        String insertRelationshipSQL = """
                    INSERT INTO Relationships (current_word_id, next_word_id, combination_count)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        combination_count = combination_count + VALUES(combination_count);
                """;

        try (Connection conn = getConnection();
             PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationshipSQL)) {
            // Get word IDs for both current and next word using helper function
            int currentId = bigram.getCurrentWordID();
            int nextId = bigram.getNextWordID();

            // Insert the bigram relationship with the word IDs
            insertRelStmt.setInt(1, currentId);
            insertRelStmt.setInt(2, nextId);
            insertRelStmt.setInt(3, bigram.getCombinationCount());
            insertRelStmt.executeUpdate();
        }
    }

    /**
     * Load all words directly into HashMap with one database query
     *
     * @return HashMap of all words with their properties
     * written by sneha
     */
    public static HashMap<String, Word> loadAllWordsOptimized() {
        // initialize hash map and statement
        HashMap<String, Word> wordHashMap = new HashMap<>();
        String sql = """
                SELECT word, word_frequency, starting_word_occurences, ending_word_occurences
                FROM Words
                """;

        // try to get db connection
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through result set
            while (rs.next()) {
                // get the values from the result set
                String wordText = rs.getString("word");
                int frequency = rs.getInt("word_frequency");
                int startCount = rs.getInt("starting_word_occurences");
                int endCount = rs.getInt("ending_word_occurences");

                // Create Word object directly with values from query and store in hashmap
                Word word = new Word(wordText, startCount, endCount, frequency);
                wordHashMap.put(wordText, word);
            }

            // system message
            System.out.println("Loaded " + wordHashMap.size() + " words into memory");
        } catch (SQLException ex) {
            System.err.println("SQL error loading words: " + ex.getMessage());
        }
        return wordHashMap;
    }

    /**
     * Load all bigrams directly into HashMap with one database query
     *
     * @return HashMap mapping "word1 word2" to combination count
     * written by sneha
     */
    public static HashMap<String, Integer> loadAllBigramsOptimized() {
        HashMap<String, Integer> bigramHashMap = new HashMap<>();

        // Join with Words table to get word text from word id
        String sql = """
                SELECT w1.word AS current_word, w2.word AS next_word, r.combination_count
                FROM Relationships r
                JOIN Words w1 ON r.current_word_id = w1.word_id
                JOIN Words w2 ON r.next_word_id = w2.word_id
                """;

        // try to get db connection
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through result set
            while (rs.next()) {
                // get values from result set
                String currentWord = rs.getString("current_word");
                String nextWord = rs.getString("next_word");
                int combinationCount = rs.getInt("combination_count");

                // create a key for the hashmap and store key and frequency count in map
                String bigramKey = currentWord + " " + nextWord;
                bigramHashMap.put(bigramKey, combinationCount);
            }

            // system message
            System.out.println("Loaded " + bigramHashMap.size() + " bigrams into memory");
        } catch (SQLException ex) {
            System.err.println("SQL error loading bigrams: " + ex.getMessage());
        }
        return bigramHashMap;
    }
}
