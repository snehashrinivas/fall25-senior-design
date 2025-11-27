package backend;
/**
 * This is a processor class for handling bigram-based language processing operations, calculating probalities to aid in word generation.
 *
 * A bigram is a sequence of two adjacent words in a text.
 * This class processes bigrams in documents to:
 * - Calculate transition probabilities between words
 * - Use the transition probabilities to calculate
 * - Sort bigrams by their probability for word generation output
 *
 * Written Andersen, Sneha, Rida
 **/
import java.util.*;
import java.sql.*;

public class BigramProcessor {
    private static DatabaseManager db = null;
    private static HashMap<String, Word> wordHashMap = null;
    private static HashMap<String, Integer> wordsHashMap = null;

    /**
     * Constructor creates dbmaanger instance and imports db tables into hashmaps
     * @param dbManager
     * @throws SQLException
     *
     * Written by Andersen
     */
    public BigramProcessor(DatabaseManager dbManager) {
        BigramProcessor.db = dbManager;
        wordHashMap = DatabaseManager.loadAllWordsOptimized();
        wordsHashMap = DatabaseManager.loadAllBigramsOptimized();
    }

    /**
     * Helper function that returns all the words that have followed the given prefix
     * across the documents
     *
     * @param prefix given word used to query the Words hashmap for all the possible bigram suffixes
     * @return an array list of all words that follow the given word across the documents
     * Written by Andersen Breyel
     */
    public static ArrayList<String> getPossibleBigrams(String prefix) {
        ArrayList<String> suffixList = new ArrayList<>();
        // Loop through each key in the hash map
        for(String key : wordsHashMap.keySet()) {
            // Split the key into the prefix and suffix
            String[] splitKey = key.split(" ");
            // If the current prefix matches the given one append the current prefix to the list
            if(splitKey[0].equals(prefix)) {
                suffixList.add(splitKey[1]);
            }
        }
        return suffixList;
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
        String bigramKey = prefix + " " + suffix;
        // Used if laplace smoothing is being applied, number of rows in the Words table
        int vocabSize = wordHashMap.size();
        // If either of the words or the bigram do not appear in the database, the bigram has a 0% chance of appearing
        // give error handling --> check if bigram exists in hasmaps, if not return 0
        if (!wordHashMap.containsKey(prefix) || !wordHashMap.containsKey(suffix) || !wordsHashMap.containsKey(bigramKey)) {
            System.out.println("prefix " + prefix + " in wordHashMap: " + wordHashMap.containsKey(prefix));
            System.out.println("suffix " + suffix + " in wordHashMap: " + wordHashMap.containsKey(suffix));
            System.out.println("prefix and suffix in wordsHashMap: " + wordsHashMap.containsKey(bigramKey));
            return 0.0;
        }
        // Word frequency of the prefix word in the Words table
        int prefixUnigramCount = wordHashMap.get(prefix).getFrequency();
        // Frequency of the bigram in the Relationships table
        int bigramCount = wordsHashMap.get(bigramKey);
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
        // Calculate the next words using the last word of the prefix sentence
        String prefixWord = tokenizedSentence[tokenizedSentence.length - 1].toLowerCase();

        // Create an array list of all the words that succeed the current word in the Relationships table
        ArrayList<String> bigrams = getPossibleBigrams(prefixWord);
        // List of sorted words to be returned
        ArrayList<String> sortedList = new ArrayList<>();
        // Hashmap of unsorted word-bigram probability pairs
        HashMap<String, Double> unsortedList = new HashMap<>();
        // If the list of possible words is empty print and error
        if (bigrams.isEmpty()) {
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

    // New method to allow frontend calls:
    public String generateFromPrefix(String prefixSentence, int n, boolean smoothing) {
        if (prefixSentence == null || prefixSentence.isEmpty()) {
            return "Please enter a starting word.";
        }

        String sentence = generateSentenceWeighted(prefixSentence, n, smoothing);
        return sentence;
    }

    /**
     * Function to pick a random word based on probability weights
     * @param candidates HashMap of words and their probabilities
     * @return String representing the random word chosen
     * Written by Andersen Breyel and edited by Rida Basit
     */
    private static String pickFromProbabilitiesWeighted(HashMap<String, Double> candidates) {
        if (candidates.isEmpty()) return "";//prevent crash if no candidates
        //sum up all probabilities to normalize the random selection range
        double total = candidates.values().stream().mapToDouble(Double::doubleValue).sum();
        //generate a random number between 0 and the total probability sum
        double rand = Math.random() * total;
        //keep track of running total while looping
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : candidates.entrySet()) {
            //add this word’s probability to the running total
            cumulative += entry.getValue();
            //when threshold is reached, use that word
            if (rand <= cumulative) {
                return entry.getKey();
            }
        }
        // fallback (shouldn't happen)
        return candidates.keySet().iterator().next();
    }

    /**
     * Public helper for the frontend: get a list of possible next words
     * using the same bigram logic as generateSentence.
     *
     * @param prefixSentence current sentence (we use the last word inside)
     * @param smoothing      whether to use Laplace smoothing
     * @return               list of next words sorted by probability
     *
     * Written by Rida Basit
     */
    public static java.util.List<String> getNextWordSuggestions(String prefixSentence, boolean smoothing) {
        if (prefixSentence == null || prefixSentence.isBlank()) {
            return new ArrayList<>();
        }
        // Take the last word of the sentence
        String[] tokens = prefixSentence.split(" ");
        String prefixWord = tokens[tokens.length - 1].toLowerCase();
        // Ask the hashmap once for all next-word probabilities for this prefix
        HashMap<String, Double> nextProbs = getBigramProbabilities(prefixWord, smoothing);
        if (nextProbs.isEmpty()) {
            return new ArrayList<>();
        }
        // Sort by probability descending and return just the words
        ArrayList<String> sorted = sortHashMap(nextProbs);
        // (Optional tiny optimization: only keep top N, e.g. 50)
        // if (sorted.size() > 50) {
        //     return new ArrayList<>(sorted.subList(0, 50));
        // }
        return sorted;
    }

    /**
     * Returns a probability map of all next words and their bigram probabilities
     * given a prefix word, optionally using Laplace smoothing.
     * Written by Rida Basit
     */
    public static HashMap<String, Double> getBigramProbabilities(String prefixWord, boolean smoothing) {
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

    /**
     * Uses bigram probabilities to generate the next n words of a given prefix sentence or until the eos token is generated
     * @param prefixSentence String - Starting point for the generated sentence
     * @param n              int - max number of words to be generated
     * @param smoothing      boolean - determines if laplace smoothing will be applied when calculated the bigram probability
     * @return               String - the sentence generated by the model based on the prefix sentence
     * Written by Andersen Breyel and edited by Rida Basit
     */
    public static String generateSentenceWeighted(String prefixSentence, int n, boolean smoothing) {
        // Tokenize the sentence into an array of words by splitting it on whitespaces
        String[] tokenizedSentence = prefixSentence.split(" ");
        // Start from the last word of the prefix sentence
        String currentWord = tokenizedSentence[tokenizedSentence.length - 1];
        System.out.println("this is " + currentWord);
        String generatedSentence = prefixSentence + " ";

        // Generate a maximum of n words
        //add up to n words to the sentence.
        for (int i = 0; i < n; i++) {
            System.out.println("entered loop");
            //current word exists in the database.
            if (!wordHashMap.containsKey(currentWord)) {
                System.out.println("error word not found");
                break;
            }
            //possible words that can come after this one, and how likely is each
            HashMap<String, Double> nextProbs = getBigramProbabilities(currentWord, smoothing);
            if (nextProbs.isEmpty()) {
                System.out.println("No next words found — stopping generation.");
                break;
            }
            String nextWord = pickFromProbabilitiesWeighted(nextProbs);

            // Append the new word to the generated sentence
            generatedSentence = generatedSentence + nextWord + " ";
            System.out.println(generatedSentence);

            // If the newly appended word ever ends a sentence in the corpus finish the current sentence
            if (wordHashMap.get(nextWord).getEndWordCount() > 0) {
                break;
            }
            // Update the current word to be the newly appended word
            currentWord = nextWord;
        }
        return generatedSentence.trim();
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
    public static String generateSentenceThreeRandom(String prefixSentence, int n, boolean smoothing) {
        // Tokenize the sentence into an array of words by splitting it on whitespaces
        String[] tokenizedSentence = prefixSentence.split(" ");
        // Start from the last word of the prefix sentence
        String currentWord = tokenizedSentence[(tokenizedSentence.length - 1)];
        System.out.println("this is " + currentWord);
        String generatedSentence =  prefixSentence + " ";
        // Generate a maximum of n words
        for (int i = 0; i < n; i++) {
            // Don't know what the next word will be
            String nextWord = "";
            // If the word is not in the database print an error and exit
            if (!wordHashMap.containsKey(currentWord)) {
                System.out.println("error word not found");
                break;
            } else {
                // Get the candidates for the next possible word
                ArrayList<String> nextPossibleWords = getNextWords(currentWord, smoothing);

                // Store the 3 highest candidates in an array to pick one at random
                String firstHighestWord = "";
                String secondHighestWord = "";
                String thirdHighestWord = "";
                double firstHighestProb = -1.0;
                double secondHighestProb = -1.0;
                double thirdHighestProb = -1.0;
                double currentProb = 0.0;
                for (int j = 0; j < nextPossibleWords.size(); j++) {
                    currentProb =  BigramProbability(currentWord, nextPossibleWords.get(j), smoothing);
                    // If the current probability is higher than the current highest probability shift
                    // second to third then first to second before replacing first with the new values
                    if (currentProb > firstHighestProb) {
                        thirdHighestWord = secondHighestWord;
                        thirdHighestProb = secondHighestProb;
                        secondHighestWord = firstHighestWord;
                        secondHighestProb = firstHighestProb;
                        firstHighestWord = nextPossibleWords.get(j);
                        firstHighestProb = currentProb;
                        // If the new probability is only higher than the second highest probability shift second to third
                        // and replace the second probability
                    } else if(currentProb > secondHighestProb) {
                        thirdHighestWord = secondHighestWord;
                        thirdHighestProb =  secondHighestProb;
                        secondHighestWord = nextPossibleWords.get(j);
                        secondHighestProb = currentProb;
                        // If the new probability is only higher than the third probability
                    } else if (currentProb > thirdHighestProb) {
                        thirdHighestWord = nextPossibleWords.get(j);
                        thirdHighestProb = currentProb;
                    }
                }
                // Store the 3 highest candidates in an array to pick one at random
                String[] topThree = {firstHighestWord, secondHighestWord, thirdHighestWord};
                nextWord = pickFromThree(topThree);
                // Append the new word to the generated sentence
                generatedSentence = generatedSentence + nextWord + " ";
                System.out.println(generatedSentence);
                // If the word ends a sentence in the corpus at least once finish the sentence
                //Word wordObject = wordHashMap.get(nextWord);
                //if (wordObject != null && wordObject.getEndWordCount() > 0) {     break; }

                if (wordHashMap.get(nextWord).getEndWordCount() > 0) {
                    break;
                }
                // Update the current word to be the newly appended word
                currentWord = nextWord;
            }
        }
        return generatedSentence;
    }

    /**
     * Uses bigram probabilities to generate the next n words of a given prefix sentence or until the eos token is generated
     * @param prefixSentence String - Starting point for the generated sentence
     * @param n              int - max number of words to be generated
     * @param smoothing      boolean - determines if laplace smoothing will be applied when calculated the bigram probability
     * @return               String - the sentence generated by the model based on the prefix sentence
     * Written by Andersen Breyel
     */
    public static String generateSentenceTopOne(String prefixSentence, int n, boolean smoothing) {
        // Tokenize the sentence into an array of words by splitting it on whitespaces
        String[] tokenizedSentence = prefixSentence.split(" ");

        // Start from the last word of the prefix sentence
        String currentWord = tokenizedSentence[(tokenizedSentence.length - 1)];
        String generatedSentence =  prefixSentence + " ";
        // Generate a maximum of n words
        for (int i = 0; i < n; i++) {
            // Don't know what the next word will be
            String nextWord = "";
            // Initialize the highest prob for each word as negative so it will be replaced immediately
            double highestProb = -1.0;
            // Don't know the next word's probability yet
            double newProb = 0.0;
            // If the word is not in the database print an error and exit
            if (!wordHashMap.containsKey(currentWord)) {
                System.out.println("error word not found");
                break;
            } else {
                // Create an array list of all the words that succeed the current word in the Relationships table
                ArrayList<String> bigrams = getPossibleBigrams(currentWord);
                // If the list is empty print an error and exit
                if (bigrams.isEmpty()) {
                    System.out.println("no bigrams found");
                    return generatedSentence;
                } else {
                    // Otherwise for each word in the list compute its probability and compare to the old word
                    for (int j = 0; j < bigrams.size(); j++) {
                        // Get the next word
                        String potentialWord = bigrams.get(j);
                        // Compute the new word's probability
                        newProb = BigramProbability(currentWord, potentialWord, smoothing);
                        // If the new word has a higher probability update the highest probability and candidate next word
                        if (newProb > highestProb) {
                            highestProb = newProb;
                            nextWord = potentialWord;
                        }
                    }
                    // Append the new word to the generated sentence
                    generatedSentence = generatedSentence + nextWord + " ";
                    // If the newly appended word is the eos token break out of the loop
                    if (wordHashMap.get(nextWord).getEndWordCount() > 0) {
                        break;
                    }
                    // Update the current word to be the newly appended word
                    currentWord = nextWord;
                }
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

        //String newSentence = generateSentenceTopOne(prefixSentence, 50, smoothing);
        String newSentence1 = generateSentenceThreeRandom(prefixSentence, 50, smoothing);
        String newSentence2 = generateSentenceWeighted(prefixSentence, 50, smoothing);

        //System.out.println("New sentence: " + newSentence);
        System.out.println("New sentence: " + newSentence1);
        System.out.println("New sentence: " + newSentence2);

        ArrayList<String> possibleNextWords = getNextWords(prefixSentence, smoothing);
    }
}