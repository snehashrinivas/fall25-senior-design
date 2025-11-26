package backend;
/*
 * This class manages database operations for the Sentence Builder
 * It handles inserting words and their relationships into a MySQL database to track:
 *   - Word frequencies (how often each word appears)
 *   - Starting word occurrences (how often a word begins a sentence)
 *   - Ending word occurrences (how often a word ends a sentence)
 *   - Word relationships (which words commonly follow other words)
 *
 * The class uses prepared statements for SQL queries and manages the db connection through
 * connect/disconnect methods.
 * written by Ezzah, Khushi, and Andersen
 */

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {
    private static Connection conn;
    private static DatabaseManager instance;

    // Prepared statements for reuse
    private PreparedStatement insertWordStmt;
    private PreparedStatement getWordIdStmt;
    private PreparedStatement insertRelStmt;
    private PreparedStatement insertFileStmt;

    public DatabaseManager(Connection conn) {
        this.conn = conn;
    }

    /**
     * @return
     * @throws SQLException
     */
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null || !instance.isConnected()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection connection = DriverManager.getConnection(
                        // "jdbc:mysql://localhost:3306/sentencebuilderdb", //khushi's url
                        "jdbc:mysql://localhost:3306/SentenceBuilder",
                        "root",
                        // ""
                        "your_new_password" //"" Khushi's password
                );
                instance = new DatabaseManager(connection);
                // System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }

    // function to check if db is connected

    /**
     * Checks if database connection is active
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // function to stop connection

    /**
     * Closes all prepared statements and the database connection
     */
    public void disconnect() {
        try {
            if (insertWordStmt != null) insertWordStmt.close();
            if (getWordIdStmt != null) getWordIdStmt.close();
            if (insertRelStmt != null) insertRelStmt.close();
            if (insertFileStmt != null) insertFileStmt.close(); // added
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException ex) {
            System.err.println("Error closing database connection: " + ex.getMessage());
        }
    }

    // for that word if that is an eos and if tis greater than 0 return true
    public static boolean wordEndsSentence(String unigram) {
        String wordEOS = """
                SELECT ending_word_occurences AS eos FROM Words WHERE word = ? 
                """;

        try (PreparedStatement wordEOSStmt = conn.prepareStatement(wordEOS);) {

            wordEOSStmt.setString(1, unigram);
            ResultSet rs = wordEOSStmt.executeQuery();

            if (rs.next()) {
                int eosValue = rs.getInt("eos");
                return eosValue > 0;
            } else {
                System.out.println("Error for finding unigram occurence" + unigram);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    // need to put get id and countWords in here

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @param word String - word whose ID should be retrieved
     * @throws SQLException if the word is not found or database access fails
     *                      Written by Ezzah Qureshi
     */
    public static int getWordId(String word) {//} throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT word_id FROM Words WHERE word = ?;")) {

            // the word id retrieval statement, set value = to word
            stmt.setString(1, word);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    return rs.getInt("word_id");
                }
            }
            throw new SQLException("Word not found: " + word);

        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @param word String - word whose ID should be retrieved
     * @throws SQLException if the word is not found or database access fails
     *                      Written by Ezzah Qureshi
     */
    public static int getWordStart(String word) {//} throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT starting_word_occurences FROM Words WHERE word = ?;")) {

            // the word id retrieval statement, set value = to word
            stmt.setString(1, word);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    return rs.getInt("starting_word_occurences");
                }
            }
            throw new SQLException("Word not found: " + word);

        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @param word String - word whose ID should be retrieved
     * @throws SQLException if the word is not found or database access fails
     *                      Written by Ezzah Qureshi
     */
    public static int getWordEnd(String word) {//} throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ending_word_occurences FROM Words WHERE word = ?;")) {

            // the word id retrieval statement, set value = to word
            stmt.setString(1, word);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    return rs.getInt("ending_word_occurences");
                }
            }
            throw new SQLException("Word not found: " + word);

        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return 0;
    }

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @throws SQLException if the word is not found or database access fails
     *                      Written by Ezzah Qureshi
     */
    public static ArrayList<String> getAllRowsWordTextCol() {//} throws SQLException {
        ArrayList<String> WordList = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT word FROM Words;")) {

            // the word id retrieval statement, set value = to word
           // stmt.setString(1, word);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                //if (rs.next()) {
                  //  return rs.getInt("ending_word_occurences");
                //}
                while (rs.next()) {
                    // Get the next item from the result set
                    String word = rs.getString("word");
                    //String suffix = getWord(suffixID, getWordStmt);
                    WordList.add(word);
                }
                return WordList;
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return WordList;
    }

    /**
     * A helper function to retrieve the unique database ID (word_id) of a given word
     * If the word doesn’t exist, function throws a SQLException to indicate a database inconsistency.
     *
     * @throws SQLException if the word is not found or database access fails
     *                      Written by Ezzah Qureshi
     */
    public static HashMap<String, Integer> getAllBigramRows() {//} throws SQLException {
        HashMap<String, Integer> BigramHashMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Relationships;")) {

            // the word id retrieval statement, set value = to word
            // stmt.setString(1, word);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                //if (rs.next()) {
                //  return rs.getInt("ending_word_occurences");
                //}
                while (rs.next()) {
                    // Get the next item from the result set
                    int prevWordID = rs.getInt("current_word_id");
                    int currentWordID = rs.getInt("next_word_id");
                    int comboCount = rs.getInt("combination_count");
                    //String suffix = getWord(suffixID, getWordStmt);

                    String prevWord = getWord(prevWordID);
                    String currentWord = getWord(currentWordID);

                    String hashMapKey = prevWord + " " + currentWord;

                    BigramHashMap.put(hashMapKey, comboCount);
                }
                return BigramHashMap;
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        return BigramHashMap;
    }



    /**
     * A helper function to retrieve the word given its ID
     *
     * @param ID   int - ID whose corresponding ID should be retrieved
     * @throws SQLException if the word ID is not found or database access fails
     *                      Written by Ezzah Qureshi and Andersen Breyel
     */
    private static String getWord(int ID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT word FROM Words WHERE word_id = ?;")) {

            // the word id retrieval statement, set value = to word
            stmt.setInt(1, ID);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    return rs.getString("word");
                }
            }
            throw new SQLException(" ID not found: " + ID);
        } catch(SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }

        return null;
    }

    /**
     * A helper function to retrieve the word given its ID
     *
     * @throws SQLException if the word ID is not found or database access fails
     *                      Written by Ezzah Qureshi and Andersen Breyel
     */
    public static ResultSet getWordRow(String word) {//} throws SQLException {
        int wordID = getWordId(word);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT word_frequency, starting_word_occurences, ending_word_occurences FROM Words WHERE word_id = ?;")) {
            // the word id retrieval statement, set value = to word
            stmt.setInt(1, wordID);
            // execute query and return ResultSet (object that holds sql results)
            try (ResultSet rs = stmt.executeQuery()) {
                // read the result (pointer is pointing to before the int, hence why .next)
                if (rs.next()) {
                    return rs;
                }
            }
            throw new SQLException(" Word not found: " + word);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


       /* // the word id retrieval statement, set value = to word
        stmt.setInt(1, ID);
        // execute query and return ResultSet (object that holds sql results)
        try (ResultSet rs = stmt.executeQuery()) {
            // read the result (pointer is pointing to before the int, hence why .next)
            if (rs.next()) {
                return rs.getString("word");
            }
        }
        throw new SQLException(" ID not found: " + ID);
    }*/

    /**
     * Helper method to get the number of words stored in the database
     *
     * @return int of the number rows in the Words table in the dataase
     * Written by Andersen Breyel
     */
    public int getVocabSize() {
        // Try block to catch SQL exceptions
        // Creates a statement object that will become the query
        try (Statement stmt = conn.createStatement()) {
            // Counts the number of rows for the first column, the primary key,
            // and stores them in an alias "NumberOfRows"
            ResultSet rs = stmt.executeQuery("SELECT COUNT(1) as NumberOfRows FROM Words");
            // Moves the pointer to the first row of the result set
            if (rs.next()) {
                // Return the result of the query
                return rs.getInt("NumberOfRows");
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        // Intellij giving redline without this return statement but should be unreachable
        return 0;
    }

    /**
     * Helper method to check if the word is being stored in the database
     *
     * @param unigram given word to be checked if it's in the database
     * @return boolean representing if the word exists in the database or not
     * Written by Andersen Breyel
     */
    public boolean wordInDB(String unigram) {
        // Prepared SQL Query String
        String checkWordSQL = "SELECT * FROM Words WHERE word = ?";
        // Try opening sql connections
        try (PreparedStatement checkWordStmt = conn.prepareStatement(checkWordSQL)) {
            checkWordStmt.setString(1, unigram);
            ResultSet rs = checkWordStmt.executeQuery();
            // rs.next() returns false if it's pointing to the end of the ResultSet, true otherwise
            return rs.next();
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Helper method to check if given bigram is stored in the database
     *
     * @param prefix prefix word in bigram to be checked
     * @param suffix suffix word in bigram to be checked
     * @return boolean representing if given bigram is being stored in the database
     * Written by Andersen Breyel
     */
    public boolean wordsInDB(String prefix, String suffix) {
        // SQL statement to select word_id for a given word
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        // Prepared SQL Query String
        String checkWordsSQL = "SELECT * FROM Relationships WHERE current_word_id = ? AND next_word_id = ?";

       // try (
                // Open a connection to get both prefix and suffix word IDs using prepared statements
         //       PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL);
           //     PreparedStatement getSuffixIDStmt = conn.prepareStatement(getWordIdSQL);
        //) {
            // Pass prepared statements into getWordIDs to get the respective IDs
        int prefixID = getWordId(prefix);//, getPrefixIDStmt);
        int suffixID = getWordId(suffix);//, getSuffixIDStmt);

        // Try opening sql connections
        try (PreparedStatement checkWordStmt = conn.prepareStatement(checkWordsSQL)) {
            checkWordStmt.setInt(1, prefixID);
            checkWordStmt.setInt(2, suffixID);
            ResultSet rs = checkWordStmt.executeQuery();
            // rs.next() returns false if it's pointing to the end of the ResultSet, true otherwise
            return rs.next();
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Relationship table: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Helper method that returns the given word's frequency
     *
     * @param unigram word to be queried for its frequency in the database
     * @return an int representing the number of times the given word appears in the documents
     * Written by Andersen Breyel and Khushi Dubey
     */
    public static int getWordFreq(String unigram, HashMap<String, Word> wordHashMap) {
        /*
        // Prepared SQL Query String
        String getWordFreqSQL = "SELECT word_frequency FROM Words WHERE word = ?";
        // Try block to catch SQL exceptions
        // Creates a statement object that will become the query
        try (PreparedStatement stmt = conn.prepareStatement(getWordFreqSQL)) {
            stmt.setString(1, unigram);
            ResultSet rs = stmt.executeQuery();
            // Moves the pointer to the first row of the result set
            if (rs.next()) {
                // Return the result of the query
                return rs.getInt("word_frequency");
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        // Intellij giving redline without this return statement but should be unreachable
        return 0;

         */
        // Check if word exists in HashMap
        if (wordHashMap.containsKey(unigram)) {
            return wordHashMap.get(unigram).getFrequency();
        }
        // Return 0 if word not found
        return 0;
    } // added

    /**
     * Helper method that returns the given bigram's frequency
     *
     * @param prefix prefix of the bigram to be queried for its frequency in the database
     * @param suffix suffix of the bigram to be queried for its frequency in the database
     * @return an int representing the number of times the given bigram appears in the documents
     * Written by Andersen Breyel and Khushi Dubey
     */
    public int getWordsFreq(String prefix, String suffix, HashMap<String, Integer> wordsHashMap ) {
        /*

        // SQL statement to select word_id for a given word
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        // SQL statement to get the frequency of a bigram given the two word's IDs
        String getWordsFreqSQL = "SELECT combination_count FROM Relationships WHERE (current_word_id = ? AND next_word_id = ?)";
        // Open SQL connections to get word IDs and catch SQL exceptions
        try (
                // Open a connection to get both prefix and suffix word IDs using prepared statements
                PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement getSuffixIDStmt = conn.prepareStatement(getWordIdSQL);
        ) {
            // Pass prepared statements into getWordIDs to get the respective IDs
            int prefixID = getWordId(prefix);//, getPrefixIDStmt);
            int suffixID = getWordId(suffix);//, getSuffixIDStmt);
            // Open SQL connection to get bigram freqs and catch SQL exceptions
            try (PreparedStatement getWordsFreqStmt = conn.prepareStatement(getWordsFreqSQL)) {
                getWordsFreqStmt.setInt(1, prefixID);
                getWordsFreqStmt.setInt(2, suffixID);
                ResultSet rs = getWordsFreqStmt.executeQuery();
                if (rs.next()) {
                    // Return the result of the query
                    return rs.getInt("combination_count");
                }
            } catch (SQLException ex) {
                System.err.println("SQL error getting bigram freqs for getWordsFreq method: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting word IDs for getWordsFreq method: " + ex.getMessage());

        }
        return 0;
         */
        // Create the bigram key
        String bigramKey = prefix + " " + suffix;

        // Check if bigram exists in HashMap
        if (wordsHashMap.containsKey(bigramKey)) {
            return wordsHashMap.get(bigramKey);
        }

        // Return 0 if bigram not found
        return 0;


    } // added

    /**
     * Helper function that returns all the words that have followed the given prefix
     * across the documents
     *
     * @param prefix given word used to query the Words table for all the possible bigram suffixes
     * @return an array list of all words that follow the given word across the documents
     * Written by Andersen Breyel
     */
    public ArrayList<String> getPossibleBigrams(String prefix) {
        ArrayList<String> suffixList = new ArrayList<>();
        // SQL statement to select word_id for a given word
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        // SQL statement to select word for a given word ID
        String getWordSQL = "SELECT word FROM Words WHERE word_id = ?";
        // SQL statement to get the frequency of a bigram given the two word's IDs
        String getSuffixIDsSQL = "SELECT next_word_id FROM Relationships WHERE current_word_id = ?";
        // Open SQL connection to get word ID and catch SQL exceptions
        //try (PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL)) {
            // Pass prepared statement into getWordID to get the prefix ID
        int prefixID = 0;//, getPrefixIDStmt);
        prefixID = getWordId(prefix);
        try (
                    PreparedStatement getSuffixIDsStmt = conn.prepareStatement(getSuffixIDsSQL);
                    PreparedStatement getWordStmt = conn.prepareStatement(getWordSQL);
            ) {
                getSuffixIDsStmt.setInt(1, prefixID);
                ResultSet suffixIDsRS = getSuffixIDsStmt.executeQuery();
                // Loop through the result set
                while (suffixIDsRS.next()) {
                    // Get the next item from the result set
                    int suffixID = suffixIDsRS.getInt("next_word_id");
                    String suffix = getWord(suffixID);//, getWordStmt);
                    suffixList.add(suffix);
                }
                return suffixList;
            } catch (SQLException ex) {
                System.err.println("SQL error next word IDs for getPossibleBigrams method: " + ex.getMessage());
            }

        //} catch (SQLException ex) {
          //  System.err.println("SQL error getting word IDs for getPossibleBigrams method: " + ex.getMessage());
        //}
        return suffixList;
    }


    /**
     * Inserts file metadata into the Files table
     * Records the filename, word count, and import timestamp in the database
     *
     * @param filename  Name of the file being imported
     * @param wordCount Total number of words processed from the file
     * @return The generated file_id for the inserted record
     * @throws SQLException if a database access error occurs
     *                      Written by Khushi Dubey
     */
    public int insertFileMetadata(String filename, int wordCount) throws SQLException {
        // define query to insert file metadata into Files db
        // use CURRENT_TIMESTAMP to record when the file was inserted
        String insertFileSQL = """
                    INSERT INTO Files (filename, file_word_count, import_date)
                    VALUES (?, ?, CURRENT_TIMESTAMP);
                """;

        // send the SQL command to the database and generate file_id
        try (PreparedStatement stmt = conn.prepareStatement(insertFileSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, filename);
            stmt.setInt(2, wordCount);
            // use INSERT command to store data in db
            stmt.executeUpdate();

            // Retrieve the auto-generated file_id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                // check if insertion was successful
                if (rs.next()) {
                    // print a confirmation message if metadata was inserted
                    int fileId = rs.getInt(1);
                    System.out.println("File metadata inserted successfully. File ID: " + fileId);

                    // return file_id to caller method
                    return fileId;
                } else {
                    // throw an error if file metadata was not stored correctly
                    throw new SQLException("Failed to retrieve generated file_id");
                }
            }
        }
    }

    public int insertFileMetadata(Document document) throws SQLException {
        // define query to insert file metadata into Files db
        // use CURRENT_TIMESTAMP to record when the file was inserted
        String insertFileSQL = """
                    INSERT INTO Files (filename, file_word_count, import_date)
                    VALUES (?, ?, CURRENT_TIMESTAMP);
                """;

        // send the SQL command to the database and generate file_id
        try (PreparedStatement stmt = conn.prepareStatement(insertFileSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, document.getFileName());
            stmt.setInt(2, document.getWordCount());
            // use INSERT command to store data in db
            stmt.executeUpdate();

            // Retrieve the auto-generated file_id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                // check if insertion was successful
                if (rs.next()) {
                    // print a confirmation message if metadata was inserted
                    int fileId = rs.getInt(1);
                    System.out.println("File metadata inserted successfully. File ID: " + fileId);

                    // return file_id to caller method
                    return fileId;
                } else {
                    // throw an error if file metadata was not stored correctly
                    throw new SQLException("Failed to retrieve generated file_id");
                }
            }
        }
    }

    /**
     * Inserts a single word into the Words table with its frequency and positional data.
     * Uses ON DUPLICATE KEY UPDATE to increment frequencies if the word already exists.
     * This method updates the word_frequency, starting_word_occurences, and ending_word_occurences
     * based on the position of the word within a sentence.
     *
     * @param word      String - the word to be inserted into the database
     * @param frequency int - the word frequency count to add
     * @param isStart   boolean - true if the word starts a sentence, false otherwise
     * @param isEnd     boolean - true if the word ends a sentence, false otherwise
     * @throws SQLException Written by Ezzah Qureshi, Khushi Dubey, and Andersen Breyel
     */
    public void insertWord(String word, int frequency, boolean isStart, boolean isEnd) throws SQLException {
        // open db connection and then close it --> use catch block to capture sql error
        String insertWordSQL = """
                    INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        word_frequency = word_frequency + VALUES(word_frequency),
                        starting_word_occurences = starting_word_occurences + VALUES(starting_word_occurences),
                        ending_word_occurences = ending_word_occurences + VALUES(ending_word_occurences);
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertWordSQL)) {
            stmt.setString(1, word);
            stmt.setInt(2, frequency);
            stmt.setInt(3, isStart ? 1 : 0);
            stmt.setInt(4, isEnd ? 1 : 0);
            stmt.executeUpdate();
        }
    }


    /**
     * Inserts a single word into the Words table with its frequency and positional data.
     * Uses ON DUPLICATE KEY UPDATE to increment frequencies if the word already exists.
     * This method updates the word_frequency, starting_word_occurences, and ending_word_occurences
     * based on the position of the word within a sentence.
     *
     *
     * @param wordPart
     * @throws SQLException Written by Ezzah Qureshi, Khushi Dubey, and Andersen Breyel
     */
    public void insertWord(Word wordPart) throws SQLException {
        // open db connection and then close it --> use catch block to capture sql error
        String insertWordSQL = """
                    INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        word_frequency = word_frequency + VALUES(word_frequency),
                        starting_word_occurences = starting_word_occurences + VALUES(starting_word_occurences),
                        ending_word_occurences = ending_word_occurences + VALUES(ending_word_occurences);
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertWordSQL)) {
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
     * @param currentWord String - the prefix word in the bigram relationship
     * @param nextWord    String - the suffix word in the bigram relationship
     * @throws SQLException Written by Ezzah Qureshi, Khushi Dubey, and Andersen Breyel
     */
    public void insertBigram(String currentWord, String nextWord) throws SQLException {
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        String insertRelationshipSQL = """
                    INSERT INTO Relationships (current_word_id, next_word_id, combination_count)
                    VALUES (?, ?, 1)
                    ON DUPLICATE KEY UPDATE
                        combination_count = combination_count + 1;
                """;

        try (
     //           PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL);
    //            PreparedStatement getSuffixIDStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationshipSQL)
        ) {
            // Get word IDs for both current and next word using helper function
            int currentId = getWordId(currentWord); //getPrefixIDStmt);
            int nextId = getWordId(nextWord);//, getSuffixIDStmt);

            // Insert the bigram relationship with the word IDs
            insertRelStmt.setInt(1, currentId);
            insertRelStmt.setInt(2, nextId);
            insertRelStmt.executeUpdate();
        }
    }

    public void insertBigram(Relationship bigram) throws SQLException {
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        String insertRelationshipSQL = """
                    INSERT INTO Relationships (current_word_id, next_word_id, combination_count)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        combination_count = combination_count + VALUES(combination_count);
                """;

        try (
                PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement getSuffixIDStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationshipSQL)
        ) {
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
     * Returns a probability map of all next words and their bigram probabilities
     * given a prefix word, optionally using Laplace smoothing.
     * Written by Rida Basit
     */
    public HashMap<String, Double> getBigramProbabilities(String prefixWord, boolean smoothing, HashMap<String, Word> wordHashMap,
                                                          HashMap<String, Integer> wordsHashMap) {
        /*
        // create an empty list to store each next word and its probability
        HashMap<String, Double> probs = new HashMap<>();
        // get all the words that can come after prefixWord from the database
        ArrayList<String> nextWords = getPossibleBigrams(prefixWord);
        // count how many unique words are in the whole database
        int vocabSize = getVocabSize();
        // get how many times the prefix word appears in total
        int prefixUnigramCount = getWordFreq(prefixWord);

        for (String next : nextWords) {
            // how many times the two words appear together in that order
            int bigramCount = getWordsFreq(prefixWord, next);
            double prob; //store
            // check if smoothing should be applied
            if (smoothing) {
                prob = (double) (bigramCount + 1) / (prefixUnigramCount + vocabSize);
            } else {
                //no smoothing, divide the bigram count by the prefix word count
                prob = prefixUnigramCount > 0 ? (double) bigramCount / prefixUnigramCount : 0.0;
            }
            // store the next word and its calculated probability
            probs.put(next, prob);
        }
        return probs;
    }
         */

        // create an empty list to store each next word and its probability
        HashMap<String, Double> probs = new HashMap<>();

        // Check if prefix word exists in HashMap
        if (!wordHashMap.containsKey(prefixWord)) {
            return probs; // Return empty map if word not found
        }

        // Get all bigrams that start with prefixWord from HashMap
        ArrayList<String> nextWords = new ArrayList<>();
        for (String bigramKey : wordsHashMap.keySet()) {
            String[] parts = bigramKey.split(" ");
            if (parts.length == 2 && parts[0].equals(prefixWord)) {
                nextWords.add(parts[1]);
            }
        }

        // count how many unique words are in the whole HashMap
        int vocabSize = wordHashMap.size();
        // get how many times the prefix word appears in total from HashMap
        int prefixUnigramCount = wordHashMap.get(prefixWord).getFrequency();

        for (String next : nextWords) {
            // how many times the two words appear together in that order from HashMap
            String bigramKey = prefixWord + " " + next;
            int bigramCount = wordsHashMap.getOrDefault(bigramKey, 0);

            double prob; //store
            // check if smoothing should be applied
            if (smoothing) {
                prob = (double) (bigramCount + 1) / (prefixUnigramCount + vocabSize);
            } else {
                //no smoothing, divide the bigram count by the prefix word count
                prob = prefixUnigramCount > 0 ? (double) bigramCount / prefixUnigramCount : 0.0;
            }
            // store the next word and its calculated probability
            probs.put(next, prob);
        }
        return probs;
    } // added
}