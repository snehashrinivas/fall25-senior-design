package backend;

import java.sql.ResultSet;

/**
 * This class represents a single word within a sentence.
 * This class tracks how often a word appears within a text, as well as
 * how frequently it appears at the start or end of sentences.
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

    public Word(String wordText) {
        // query db for each field
        this.wordText = wordText;

       // ResultSet rs = DatabaseManager.getWordRow(wordText);

        this.frequency = DatabaseManager.getWordFreq(wordText);
        this.startWordCount = DatabaseManager.getWordStart(wordText);
        this.endWordCount = DatabaseManager.getWordEnd(wordText);

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
     * Sets content of this word.
     *
     * @param wordText The new word text
     */
    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    /**
     * Sets the start word count.
     *
     * @param startWordCount The new start word count
     */
    public void setStartWordCount(int startWordCount) {
        this.startWordCount = startWordCount;
    }

    /**
     * Sets the end word count.
     *
     * @param endWordCount The new end word count
     */
    public void setEndWordCount(int endWordCount) {
        this.endWordCount = endWordCount;
    }

    /**
     * Sets the total frequency of this word.
     *
     * @param frequency The new frequency count
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
