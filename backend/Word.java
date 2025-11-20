package backend;
/*
This class represents a single word and its properties within a sentence.
 */
public class Word {
    private String wordText;
    private boolean isStartWord;
    private boolean isEndWord;
    private int frequency;
    private String previousWord;

    // Constructor for creating a Word object, has all the attributes + previous word
    public Word(String wordText, boolean isStartWord, boolean isEndWord, int frequency, String previousWord) {
        this.wordText = wordText;
        this.isStartWord = isStartWord;
        this.isEndWord = isEndWord;
        this.frequency = frequency;
        this.previousWord = previousWord;
    }

    //
    public Word(String wordText, boolean isStartWord, boolean isEndWord, int frequency) {
        this(wordText, isStartWord, isEndWord, frequency, null);
    }

    // Getters
    public String getWordText() {
        return wordText;
    }

    public boolean isStartWord() {
        return isStartWord;
    }

    public boolean isEndWord() {
        return isEndWord;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getPreviousWord() {
        return previousWord;
    }

    // Setters
    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public void setStartWord(boolean startWord) {
        isStartWord = startWord;
    }

    public void setEndWord(boolean endWord) {
        isEndWord = endWord;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setPreviousWord(String previousWord) {
        this.previousWord = previousWord;
    }
}
