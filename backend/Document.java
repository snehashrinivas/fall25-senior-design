package backend;

/**
 * Class for interfacing with imported files
 * Written by Andersen Breyel
 */

public class Document {
    private final String fileName;
    private final int wordCount;

    public Document(String fileName, int wordCount) {

        this.fileName = fileName;
        this.wordCount = wordCount;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public int getWordCount(){
        return wordCount;
    }
}
