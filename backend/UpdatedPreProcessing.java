package backend;/*
This program takes in a text file, parses its words, cleans their contents, and stores them in a database.
Cleaning words involves converting accented letters to their regular characters, removing miscellaneous
characters, converting words to lowercase, and adding end of sentence tokens. Lines are processed
one at a time, split into tokens, cleaned, and inserted into the database through the backend.backend.DatabaseManager
object. End of sentences are marked with "</s>".

The main purpose of this program is to count the total number of words in the file, store word frequency counts,
and end of sentence/beginning of sentence frequency counts.

*/

import java.sql.*;
import java.io.*;
import java.util.Scanner;

public class UpdatedPreProcessing {
    private static DatabaseManager dbManager = null;

    /**
     * Constructor
     *
     * @param dbManager instance of DatabaseManager
     * Written by Ezzah
     */
    public UpdatedPreProcessing(DatabaseManager dbManager) {
        UpdatedPreProcessing.dbManager = dbManager;
    }

    /**
     * Returns a scanner object corresponding to the file at the given location
     * File must be in the same folder
     * @param fileName  String representation of the name of a file in the same GitHub folder
     * @return          Scanner object of the file at the given location or null if the file can't be found
     * Written by Andersen Breyel
     */
    private static Scanner importFile(String fileName) {
        // try to create a scanner
        try {
            return new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
            return null;
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
        // alphabetical letters are allowed so skip
        if (myChar >= 'a' && myChar <= 'z') return false;
        // !, -, ?, and . are also allowed so skip
        if (myChar == '!' || myChar == '.' || myChar == '?' || myChar == '-') return false;
        // if character is not alphabetical or not one of the allowed punctuation marks, mark as miscellaneous
        return true;
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
    private static char findRegularChar(char accented, Scanner accentsFile) {
        // loop lines on the mapping file
        while (accentsFile.hasNextLine()) {
            String line = accentsFile.nextLine();

            // process only the lines with the correct format
            if (line.contains("=")) {

                // tokenize the line
                String[] parts = line.split("=");

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
     * @return preprocessed, lowercase version of input word that contains only digits and alphabetical letters
     * Written by Khushi Dubey
     */
    private static String cleanWord(String word){
        // preprocessed word
        StringBuilder cleaned = new StringBuilder();

        // iterate through each character in the word
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);

            // replace accented with their regular equivalents
            if (isAccented(currentChar)) {
                // Create a new scanner for each accented character
                Scanner accentScanner = importFile("accents.txt");
                char replacement = findRegularChar(currentChar, accentScanner);

                if (accentScanner != null) accentScanner.close();

                // if replacement letter is found, add replaced letter to final preprocessed word
                if (replacement != '\0') {
                    cleaned.append(replacement);
                } else {
                    System.err.println("Accented char not found: " + currentChar);
                }
                // Move to next character
                continue;
            }

            // skip over symbols and numbers (after checking for accents)
            if (checkIfMiscellaneous(currentChar)) continue;

            // append normal character as per regular
            cleaned.append(currentChar);
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
     * @return           int of the number of words added to the database
     * Written by Andersen Breyel
     */
    private static int preprocess(Scanner textFile) throws SQLException {
        // track total number of words
        int count = 0;
        boolean isItFirstWord = true;
        String previousWord = null;

        // Process the text line by line (newlines are ignored)
        while (textFile.hasNextLine()) {
            String line = textFile.nextLine().trim();

            // skip empty lines
            if (line.isEmpty()) continue;

            // Split on whitespace — newlines are already ignored
            String[] tokens = line.toLowerCase().split("\\s+");

            for (String rawToken : tokens) {
                // skip if any token is empty
                if (rawToken.isEmpty()) continue;

                // Clean token (removes garbage & converts accents but keeps punctuation)
                String cleanedWord = cleanWord(rawToken);

                // skip word if it becomes empty after cleaning it
                if (cleanedWord == null || cleanedWord.isEmpty()) {
                    continue;
                }

                // If word ends with punctuation, separate it as its own token
                char lastChar = cleanedWord.charAt(cleanedWord.length() - 1);
                boolean endsWithPunc = (lastChar == '.' || lastChar == '!' || lastChar == '?');

                // handle words that end with punctuation
                // account for case if a punctuation ends a word that was cleaned out, so standalone punctuation
                if (endsWithPunc) {
                    // Case 1: Only punctuation (e.g., ".")
                    if (cleanedWord.length() == 1) {
                        String punctuation = cleanedWord;

                        // Create Word object for punctuation
                        Word punctuationWord = new Word(punctuation, 0, 1, 1);
                        dbManager.insertWord(punctuationWord);
                        count++;

                        // If there was a previous word, get the word IDs and create Relationship object and insert bigrams
                        if (previousWord != null) {
                            int previousWordID = dbManager.getWordId(previousWord);
                            int punctuationID = dbManager.getWordId(punctuation);
                            Relationship rel = new Relationship(previousWordID, punctuationID, 1);
                            dbManager.insertBigram(rel);
                        }

                        // set booleans
                        previousWord = null;
                        isItFirstWord = true;
                    } else {
                        // Add the word minus punctuation
                        String wordPart = cleanedWord.substring(0, cleanedWord.length() - 1);
                        String punctuation = Character.toString(lastChar);

                        // check the bool value
                        int firstWordInt = isItFirstWord ? 1 : 0;

                        // Send word part and insert bigram from previous word
                        // Create Word object for the word part
                        Word wordPartObj = new Word(wordPart, firstWordInt, 0, 1);
                        dbManager.insertWord(wordPartObj);
                        count++;

                        // If there was a previous word, get the word IDs and create Relationship object and insert bigrams
                        if (previousWord != null) {
                            int previousWordID = dbManager.getWordId(previousWord);
                            int wordPartID = dbManager.getWordId(wordPart);
                            Relationship rel1 = new Relationship(previousWordID, wordPartID, 1);
                            dbManager.insertBigram(rel1);
                        }

                        // Send punctuation as end-of-sentence marker
                        // Create Word object for punctuation
                        Word punctuationObj = new Word(punctuation, 0, 1, 1);
                        dbManager.insertWord(punctuationObj);
                        count++;

                        // Insert bigram from word to punctuation
                        int wordPartID = dbManager.getWordId(wordPart);
                        int punctuationID = dbManager.getWordId(punctuation);
                        Relationship rel2 = new Relationship(wordPartID, punctuationID, 1);
                        dbManager.insertBigram(rel2);

                        // set bools
                        previousWord = null;
                        isItFirstWord = true;
                    }
                } else {
                    // check boolean value
                    int firstWordInt = isItFirstWord ? 1 : 0;
                    // Regular word without punctuation
                    Word wordObj = new Word(cleanedWord, firstWordInt, 0, 1);
                    dbManager.insertWord(wordObj);
                    count++;

                    // If there was a previous word, get word IDs and insert the bigram relationship
                    if (previousWord != null) {
                        int previousWordID = dbManager.getWordId(previousWord);
                        int cleanedWordID = dbManager.getWordId(cleanedWord);
                        Relationship rel = new Relationship(previousWordID, cleanedWordID,1);
                        dbManager.insertBigram(rel);
                    }

                    // set bools
                    previousWord = cleanedWord;
                    isItFirstWord = false;
                    // If it’s a sentence-ending punctuation mark, also add </s>
                    //if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                    //currentSentence.add("</s>");
                    //count++;

                    // send word to database for counting
                    //dbManager.countWords(currentSentence);
                    // clear list to process next sentence
                    //currentSentence.clear();
                    // } else {
                    // Just add the cleaned word normally
                    // currentSentence.add(cleanedWord);
                   // count++;
                    //}
                }
            }
        }
        // Handle any leftover words at the end (no punctuation)
        /*if (!currentSentence.isEmpty()) {
            // add end of sentence token
            //currentSentence.add("</s>");
            dbManager.countWords(currentSentence);

            // increment count for remaining tokens
            count += currentSentence.size();
            currentSentence.clear();
        }
        // return total number of words processed

         */
        return count;
    }

    /*
    This method uses processSingleFile to process a file that is imported from the frontend GUI
    @param file File object representing the text file to be processed
    Written by Sneha Shrinivas
     */
    public static void processFileFromGui(File file) {
        try {
            // Get or create DB connection
            DatabaseManager dbManager = DatabaseManager.getInstance();

            if (!dbManager.isConnected()) {
                System.err.println("Could not connect to DB.");
                return;
            }

            // Initialize static conn/dbManager fields
            new UpdatedPreProcessing(dbManager);

            // Re-use the single-file logic (same as CLI, but no user prompts)
            processSingleFile(dbManager, file);

            // Optionally: don't disconnect so the app can reuse the connection
            // dbManager.disconnect();

        } catch (Exception e) {
            System.err.println("Error processing uploaded file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    Method written to process a single file that is imported from the frontend - helper to processFileFromGui
    @param dbManager DatabaseManager object to interact with the database
    @param file File object representing the text file to be processed
    @throws SQLException if a database access error occurs
    Written by Sneha Shrinivas
     */
    public static void processSingleFile(DatabaseManager dbManager, File file) throws SQLException {
        Scanner asciiFile = importFile("accents.txt");

        Scanner currentFile = importFile(file.getAbsolutePath());
        if (currentFile == null) {
            System.out.println("File not found: " + file.getAbsolutePath());
            if (asciiFile != null) asciiFile.close();
            return;
        }

        int fileWordCount = 0;
        try {
            fileWordCount = preprocess(currentFile, asciiFile);
            dbManager.insertFileMetadata(file.getName(), fileWordCount);
            System.out.println("Finished processing " + file.getName()
                    + " (word count = " + fileWordCount + ")");
        } catch (Exception e) {
            System.err.println("[ERROR in processSingleFile]: " + e.getMessage());
            e.printStackTrace();
        } finally {
            currentFile.close();
            if (asciiFile != null) asciiFile.close();
        }
    }

    /**
     * Main driver method for the program that prompts user for text files until they
     * want to exit. After the user enters in a file, the method preprocesses the words
     * and accurately stores in the DB and computes the total number of words
     * Written by Khushi Dubey
     */
    public static void run() throws SQLException {
        // initialize flag to determine whether program needs to continue accepting file names
        boolean keepReceiving = true;
        // total number of words in a file
        int fileWordCount = 0;
        // scanner for user input
        Scanner userInput = new Scanner(System.in);
        // import ASCII file with equivalent non-accented letters
        Scanner asciiFile = importFile("accents.txt");

        // loop until user exits program
        while (keepReceiving) {
            // reset count for each file
            fileWordCount = 0;
            // prompt user to enter in a file name
            System.out.print("Enter file name or 'no' to exit: ");
            // read user input
            String fileName = userInput.nextLine().trim();

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
                try {
                    fileWordCount = preprocess(currentFile);

                    // Create a Document object and insert it
                    Document doc = new Document(fileName, fileWordCount);
                    dbManager.insertFileMetadata(doc);

                } catch (Exception e) {
                    System.err.println("[ERROR in preprocess]: " + e.getMessage());
                    e.printStackTrace();
                }
                // show user results of the preprocessing method
                System.out.println("Finished processing. Word count: " + fileWordCount);
            }
        }

        // close scanners and program
        userInput.close();

        // Make sure the file is open and then closes it
        if (asciiFile != null) asciiFile.close();
        System.out.println("Exiting program...");
    }
}