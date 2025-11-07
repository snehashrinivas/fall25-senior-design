
/*
 * This is a processor class for handling bigram-based language processing operations, calculating probalities to aid in word generation.
 *
 * A bigram is a sequence of two adjacent words in a text.
 * This class processes bigrams in documents to:
 * - Calculate transition probabilities between words
 * - Use the transition probabilities to calculate
 * - Sort bigrams by their probability for word generation output
 *
 *
 */
import java.util.*;
import java.sql.*;

public class BigramProcessor {
    private static DatabaseManager db = null;
    private static Connection conn = null;
    

    public BigramProcessor(Connection conn, DatabaseManager dbManager) {
        BigramProcessor.conn = conn; // assign to static member
        BigramProcessor.db = dbManager;
    }

    

    /**
     * Sorts a HashMap of words and their probabilities by probability in descending order
     * @param unsortedMap HashMap containing words as keys and their probabilities as values
     * @return ArrayList of words sorted by their probabilities in descending order
     * Written by Sneha Shrinivas
     */
    private static ArrayList<String> sortHashMap(HashMap<String, Double> unsortedMap) {
        // convert HashMap entries to a List for sorting
        List<Map.Entry<String, Double>> list = new ArrayList<>(unsortedMap.entrySet());
        
        // Sort the list based on values (probabilities) in descending order
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        
        // Create an ArrayList to store just the words in sorted order
        ArrayList<String> sortedList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedList.add(entry.getKey());
        }
        
        return sortedList;
    }

    /**
     * Finds the probability of the suffix following the prefix by dividing the number of times the given bigram appears
     * by the number of times the prefix appears. If smoothing is true, laplace smoothing is applied and
     * each bigram count is incremented by 1 and each prefix count is incremented by the number of words in the vocabulary.
     * This is done to reduce overfitting by reducing the model's confidence in one bigram at the cost of increasing the
     * perplexity of the prediction
     * @param prefix    String - first word in the bigram
     * @param suffix    String - second word in the bigram
     * @param smoothing boolean - used to determine if laplace smoothing
     * @return          double - probability of the given bigram appearing in the data
     * Written by Andersen Breyel
     */
    private static double BigramProbability(String prefix, String suffix, boolean smoothing) {
        double prob;
        // Used if laplace smoothing is being applied, number of rows in the Words table
        int vocabSize = db.getVocabSize();
        // If either of the words or the bigram do not appear in the database, the bigram has a 0% chance of appearing
        // give error handling --> check if bigram exists in db, if not return 0
        if (!db.wordInDB(prefix) || !db.wordInDB(suffix) || !db.wordsInDB(prefix, suffix)) {
            System.out.println("prefix in db: " + db.wordInDB(prefix));
            System.out.println("suffix in db: " + db.wordInDB(suffix));
            System.out.println("prefix and suffix in db: " + db.wordsInDB(prefix, suffix));
            return 0.0;
        }
        // Word frequency of the prefix word in the Words table
        int prefixUnigramCount = db.getWordFreq(prefix);
        // Frequency of the bigram in the Relationships table
        int bigramCount = db.getWordsFreq(prefix, suffix);
        // If smoothing is true apply laplace smoothing
        if (smoothing) {
            bigramCount += 1;
            prefixUnigramCount += vocabSize;
            prob = (double) bigramCount / prefixUnigramCount;
        // Otherwise calculate the probability as normal
        } else {
            prob = (double) bigramCount / prefixUnigramCount;
        }
        return prob;
    }

    /**
     * From the prefix sentence generate a list of all possible next words and their probabilities
     * @param prefixSentence String - Starting point for the next possible words
     * @param smoothing      boolean - determines if laplace smoothing will be applied when calculated the bigram probability
     * @return               ArrayList<String> - List of next possible words sorted by how likely they are to appear
     * Written by Andersen Breyel
     */
    private static ArrayList<String> getNextWords(String prefixSentence, boolean smoothing) {
        // Tokenize the sentence into an array of words by splitting it on whitespaces
        String[] tokenizedSentence = prefixSentence.split(" ");
        // Convert to lowercase and clean each word so they match words in the database
        // String[] preprocessedSentence = preprocessSentence(tokenizedSentence);
        // Calculate the next words using the last word of the prefix sentence
        //preprocessedSentence[preprocessedSentence.length() - 1];
        //int str_length = tokenizedSentence.length - 1;
        String prefixWord = tokenizedSentence[tokenizedSentence.length - 1].toLowerCase();
        // Create an array list of all the words that succeed the current word in the Relationships table
        ArrayList<String> bigrams = db.getPossibleBigrams(prefixWord);
        // List of sorted words to be returned
        ArrayList<String> sortedList = new ArrayList<String>();
        // Hashmap of unsorted word-bigram probability pairs
        HashMap<String, Double> unsortedList = new HashMap<String, Double>();
        // If the list of possible words is empty print and error
        if(bigrams.isEmpty()) {
            System.out.println("error word no suffixes found");
            return sortedList;
        }
        // For each word in the possible words list calculate its probability and put them into the hash map
        for (String newWord : bigrams) {
            Double newProb = BigramProbability(prefixWord, newWord, smoothing);
            unsortedList.put(newWord, newProb);
        }
        // Sort the hash map based on its probabilities and return a sorted list of words
        sortedList = sortHashMap(unsortedList);
        return sortedList;
    }

    /**
     * Function to pick a random word from an array of 3
     * @param possibleWords Array of Strings representing the 3 most likely next words
     * @return              String representing the random word chosen
     * Written by Andersen Breyel
     */
    private static String pickFromThree(String[] possibleWords) {
        // Generate random number from 0-2 inclusive
        int randomNum = (int)(Math.random() * 3);
        return possibleWords[randomNum];
    }

    /**
     * Uses bigram probabilities to generate the next n words of a given prefix sentence or until the eos token is generated
     * @param prefixSentence String - Starting point for the generated sentence
     * @param n              int - max number of words to be generated
     * @param smoothing      boolean - determines if laplace smoothing will be applied when calculated the bigram probability
     * @return               String - the sentence generated by the model based on the prefix sentence
     * Written by Andersen Breyel
     */
    private static String generateSentence(String prefixSentence, int n, boolean smoothing) {
        // Tokenize the sentence into an array of words by splitting it on whitespaces
        String[] tokenizedSentence = prefixSentence.split(" ");
        // Convert to lowercase and clean each word so they match words in the database
       // String[] preprocessedSentence = preprocessSentence(tokenizedSentence);
        // Start from the last word of the prefix sentence
        String currentWord = tokenizedSentence[(tokenizedSentence.length - 1)];
        System.out.println("this is " + currentWord);
        String generatedSentence =  prefixSentence + " ";
        // Generate a maximum of n words
        for (int i = 0; i < n; i++) {
            // Don't know what the next word will be
            String nextWord = "";
            // Initialize the highest prob for each word as negative so it will be replaced immediately
            //double highestProb = -1.0;
            // Don't know the next word's probability yet
            //double newProb = 0.0;
            // If the word is not in the database print an error and exit
            System.out.println("entered loop");
            if (!db.wordInDB(currentWord)) {
                System.out.println("error word not found");
                break;
            } else {
                // Get the candidates for the next possible word
                ArrayList<String> nextPossibleWords = getNextWords(currentWord, smoothing);
                // Store the 3 highest candidates in an arrray to pick one at random
                String[] topThree = new String[]{nextPossibleWords.get(0), nextPossibleWords.get(1), nextPossibleWords.get(2)};
                nextWord = pickFromThree(topThree);
                // Append the new word to the generated sentence
                generatedSentence = generatedSentence + nextWord + " ";
                System.out.println(generatedSentence);
                // If the newly appended word is the eos token break out of the loop
                // dbmanager method that returns a boolean, if that word ever ends a sentence just end the sentence
                if (DatabaseManager.wordEndsSentence(nextWord)) {//nextWord.equals("</s>")) {
                    break;
                }
                // Update the current word to be the newly appended word
                currentWord = nextWord;

            }
        }
        return generatedSentence;
    }



    /**
     * Driver method to process text
     * Written by Andersen Breyel
     */
    public static void run() {
        final boolean smoothing = true;
        final String prefixSentence = "Hi I am";
        System.out.println("hello");
        String newSentence = generateSentence(prefixSentence, 10, smoothing);
        System.out.println("New sentence: " + newSentence);
        ArrayList<String> possibleNextWords = getNextWords(prefixSentence, smoothing);
        for (String nextWord : possibleNextWords) {
            System.out.println(nextWord);
        }
    }
}

    

