/*
Written by Rida Basit. RXB210086
Displays suggested words the user can click to continue building a sentence
with reroll and finish options.
*/
package frontend.views;

import frontend.views.MainView;
import frontend.views.FeedbackView;
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
        currentSentence = "Sentence: " + firstWord;

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
            sentenceLabel.setText(currentSentence);
        });
        b2.setOnAction(e -> {
            currentSentence += " " + b2.getText();
            sentenceLabel.setText(currentSentence);
        });
        b3.setOnAction(e -> {
            currentSentence += " " + b3.getText();
            sentenceLabel.setText(currentSentence);
        });

        // When “Re-Roll” is pressed: get new word suggestions
        btnReroll.setOnAction(e -> {
            // TODO: Replace this placeholder with an API call to your backend:
            // recreate this view with those new suggestions:
            // MainView.setCenter(AutocompleteView.create(firstWord, newOptions), "Word Completion");
            String[] newOptions = getRandomSuggestions(); // mock data for now
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

    // Mock suggestion generator (replace with backend API later)
    private static String[] getRandomSuggestions() {
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
}
