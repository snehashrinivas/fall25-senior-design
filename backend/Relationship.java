package backend;
/**
 * This class represents a relationship between two consecutive words in a sentence.
 * It tracks the frequency of a specific word following another word, which is vital to
 * our autocomplete and sentence generation algorithms.
 * written by Ezzah and Khushi
 */
public class Relationship {
    private final int currentWordID;
    private final int nextWordID;
    private final int combinationCount;

    /**
     * Constructor for creating a Relationship between two words.
     *
     * @param currentWordID ID of the current word
     * @param nextWordID ID of the subsequent word after the current word
     * @param combinationCount count of how many times this pair of words occurs
     * written by Ezzah and Khushi
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
     * written by Ezzah and Khushi
     */
    public int getCurrentWordID() { return currentWordID; }

    /**
     * Gets the ID of the next word in this relationship.
     *
     * @return The next word's ID
     * written by Ezzah and Khushi
     */
    public int getNextWordID() {
        return nextWordID;
    }

    /**
     * Gets the count of how many times this word combination appears.
     *
     * @return The combination count
     * written by Ezzah and Khushi
     */
    public int getCombinationCount() {
        return combinationCount;
    }
}

