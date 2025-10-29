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
                boolean isEnd = (next != null && next.equals("</s>"));

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
