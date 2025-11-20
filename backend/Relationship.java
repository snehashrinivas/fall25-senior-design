package backend;
public class Relationship {
    private String currentWord;
    private String nextWord;
    private int combinationCount;

    public Relationship(String currentWord, String nextWord, int combinationCount) {
        this.currentWord = currentWord;
        this.nextWord = nextWord;
        this.combinationCount = combinationCount;
    }

    // Getters
    public String getCurrentWord() {
        return currentWord;
    }

    public String getNextWord() {
        return nextWord;
    }

    public int getCombinationCount() {
        return combinationCount;
    }

    // Setters
    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

    public void setNextWord(String nextWord) {
        this.nextWord = nextWord;
    }

    public void setCombinationCount(int combinationCount) {
        this.combinationCount = combinationCount;
    }

    public void incrementCombinationCount() {
        this.combinationCount++;
    }
}

