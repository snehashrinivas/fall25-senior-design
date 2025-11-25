package backend;

// Class for interfacing with imported files
// Written by Andersen Breyel
public class Document {
    private String fileName;
    private int wordCount;
    private int importDate;

    public Document(String fileName, int wordCount) {

        this.fileName = fileName;
        this.wordCount = wordCount;
    }

    // we might not need this constructor if import date time is being calculated in the sql statement itself
    public Document(String fileName, int wordCount, int importDate) {

        this.fileName = fileName;
        this.wordCount = wordCount;
        this.importDate = importDate;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public int getWordCount(){
        return wordCount;
    }

    public int getImportDate() {
        return importDate;
    }

    // Setters
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public void setImportDate(int importDate) {
        this.importDate = importDate;
    }
}
