package backend;
/*
This class represents a single word and its properties within a sentence.
 */
public class Word {
    private String wordText;
    private boolean isStartWord;
    private boolean isEndWord;
    private int frequency;

    // Constructor for creating a Word object, has all the attributes + previous word
    // should nto be bools, should be counts
    public Word(String wordText, boolean isStartWord, boolean isEndWord, int frequency) {
        this.wordText = wordText;
        this.isStartWord = isStartWord;
        this.isEndWord = isEndWord;
        this.frequency = frequency;
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
}
