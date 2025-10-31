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
 *
 */

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private static Connection conn;

    // Prepared statements for reuse
    private PreparedStatement insertWordStmt;
    private PreparedStatement getWordIdStmt;
    private PreparedStatement insertRelStmt;
    private PreparedStatement insertFileStmt; // added

    public DatabaseManager(Connection conn) {//String url, String username, String password, Connection conn) {
       /* this.url = url;
        this.username = username;
        this.password = password;*/
        this.conn = conn;
    }

    /*private void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String strConnect = "jdbc:mysql://localhost:3306/SentenceBuilder";
            conn = DriverManager.getConnection(strConnect, "root", "your_new_password");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC Driver not found: " + ex.getMessage());
        } catch (SQLException ex) {
            throw new SQLException("Database connection error: " + ex.getMessage());
        }
    }*/

    // function to check if db is connected
    /**
     * Checks if database connection is active
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

            try ( PreparedStatement wordEOSStmt = conn.prepareStatement(wordEOS);) {

                wordEOSStmt.setString(1, unigram);
                ResultSet rs = wordEOSStmt.executeQuery();

                if (rs.next()) {
                    int eosValue = rs.getInt("eos");
                    return eosValue > 0;
                }
                else {
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
     * @param word  String - word whose ID should be retrieved
     * @param stmt  PreparedStatement - SELECT statement for looking up word_id by word
     * @throws SQLException  if the word is not found or database access fails
     * Written by Ezzah Qureshi
     */
    private static int getWordId(String word, PreparedStatement stmt) throws SQLException {
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
    }

    /**
     * A helper function to retrieve the word given its ID
     * @param ID  int - ID whose corresponding ID should be retrieved
     * @param stmt  PreparedStatement - SELECT statement for looking up word by word_id
     * @throws SQLException  if the word ID is not found or database access fails
     * Written by Ezzah Qureshi and Andersen Breyel
     */
    private static String getWord(int ID, PreparedStatement stmt) throws SQLException {
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
    }

    /**
     * Helper method to get the number of words stored in the database
     * @return int of the number rows in the Words table in the dataase
     * Written by Andersen Breyel
     */
    public int getVocabSize() {
        // Try block to catch SQL exceptions
        // Creates a statement object that will become the query
        try(Statement stmt = conn.createStatement()) {
            // Counts the number of rows for the first column, the primary key,
            // and stores them in an alias "NumberOfRows"
            ResultSet rs = stmt.executeQuery("SELECT COUNT(1) as NumberOfRows FROM Words");
            // Moves the pointer to the first row of the result set
            if(rs.next()) {
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
     * @param unigram given word to be checked if it's in the database
     * @return     boolean representing if the word exists in the database or not
     * Written by Andersen Breyel
     */
    public boolean wordInDB(String unigram) {
        // Prepared SQL Query String
        String checkWordSQL = "SELECT * FROM Words WHERE word = ?";
        // Try opening sql connections
        try(PreparedStatement checkWordStmt = conn.prepareStatement(checkWordSQL)) {
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
     * @param prefix prefix word in bigram to be checked
     * @param suffix suffix word in bigram to be checked
     * @return      boolean representing if given bigram is being stored in the database
     * Written by Andersen Breyel
     */
    public boolean wordsInDB(String prefix, String suffix) {
        // SQL statement to select word_id for a given word
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";
        // Prepared SQL Query String
        String checkWordsSQL = "SELECT * FROM Relationships WHERE current_word_id = ? AND next_word_id = ?";

        try (
                // Open a connection to get both prefix and suffix word IDs using prepared statements
                PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement getSuffixIDStmt = conn.prepareStatement(getWordIdSQL);
        ) {
            // Pass prepared statements into getWordIDs to get the respective IDs
            int prefixID = getWordId(prefix, getPrefixIDStmt);
            int suffixID = getWordId(suffix, getSuffixIDStmt);

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        /**
     * Helper method that returns the given word's frequency
     * @param unigram word to be queried for its frequency in the database
     * @return      an int representing the number of times the given word appears in the documents
     * Written by Andersen Breyel
     */
    public int getWordFreq(String unigram) {
        // Prepared SQL Query String
        String getWordFreqSQL = "SELECT word_frequency FROM Words WHERE word = ?";
        // Try block to catch SQL exceptions
        // Creates a statement object that will become the query
        try(PreparedStatement stmt = conn.prepareStatement(getWordFreqSQL)) {
            stmt.setString(1, unigram);
            ResultSet rs = stmt.executeQuery();
            // Moves the pointer to the first row of the result set
            if(rs.next()) {
                // Return the result of the query
                return rs.getInt("word_frequency");
            }
        } catch (SQLException ex) {
            System.err.println("SQL error getting number of rows of Words table: " + ex.getMessage());
        }
        // Intellij giving redline without this return statement but should be unreachable
        return 0;
    }

    /**
     * Helper method that returns the given bigram's frequency
     * @param prefix prefix of the bigram to be queried for its frequency in the database
     * @param suffix suffix of the bigram to be queried for its frequency in the database
     * @return      an int representing the number of times the given bigram appears in the documents
     * Written by Andersen Breyel
     */
    public int getWordsFreq(String prefix, String suffix) {
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
            int prefixID = getWordId(prefix, getPrefixIDStmt);
            int suffixID = getWordId(suffix, getSuffixIDStmt);
            // Open SQL connection to get bigram freqs and catch SQL exceptions
            try (PreparedStatement getWordsFreqStmt = conn.prepareStatement(getWordsFreqSQL)) {
                getWordsFreqStmt.setInt(1, prefixID);
                getWordsFreqStmt.setInt(2, suffixID);
                ResultSet rs = getWordsFreqStmt.executeQuery();
                if(rs.next()) {
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
    }

    /**
     * Helper function that returns all the words that have followed the given prefix
     * across the documents
     * @param prefix given word used to query the Words table for all the possible bigram suffixes
     * @return       an array list of all words that follow the given word across the documents
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
        try (PreparedStatement getPrefixIDStmt = conn.prepareStatement(getWordIdSQL)) {
            // Pass prepared statement into getWordID to get the prefix ID
            int prefixID = getWordId(prefix, getPrefixIDStmt);
            try(
                    PreparedStatement getSuffixIDsStmt = conn.prepareStatement(getSuffixIDsSQL);
                    PreparedStatement getWordStmt = conn.prepareStatement(getWordSQL);
            ) {
                getSuffixIDsStmt.setInt(1, prefixID);
                ResultSet suffixIDsRS = getSuffixIDsStmt.executeQuery();
                // Loop through the result set
                while(suffixIDsRS.next()) {
                    // Get the next item from the result set
                    int suffixID = suffixIDsRS.getInt("next_word_id");
                    String suffix = getWord(suffixID, getWordStmt);
                    suffixList.add(suffix);
                }
                return suffixList;
            } catch (SQLException ex) {
                System.err.println("SQL error next word IDs for getPossibleBigrams method: " + ex.getMessage());
            }

        } catch (SQLException ex) {
            System.err.println("SQL error getting word IDs for getPossibleBigrams method: " + ex.getMessage());
        }
        return suffixList;
    }


    /**
     * Inserts file metadata into the Files table
     * Records the filename, word count, and import timestamp in the database
     * @param filename      Name of the file being imported
     * @param wordCount     Total number of words processed from the file
     * @return              The generated file_id for the inserted record
     * @throws SQLException if a database access error occurs
     * Written by Khushi Dubey
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
    } // added

    /**
     * Takes in a tokenized sentence and loops through each word to update the database via SQL statements.
     * Records each word’s frequency, start, and end occurrences (Words table)
     * Records each word pair by the word ids (retrieve word ids from words table) and update their occurrence (Relationships table)
     *
     * @param sentence  An ArrayList of words representing the current sentence (tokenized with eos token "</s>")
     * @throws SQLException  if a database access error occurs
     * Written by Ezzah Qureshi
     */
    public void countWords(ArrayList<String> sentence) throws SQLException {
        // if sentence is null then method returns
        if (sentence == null) return;

        // SQL statement for inserting into words table in db, relies on the word being unique
        // based on values provided, the word table will update word frequency, starting and ending word frequencies
        String insertWordSQL = """
            INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                word_frequency = word_frequency + VALUES(word_frequency),
                starting_word_occurences = starting_word_occurences + VALUES(starting_word_occurences),
                ending_word_occurences = ending_word_occurences + VALUES(ending_word_occurences);
        """;

        // SQL statement to select word_id for a given word
        String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";

        // SQL statement for inserting into the relationships table in db
        String insertRelationshipSQL = """
            INSERT INTO Relationships (current_word_id, next_word_id, combination_count)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE
                combination_count = combination_count + 1;
        """;

        //opens prepared statements
        try (
                PreparedStatement insertWordStmt = conn.prepareStatement(insertWordSQL);
                PreparedStatement getWordIdStmt = conn.prepareStatement(getWordIdSQL);
                PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationshipSQL)
        ) {
            // loop over the current sentence and get current word and next word
            for (int i = 0; i < sentence.size(); i++) {
                String current = sentence.get(i);
                //String next = sentence.get(i + 1);
                String next = (i < sentence.size() - 1) ? sentence.get(i + 1) : null;

                // if the current token is </s> skip to next iteration
                //if (current.equals("</s>")) continue;

                // if isStart is true then current word is first token
                boolean isStart = (i == 0);
                // if isEnd is true then the next word is the end of sentence token
                //boolean isEnd = (next.equals("</s>"));
                //boolean isEnd = (i == sentence.size() - 1) || (next != null && next.equals("</s>"));
                //boolean isEnd = (next != null && next.equals("</s>"));
                boolean isEnd = (i == sentence.size() - 1);

                // setting parameters for the word table sql statement
                // sets current word as first value to pass
                insertWordStmt.setString(1, current);
                // frequency
                insertWordStmt.setInt(2, 1);
                // if it's a starting word, pass 1, if not pass 0 into second parameter
                insertWordStmt.setInt(3, isStart ? 1 : 0);
                // if it's the last word, pass 1, if not pass 0 into third parameter
                insertWordStmt.setInt(4, isEnd ? 1 : 0);
                // executeUpdate allows java to run the prev sql lines and alter db, in our case performs INSERT
                insertWordStmt.executeUpdate();

                // if there is another word after the current one, we need to make sure it's recorded in the db,
                // since the relationships table has a reference to the next_word_id

                if (next != null) {
                    // set first parameter to next word, dont pass in anything else
                    insertWordStmt.setString(1, next);
                    insertWordStmt.setInt(2, 0);
                    insertWordStmt.setInt(3, 0);
                    insertWordStmt.setInt(4, 0);
                    insertWordStmt.executeUpdate();

                    // To update relationship table, we need to id of current word and next word
                    int currentId = getWordId(current, getWordIdStmt);

                    //int nextId = next.equals("</s>") ? -1 : getWordId(next, getWordIdStmt);
                    int nextId = getWordId(next, getWordIdStmt);

                    // If there is a next word, then insert current id and next id, if not then skip
                    //if (nextId != -1) {
                    insertRelStmt.setInt(1, currentId);
                    insertRelStmt.setInt(2, nextId);
                    insertRelStmt.executeUpdate();
                }
            }
        }
    }
}
