/*
This program takes in a text file, parses its words, cleans their contents, and stores them in a database.
Cleaning words involves converting accented letters to their regular characters, removing miscellaneous
characters, converting words to lowercase, and adding end of sentence tokens. Lines are processed
one at a time, split into tokens, cleaned, and inserted into the database through the DatabaseManager
object. End of sentences are marked with "</s>".

The main purpose of this program is to count the total number of words in the file, store word frequency counts,
and end of sentence/beginning of sentence frequency counts.

*/

import java.sql.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

    public class UpdatedPreProcessing {

        // JDBC connection to database
        private static Connection conn = null;
        private static DatabaseManager dbManager = null;


        public UpdatedPreProcessing(Connection conn, DatabaseManager dbManager) {
            UpdatedPreProcessing.conn = conn; // assign to static member
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
            try {
                return new Scanner(new File(fileName));// "utf-8"));
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
         * @param accentsFile File that stores the equivalent non-accented letters for the accented letters
         * @return preprocessed, lowercase version of input word that contains only digits and alphabetical letters
         * Written by Khushi Dubey
         */
        // TODO: figure out accents issue
        private static String cleanWord(String word, Scanner accentsFile) {
            // preprocessed word
            StringBuilder cleaned = new StringBuilder();

            // iterate through each character in the word
            for (int i = 0; i < word.length(); i++) {
                char currentChar = word.charAt(i);

                // skip over symbols and numbers
                if (checkIfMiscellaneous(currentChar)) continue;

                // replace accented with their regular equivalents
                if (isAccented(currentChar)) {
                    // find the replacement letter
                    char replacement = findRegularChar(currentChar, accentsFile);
                    // if replacement letter is found, add replaced letter to final preprocessed word
                    if (replacement != '\0') cleaned.append(replacement);
                    // if replacement letter is not found, fallback on accented letter
                    else System.err.println("Accented char not found: " + currentChar);
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
        // TODO: change logic to not have eos
        private static int preprocess(Scanner textFile, Scanner asciiFile) throws SQLException {
            // track total number of words
            int count = 0;
            ArrayList<String> currentSentence = new ArrayList<>();

            // Process the text line by line (newlines are ignored)
            while (textFile.hasNextLine()) {
                // process line by line
                String line = textFile.nextLine().trim();

                // skip empty lines
                if (line.isEmpty()) continue;

                // Split on whitespace — newlines are already ignored
                String[] tokens = line.toLowerCase().split("\\s+");

                for (String rawToken : tokens) {
                    // skip if any token is empty
                    if (rawToken.isEmpty()) continue;

                    // TODO: change logic so we are not importing file each time
                    // initalize a new Scanner for accents file
                    Scanner accentScanner = importFile("accents.txt");

                    // Clean token (removes garbage & converts accents but keeps punctuation)
                    String cleanedWord = cleanWord(rawToken, accentScanner);

                    // Close the scanner after use
                    if (accentScanner != null) accentScanner.close();

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
                            currentSentence.add(Character.toString(lastChar));
                            count++;
                        }
                        // if the word length is greater than 1
                        else {
                            // Trim off the punctuation and add it to the current sentence
                            String trimmed = cleanedWord.substring(0, cleanedWord.length() - 1);
                            currentSentence.add(trimmed);
                            count++;

                            // Add the punctuation itself as a separate token
                            currentSentence.add(Character.toString(lastChar));
                            count++;
                        }
                        // If it’s a sentence-ending punctuation mark, also add </s>
                        //if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                          //  currentSentence.add("</s>");
                          //  count++;

                            // send word to database for counting
                        dbManager.countWords(currentSentence);
                            // clear list to process next sentence
                        currentSentence.clear();
                        //}
                    } else {
                        // Just add the cleaned word normally
                        currentSentence.add(cleanedWord);
                        count++;
                    }
                }
            }
            // Handle any leftover words at the end (no punctuation)
            if (!currentSentence.isEmpty()) {
                // add end of sentence token
                //currentSentence.add("</s>");
                dbManager.countWords(currentSentence);

                // increment count for remaining tokens
                count += currentSentence.size();
                currentSentence.clear();
            }
            // return total number of words processed
            return count;
        }

        /**
         * Main driver method for the program that prompts user for text files until they
         * want to exit. After the user enters in a file, the method preprocesses the words
         * and accurately stores in the DB and computes the total number of words
         * Written by Khushi Dubey
         */

        // TODO: write file info, check is file is already in the db
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

                    // Check if file has content, Debugging statements
                    System.err.println("[DEBUG] Checking if file has lines...");
                    if (currentFile.hasNextLine()) {
                        System.out.println("[DEBUG] File HAS lines");
                    } else {
                        System.out.println("[DEBUG] File is EMPTY or already consumed");
                    }
                    System.out.println("[DEBUG] About to call preprocess...");

                    System.out.println("Processing file...");

                    // if it exists, preprocess the file and output its total word count
                    try {
                        fileWordCount = preprocess(currentFile, asciiFile);
                        System.out.println("[DEBUG] preprocess returned: " + fileWordCount);

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

