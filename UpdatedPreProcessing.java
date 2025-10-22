import java.sql.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

    public class UpdatedPreProcessing {

        // JDBC connection to database
        private static Connection conn = null;

        public UpdatedPreProcessing(Connection conn) {
            UpdatedPreProcessing.conn = conn; // assign to static member
        }

        /**
         * Returns a Scanner object corresponding to the file at the given location.
         * File must be in the same folder.
         */
        private static Scanner importFile(String fileName) {
            try {
                return new Scanner(new File(fileName));
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + fileName);
                return null;
            }
        }

        /** Helper function to get word_id of a given word. */
        private static int getWordId(String word, PreparedStatement stmt) throws SQLException {
            stmt.setString(1, word);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("word_id");
                }
            }
            throw new SQLException("Word not found: " + word);
        }

        /** Records words and relationships in the database. */
        private static void countWords(ArrayList<String> sentence) throws SQLException {
            if (sentence == null) return;

            String insertWordSQL = """
            INSERT INTO Words (word, word_frequency, starting_word_occurences, ending_word_occurences)
            VALUES (?, 1, ?, ?)
            ON DUPLICATE KEY UPDATE
                word_frequency = word_frequency + 1,
                starting_word_occurences = starting_word_occurences + VALUES(starting_word_occurences),
                ending_word_occurences = ending_word_occurences + VALUES(ending_word_occurences);
        """;

            String getWordIdSQL = "SELECT word_id FROM Words WHERE word = ?";

            String insertRelationshipSQL = """
            INSERT INTO Relationships (current_word_id, next_word_id, combination_count)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE
                combination_count = combination_count + 1;
        """;

            try (
                    PreparedStatement insertWordStmt = conn.prepareStatement(insertWordSQL);
                    PreparedStatement getWordIdStmt = conn.prepareStatement(getWordIdSQL);
                    PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationshipSQL)
            ) {
                for (int i = 0; i < sentence.size() - 1; i++) {
                    String current = sentence.get(i);
                    String next = sentence.get(i + 1);

                    boolean isStart = (i == 0);
                    boolean isEnd = (next.equals("</s>"));

                    insertWordStmt.setString(1, current);
                    insertWordStmt.setInt(2, isStart ? 1 : 0);
                    insertWordStmt.setInt(3, isEnd ? 1 : 0);
                    insertWordStmt.executeUpdate();

                    insertWordStmt.setString(1, next);
                    insertWordStmt.setInt(2, 0);
                    insertWordStmt.setInt(3, 0);
                    insertWordStmt.executeUpdate();

                    int currentId = getWordId(current, getWordIdStmt);
                    int nextId = getWordId(next, getWordIdStmt);

                    insertRelStmt.setInt(1, currentId);
                    insertRelStmt.setInt(2, nextId);
                    insertRelStmt.executeUpdate();
                }
            }
        }

        /** Checks if a character is non-alphabetic, non-numeric, or non-punctuation. */
        private static boolean checkIfMiscellaneous(char myChar) {
            if (myChar >= 'a' && myChar <= 'z') return false;
            if (myChar == '!' || myChar == '.' || myChar == '?' || myChar == '-') return false;
            return true;
        }

        /** Converts accented character to regular character using ASCII mapping file. */
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

        /** Checks if a character is accented based on ASCII range. */
        private static boolean isAccented(char c) {
            return (c >= 192 && c <= 255 && c != 215 && c != 247);
        }

        /** Cleans a word by removing miscellaneous characters and replacing accented letters. */
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
        private static int preprocess(Scanner textFile, Scanner asciiFile) throws SQLException {
            int count = 0;
            ArrayList<String> currentSentence = new ArrayList<>();

            System.out.println("=== STARTING PREPROCESS ===");

            // Process the text line by line (newlines are ignored)
            while (textFile.hasNextLine()) {
                String line = textFile.nextLine().trim();
                System.out.println("\n[LINE READ]: '" + line + "'");
                if (line.isEmpty()) continue;

                // Split on whitespace — newlines are already ignored
                String[] tokens = line.toLowerCase().split("\\s+");
                System.out.println("[TOKENS SPLIT]: " + tokens.length + " tokens");

                for (String rawToken : tokens) {
                    if (rawToken.isEmpty()) continue;

                    // FIXED: Create a new Scanner for accents file for each word
                    Scanner accentScanner = importFile("accents.txt");

                    // Clean token (removes garbage & converts accents but keeps punctuation)
                    String cleanedWord = cleanWord(rawToken, asciiFile);

                    // Close the scanner after use
                    if (accentScanner != null) accentScanner.close();

                    System.out.println("  [CLEANED]: '" + cleanedWord + "'");

                    if (cleanedWord == null || cleanedWord.isEmpty()) continue;

                    // If word ends with punctuation, separate it as its own token
                    char lastChar = cleanedWord.charAt(cleanedWord.length() - 1);
                    boolean endsWithPunc = (lastChar == '.' || lastChar == '!' || lastChar == '?');
                    System.out.println("  [ENDS WITH PUNC]: " + endsWithPunc + " (lastChar='" + lastChar + "')");


                    if (endsWithPunc && cleanedWord.length() > 1) {
                        // Add the word minus punctuation
                        String trimmed = cleanedWord.substring(0, cleanedWord.length() - 1);
                        System.out.println("  [ADDING WORD]: '" + trimmed + "'");
                        currentSentence.add(trimmed);
                        count++;

                        System.out.println("  [ADDING PUNC]: '" + lastChar + "'");
                        // Add the punctuation itself as a separate token
                        currentSentence.add(Character.toString(lastChar));
                        count++;

                        System.out.println("  [ADDING EOS]: '</s>'");
                        // If it’s a sentence-ending punctuation mark, also add </s>
                        //if (lastChar == '.' || lastChar == '!' || lastChar == '?') {
                            currentSentence.add("</s>");
                            count++;
                            countWords(currentSentence);
                            currentSentence.clear();
                        //}
                    } else {
                        System.out.println("  [ADDING WORD]: '" + cleanedWord + "'");
                        // Just add the cleaned word normally
                        currentSentence.add(cleanedWord);
                        count++;
                    }

                    System.out.println("  [CURRENT SENTENCE]: " + currentSentence);
                }
            }
            // Handle any leftover words at the end (no punctuation)
            if (!currentSentence.isEmpty()) {
                currentSentence.add("</s>");
                countWords(currentSentence);
                count += currentSentence.size();
                currentSentence.clear();
            }

            return count;
        }
        /** Main driver function. */
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

