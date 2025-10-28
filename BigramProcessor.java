import java.util.*;

public class BigramProcessor {
    private final DatabaseManager db;
    

    public BigramProcessor(DatabaseManager dbManager) {
        this.db = dbManager;
    }

    

    /**
     * Sorts a HashMap of words and their probabilities by probability in descending order
     * @param unsortedMap HashMap containing words as keys and their probabilities as values
     * @return ArrayList of words sorted by their probabilities in descending order
     * Written by Sneha Shrinivas
     */
    private ArrayList<String> sortHashMap(HashMap<String, Double> unsortedMap) {
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

    
}
