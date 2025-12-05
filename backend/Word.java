package backend;

/**
 * This class represents a single word within a sentence.
 * This class tracks how often a word appears within a text, as well as
 * how frequently it appears at the start or end of sentences.
 *
 * Written by Ezzah and Khushi
 */
public class Word {
    private String wordText;
    private int startWordCount;
    private int endWordCount;
    private int frequency;

    /**
     * Constructor for creating a Word object.
     *
     * @param wordText Contents of a word
     * @param startWordCount count of appearances at sentence start
     * @param endWordCount count of appearances at sentence end
     * @param frequency total frequency count
     */
    public Word(String wordText, int startWordCount, int endWordCount, int frequency) {
        this.wordText = wordText;
        this.startWordCount = startWordCount;
        this.endWordCount = endWordCount;
        this.frequency = frequency;
    }

    // Getters

    /**
     * Gets the total character content of this word.
     *
     * @return The word text
     */
    public String getWordText() {
        return wordText;
    }

    /**
     * Gets the count of how many times this word appears at the start of sentences.
     *
     * @return The start word count
     */
    public int getStartWordCount() {
        return startWordCount;
    }

    /**
     * Gets the count of how many times this word appears at the end of sentences.
     *
     * @return The end word count
     */
    public int getEndWordCount() {
        return endWordCount;
    }

    /**
     * Gets the total frequency of this word in the text.
     *
     * @return The frequency count
     */
    public int getFrequency() {
        return frequency;
    }

    // Setters
    /**
     * Sets the total frequency of this word.
     *
     * @param frequency The new frequency count
     */
    /*public void setFrequency(int frequency) {
        this.frequency = frequency;
    }*/
}
