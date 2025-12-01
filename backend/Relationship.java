package backend;
/**
 * This class represents a relationship between two consecutive words in a sentence.
 * It tracks the frequency of a specific word following another word, which is vital to
 * our autocomplete and sentence generation algorithms.
 */
public class Relationship {
    private int currentWordID;
    private int nextWordID;
    private int combinationCount;

    /**
     * Constructor for creating a Relationship between two words.
     *
     * @param currentWordID ID of the current word
     * @param nextWordID ID of the subsequent word after the current word
     * @param combinationCount count of how many times this pair of words occurs
     */
    public Relationship(int currentWordID, int nextWordID, int combinationCount) {
        this.currentWordID = currentWordID;
        this.nextWordID = nextWordID;
        this.combinationCount = combinationCount;
    }

    // Getters

    /**
     * Gets the ID of the current word in this relationship.
     *
     * @return The current word's ID
     */
    public int getCurrentWordID() { return currentWordID; }

    /**
     * Gets the ID of the next word in this relationship.
     *
     * @return The next word's ID
     */
    public int getNextWordID() {
        return nextWordID;
    }

    /**
     * Gets the count of how many times this word combination appears.
     *
     * @return The combination count
     */
    public int getCombinationCount() {
        return combinationCount;
    }

    // Setters
    /**
     * Sets the ID of the current word.
     *
     * @param currentWordId The new current word ID
     */
    public void setCurrentWordID(int currentWordId) {
        this.currentWordID = currentWordId;
    }

    /**
     * Sets the ID of the next word.
     *
     * @param nextWordID The new next word ID
     */
    public void setNextWordID(int nextWordID) {
        this.nextWordID = nextWordID;
    }

    /**
     * Sets the combination count for this word pair.
     *
     * @param combinationCount The new combination count
     */
    public void setCombinationCount(int combinationCount) {
        this.combinationCount = combinationCount;
    }

    /**
     * Increments the combination count by 1.
     * Call this method each time this word pair is encountered in the text.
     */
    public void incrementCombinationCount() {
        this.combinationCount++;
    }
}

