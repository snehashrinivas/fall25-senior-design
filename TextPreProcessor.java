import java.sql.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

public class TextPreProcessor {

    // TODO: write db connection  class
    private static Connection conn = null; // JDBC connections to db

    public TextPreProcessor(Connection conn) {
        this.conn = conn;}  // constructor to store the connection
    
    /**
     * Returns a scanner object corresponding to the file at the given location
     * File must be in the same folder
     * @param fileName  String representation of the name of a file in the same GitHub folder
     * @return          Scanner object of the file at the given location or null if the file can't be found
     * Written by Andersen Breyel
     */
    private static Scanner importFile(String fileName){
        try {
            return new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        // If file is not found return NULL
        return null;
    }

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
     * Takes in a tokenized sentence and loops through each word to update the database via SQL statements.
     * Records each word’s frequency, start, and end occurrences (Words table)
     * Records each word pair by the word ids (retrieve word ids from words table) and update their occurrence (Relationships table)
     *
     * @param sentence  An ArrayList of words representing the current sentence (tokenized with eos token "</s>")
     * @throws SQLException  if a database access error occurs
     * Written by Ezzah Qureshi
     */
    private static void countWords(ArrayList<String> sentence) throws SQLException {
        // if sentence is null then method returns
        if (sentence == null) return;

        // SQL statement for inserting into words table in db, relies on the word being unique
        // based on values provided, the word table will update word frequency, starting and ending word frequencies
        String insertWordSQL = """
            INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
            VALUES (?, 1, ?, ?)
            ON DUPLICATE KEY UPDATE
                word_frequency = word_frequency + 1,
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
            for (int i = 0; i < sentence.size() - 1; i++) {
                String current = sentence.get(i);
                String next = sentence.get(i + 1);
                // TODO: Count EOS token as apart of the DB
                // if the current token is </s> skip to next iteration
                //if (current.equals("</s>")) continue;

                // if isStart is true then current word is first token
                boolean isStart = (i == 0);
                // if isEnd is true then the next word is the end of sentence token
                boolean isEnd = (next.equals("</s>"));

                // setting parameters for the word table sql statement
                // sets current word as first value to pass
                insertWordStmt.setString(1, current);
                // if it's a starting word, pass 1, if not pass 0 into second parameter
                insertWordStmt.setInt(2, isStart ? 1 : 0);
                // if it's the last word, pass 1, if not pass 0 into third parameter
                insertWordStmt.setInt(3, isEnd ? 1 : 0);
                // executeUpdate allows java to run the prev sql lines and alter db, in our case performs INSERT
                insertWordStmt.executeUpdate();

                // if there is another word after the current one, we need to make sure it's recorded in the db,
                // since the relationships table has a reference to the next_word_id
                // TODO: count EOS token
                //if (!next.equals("</s>")) {
                // set first parameter to next word, dont pass in anything else
                insertWordStmt.setString(1, next);
                insertWordStmt.setInt(2, 0);
                insertWordStmt.setInt(3, 0);
                insertWordStmt.executeUpdate();

                // To update relationship table, we need to id of current word and next word
                int currentId = getWordId(current, getWordIdStmt);
                // TODO: count EOS token
                //int nextId = next.equals("</s>") ? -1 : getWordId(next, getWordIdStmt);
                int nextId = getWordId(next, getWordIdStmt);

                // If there is a next word, then insert current id and next id, if not then skip
                //if (nextId != -1) {
                    insertRelStmt.setInt(1, currentId);
                    insertRelStmt.setInt(2, nextId);
                    insertRelStmt.executeUpdate();
                //}
            }
        }
    }

    /**
     * Check if character is non-alphabetic, non-numerical, or non-punctuation
     * Passes uppercase, lowercase letters, punctuation, and numbers
     * Not counting ',' ':' or ';' to not worry about grammatic structure of sentences
     * @param myChar character being checked
     * @return preprocessed, lowercase version of input word that contains only digits and alphabetical letters
     * Written by Khushi Dubey
     */
    private static boolean checkIfMiscellaneous(char myChar) {
         // words are immediately converted to lower case so no need to check for uppercase letters
         if (myChar >= 97 && myChar <= 122) { // lowercase a-z
            return false;
            // punctuation marks that are allowed
        } else if (myChar == '!' || myChar == '.' || myChar == '?' || myChar == '-') {
            return false;
        } else {
            // if it is not part of the criterias above, it is considered miscellaneous
            return true;
        }
    }
    /**
     * Searches given ASCII file for the unaccented equivalent of an accented character
     * Each line in the file has the form accented=regular to appropriately parse
     * If there is an equivalent, corresponding regular character is returned; otherwise, '\0' is returned.
     *
     * @param accented     The accented character to be converted
     * @param accentsFile  The text file that defines mappings of accented to regular characters
     * @return             The unaccented equivalent if found; '\0' if not found or if an error occurs
     * Written by Khushi Dubey
     */
    // TODO: change to use scanner instead of buffered reader
    private static char findRegularChar(char accented, Scanner accentsFile) {

            // loop lines on the mapping file
            while (accentsFile.hasNextLine()) {
                String currentLine = accentsFile.nextLine();
                // process only the lines with the correct format
                if (currentLine.contains("=")) {
                    // tokenize the line
                    String[] parts = currentLine.split("=");
                    // ensure line is properly parsed into two parts
                    if (parts.length == 2) {
                        // extract accented character and equivalent regular character
                        char accentedChar = parts[0].trim().charAt(0);
                        char regularChar = parts[1].trim().charAt(0);

                        // return the mapping if match is found
                        if (accentedChar == accented) {
                            return regularChar;
                        }
                    }
                }
            }
        // Accented char is not found return null
        return '\0';
    }

    /**
     * Check if character is accented using its ASCII
     * @param c character being checked
     * @return true or false, true if character is accented and false if not
     * Written by Khushi Dubey
     */
    private static boolean isAccented(char c) {
        // check accented characters ASCII range to see if character is accented
        return (c >= 192 && c <= 255 && c != 215 && c != 247); //exclude x and division signs
    }

    /**
     * Preprocesses each word by removing miscellaneous symbols and replacing accented letters
     * with their non-accented equivalent letters. As each character is parsed, only alphabetic
     * letters and numbers are kept
     * @param word The word that needs to be preprocessed
     * @param accentsFile File that stores the equivalent non-accented letters for the accented letters
     * @return preprocessed, lowercase version of input word that contains only digits and alphabetical letters
     * Written by Khushi Dubey
     */
    private static String cleanWord(String word, Scanner accentsFile) {
        // preprocessed word
        StringBuilder cleaned = new StringBuilder();

        // iterate through each character in the word
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);

            // skip over symbols and numbers
            if (checkIfMiscellaneous(currentChar)) {
                continue;
            }

            // replace accented with their regular equivalents
            if (isAccented(currentChar)) {
                // find the replacement letter
                char replacement = findRegularChar(currentChar, accentsFile);

                // if replacement letter is found, add replaced letter to final preprocessed word
                if (replacement != '\0') {
                    cleaned.append(replacement);
                } else {
                    // if replacement letter is not found, fallback on accented letter
                    System.err.println("Accented char: " + currentChar + " not found");
                }
            } else {
                // append normal character as per regular
                cleaned.append(currentChar);
            }
        }
        // convert word to lowercase and return
        return cleaned.toString();
    }



/**
 * Method that loops through the given document and preprocesses it, discarding miscellaneous symbols, converting
 * words to lower case, and cleaning them by removing miscellaneous symbols and converting accented characters,
 * before calling the method countWords to update the database
 * while keeping track of words added
 * @param textFile   Scanner object of the text document to preprocess
 * @param asciiFile  Scanner object of ascii file used by the method cleanWords to convert ascii characters
 * @return           int of the number of words added to the database
 * Written by Andersen Breyel
 */
private static int preprocess(Scanner textFile, Scanner asciiFile) throws SQLException {
    // Counter to keep track of number of words being added into the database from this document
    int count = 0;
    // ArrayList to keep track of the words in the current sentence
    ArrayList<String> currentSentence = new ArrayList<>();
    // Loop through text file, token by token
    while (textFile.hasNext()) {
        // Get next token and immediately convert to lower case
        String newWord = textFile.next().toLowerCase();
        // TODO: test empty string builder case
        /*
        // If the new word is a miscellaneous symbol discard it
        if (checkIfMiscellaneous(newWord.charAt(0))) {
            continue;
        }*/
        // Clean the word, removing miscellaneous symbols and converting accents
        String cleanedWord = cleanWord(newWord, asciiFile);
        // Use final char to check if word is the end of the sentence
        String finalChar = cleanedWord.substring(cleanedWord.length() - 2, cleanedWord.length() - 1);
        // If final char is an end of sentence punctuation append the cleaned word, its final char, and the eos token to the current sentence
        // increment the counter, call count words to update the DB and clear out the current sentence
        if (finalChar.equals(".") || finalChar.equals("!") || finalChar.equals("?")) {
            currentSentence.add(cleanedWord.substring(0, cleanedWord.length() - 2));
            currentSentence.add(finalChar);
            currentSentence.add("</s>");
            countWords(currentSentence);
            count += 3;
            currentSentence.clear();
            // To account for text files without punctuation, if the loop makes it to the end without running into eos punctuation
            // treat the entire document as one sentence
        } else if(!textFile.hasNext()) {
            currentSentence.add(cleanedWord);
            currentSentence.add("</s>");
            countWords(currentSentence);
            count ++;
            // Else just add the cleaned word, increment the count and carry on
        } else {
            currentSentence.add(cleanedWord);
            count ++;
        }
    }
    return count;
}

/**
 * Main driver method for the program that prompts user for text files until they
 * want to exit. After the user enters in a file, the method preprocesses the words
 * and accurately stores in the DB and computes the total number of words
 * @param args commands line arguments provided by user
 * Written by Khushi Dubey
 */
public static void main(String[] args) throws SQLException {
        // initialize flag to determine whether program needs to continue accepting file names
    boolean keepReceiving = true;

    // total number of words in a file 
    int fileWordCount = 0;
    Scanner userInput = new Scanner(System.in); // scanner for user input
    Scanner asciiFile = importFile("accents.txt"); // import ASCII file with equivalent non-accented letters

    // loop until user exits program
    while (keepReceiving) {
        fileWordCount = 0; // reset count for each file
        System.out.print("Enter file name or 'no' to exit: "); // prompt user to enter in a file name 
        String fileName = userInput.nextLine().trim(); // read user input

        // if user says no, end program
        if (fileName.equalsIgnoreCase("no")) {
            // set flag to false
            keepReceiving = false;
        } else {
            //import file 
            Scanner currentFile = importFile(fileName);
            // check that file exists before preprocessing
            if (currentFile == null) {
                System.out.println("File not found: " + fileName);
                continue;
            }
            // if it exists, preprocess the file and output its total word count 
            System.out.println("Processing file...");
            fileWordCount = preprocess(currentFile, asciiFile);
            System.out.println("Finished processing. Word count: " + fileWordCount);
        }
    }
    // close scanners and program
    userInput.close();
    // Make sure the file is open
    assert asciiFile != null;
    asciiFile.close();
    System.out.println("Exiting program...");
    }
}
