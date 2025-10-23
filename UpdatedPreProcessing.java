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
            if (myChar >= 'a' && myChar <= 'z') return false;
            if (myChar == '!' || myChar == '.' || myChar == '?' || myChar == '-') return false;
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
            while (accentsFile.hasNextLine()) {
                String line = accentsFile.nextLine();
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        char accentedChar = parts[0].trim().charAt(0);
                        char regularChar = parts[1].trim().charAt(0);
                        if (accentedChar == accented) {
                            return regularChar;
                        }
                    }
                }
            }
            return '\0';
        }

        /**
         * Check if character is accented using its ASCII
         * @param c character being checked
         * @return true or false, true if character is accented and false if not
         * Written by Khushi Dubey
         */
        private static boolean isAccented(char c) {
            return (c >= 192 && c <= 255 && c != 215 && c != 247);
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
            StringBuilder cleaned = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                char currentChar = word.charAt(i);
                if (checkIfMiscellaneous(currentChar)) continue;

                if (isAccented(currentChar)) {
                    char replacement = findRegularChar(currentChar, accentsFile);
                    if (replacement != '\0') cleaned.append(replacement);
                    else System.err.println("Accented char not found: " + currentChar);
                } else {
                    cleaned.append(currentChar);
                }
            }
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
            int count = 0;
            ArrayList<String> currentSentence = new ArrayList<>();

            // Process the text line by line (newlines are ignored)
            while (textFile.hasNextLine()) {
                String line = textFile.nextLine().trim();

                if (line.isEmpty()) continue;

                // Split on whitespace — newlines are already ignored
                String[] tokens = line.toLowerCase().split("\\s+");

                for (String rawToken : tokens) {
                    if (rawToken.isEmpty()) continue;

                    // FIXED: Create a new Scanner for accents file for each word
                    Scanner accentScanner = importFile("accents.txt");

                    // Clean token (removes garbage & converts accents but keeps punctuation)
                    String cleanedWord = cleanWord(rawToken, accentScanner); //asciiFile);

                    // Close the scanner after use
                    if (accentScanner != null) accentScanner.close();

                    if (cleanedWord == null || cleanedWord.isEmpty()) {
                        continue;
                    }

                    // If word ends with punctuation, separate it as its own token
                    char lastChar = cleanedWord.charAt(cleanedWord.length() - 1);
                    boolean endsWithPunc = (lastChar == '.' || lastChar == '!' || lastChar == '?');

                    if (endsWithPunc && cleanedWord.length() > 1) {
                        // Add the word minus punctuation
                        String trimmed = cleanedWord.substring(0, cleanedWord.length() - 1);
                        currentSentence.add(trimmed);
                        count++;

                        // Add the punctuation itself as a separate token
                        currentSentence.add(Character.toString(lastChar));
                        count++;

                        // If it’s a sentence-ending punctuation mark, also add </s>
                        //if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                            currentSentence.add("</s>");
                            count++;
                            dbManager.countWords(currentSentence);
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
                currentSentence.add("</s>");
                dbManager.countWords(currentSentence);
                count += currentSentence.size();
                currentSentence.clear();
            }

            return count;
        }

        /**
         * Main driver method for the program that prompts user for text files until they
         * want to exit. After the user enters in a file, the method preprocesses the words
         * and accurately stores in the DB and computes the total number of words
         * Written by Khushi Dubey
         */
        public static void run() throws SQLException {
            boolean keepReceiving = true;
            int fileWordCount = 0;
            Scanner userInput = new Scanner(System.in);
            Scanner asciiFile = importFile("accents.txt");

            while (keepReceiving) {
                fileWordCount = 0;
                System.out.print("Enter file name or 'no' to exit: ");
                String fileName = userInput.nextLine().trim();

                if (fileName.equalsIgnoreCase("no")) {
                    keepReceiving = false;
                } else {
                    Scanner currentFile = importFile(fileName);
                    if (currentFile == null) {
                        System.out.println("File not found: " + fileName);
                        continue;
                    }

                    // Check if file has content
                    System.err.println("[DEBUG] Checking if file has lines...");
                    if (currentFile.hasNextLine()) {
                        System.out.println("[DEBUG] File HAS lines");
                    } else {
                        System.out.println("[DEBUG] File is EMPTY or already consumed");
                    }
                    System.out.println("[DEBUG] About to call preprocess...");

                    System.out.println("Processing file...");
                    try {
                        fileWordCount = preprocess(currentFile, asciiFile);
                        System.out.println("[DEBUG] preprocess returned: " + fileWordCount);

                    } catch (Exception e) {
                        System.err.println("[ERROR in preprocess]: " + e.getMessage());
                        e.printStackTrace();
                    }
                    System.out.println("Finished processing. Word count: " + fileWordCount);
                }
            }

            userInput.close();
            if (asciiFile != null) asciiFile.close();
            System.out.println("Exiting program...");
        }
    }

