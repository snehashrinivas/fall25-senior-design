/*
Written by Rida Basit. RXB210086
Displays suggested words the user can click to continue building a sentence
with reroll and finish options.
*/
package frontend.views;

import frontend.views.MainView;
import frontend.views.FeedbackView;
import backend.DatabaseManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Random;

public class AutocompleteView {

    private static String currentSentence; // track sentence locally
    private static final Random random = new Random();

    // accepts both the starter word and the initial suggestion options
    public static Parent create(String firstWord, String[] options) {
        VBox root = new VBox(12); //main vertical layout with 12 px spacing between children.
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        // initialize sentence with the starter word from HomeView
        if (currentSentence == null) {
            currentSentence = "Sentence: " + firstWord;
        }


        // Display the current sentence at top
        Label sentenceLabel = new Label(currentSentence);
        sentenceLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label prompt = new Label("Choose the next word:");

        // Create buttons for each suggestion
        Button b1 = new Button(options.length > 0 ? options[0] : "word1");
        Button b2 = new Button(options.length > 1 ? options[1] : "word2");
        Button b3 = new Button(options.length > 2 ? options[2] : "word3");

        HBox choiceRow = new HBox(10, b1, b2, b3);
        choiceRow.setAlignment(Pos.CENTER);

        Button btnReroll = new Button("Re-Roll");
        Button btnFinish = new Button("Finish");
        HBox actionRow = new HBox(10, btnReroll, btnFinish);
        actionRow.setAlignment(Pos.CENTER);

        // When a choice is clicked: append to sentence
        b1.setOnAction(e -> {
            currentSentence += " " + b1.getText();
            String[] newOptions = getNextWordSuggestions(currentSentence);
            MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
        });
        b2.setOnAction(e -> {
            currentSentence += " " + b2.getText();
            String[] newOptions = getNextWordSuggestions(currentSentence);
            MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
        });
        b3.setOnAction(e -> {
            currentSentence += " " + b3.getText();
            String[] newOptions = getNextWordSuggestions(currentSentence);
            MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
        });


        // When “Re-Roll” is pressed: get new word suggestions
        btnReroll.setOnAction(e -> {
            // TODO: Replace this placeholder with an API call to your backend:
            // recreate this view with those new suggestions:
            // MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
            String[] newOptions = getRandomSuggestions();
            MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
        });

        // When “Finish” is pressed: go to FeedbackView with full sentence
        btnFinish.setOnAction(e -> {
            // TODO: send the final sentence to backend for processing:
            // MainView.setCenter(FeedbackView.create(result), "Feedback");
            MainView.setCenter(FeedbackView.create(currentSentence), "Feedback");
            currentSentence = null; // reset for next time
        });

        root.getChildren().addAll(sentenceLabel, prompt, choiceRow, actionRow);
        return root;
    }
/*
    This method pulls the top starting words from the database and returns
   up to 3 random suggestions. If the database is empty or an error occurs,
   it falls back to hard-coded word sets to ensure execution continuity.
   Written by Sneha Shrinivas.
 */
    private static String[] getRandomSuggestions() {
        ArrayList<String> topWords = new ArrayList<>();

        try {
            DatabaseManager db = DatabaseManager.getInstance();
            // get top 10 starting words from DB
            topWords = db.getTopStartingWords(10);
        } catch (SQLException e) {
            System.err.println("Error connecting to DB in AutocompleteView: " + e.getMessage());
        }

        // Fallback: if DB is empty or error, use the old hard-coded sets
        if (topWords == null || topWords.isEmpty()) {
            String[][] sets = {
                    {"quick", "lazy", "sleepy"},
                    {"happy", "sad", "excited"},
                    {"dog", "cat", "fox"},
                    {"bright", "dark", "colorful"},
                    {"runs", "jumps", "eats"},
                    {"beautiful", "strong", "funny"}
            };
            return sets[random.nextInt(sets.length)];
        }

        // Shuffle and pick up to 3 suggestions from the top words
        Collections.shuffle(topWords, random);
        int count = Math.min(3, topWords.size());
        String[] suggestions = new String[count];

        for (int i = 0; i < count; i++) {
            suggestions[i] = topWords.get(i);
        }

        return suggestions;
    }

    private static String[] getNextWordSuggestions(String currentSentence) {
        String[] words = currentSentence.replace("Sentence: ", "").split(" ");
        String lastWord = words[words.length - 1];

        ArrayList<String> nextWords = new ArrayList<>();
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            nextWords = db.getTopNextWords(lastWord, 10); // or 3
        } catch (SQLException e) {
            System.err.println("Error getting next words from DB: " + e.getMessage());
        }

        if (nextWords == null || nextWords.isEmpty()) {
            return new String[] {"the", "and", "to"}; // fallback
        }

        int count = Math.min(3, nextWords.size());
        String[] suggestions = new String[count];
        for (int i = 0; i < count; i++) {
            suggestions[i] = nextWords.get(i);
        }
        return suggestions;
    }


}
