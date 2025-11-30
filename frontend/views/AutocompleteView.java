/*
Written by Rida Basit. RXB210086
Displays suggested words the user can click to continue building a sentence
with reroll and finish options.
*/
package frontend.views;

import frontend.views.MainView;
import frontend.views.FeedbackView;
import frontend.services.SentenceService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class AutocompleteView {

    private static String currentSentence; // track sentence locally
    //private static final Random random = new Random();
    // Full list of candidate next words for the current last word
    private static List<String> suggestionList = new ArrayList<>();

    // Index into suggestionList – where the current group of 3 starts
    private static int suggestionIndex = 0;

    // accepts both the starter word and the initial suggestion options
    // accepts both the starter word and the initial suggestion options
    // (the options parameter is now ignored; we always pull real suggestions from the backend)
    //edited by Rida Basit
    public static Parent create(String firstWord, String[] options) {
        VBox root = new VBox(12); //main vertical layout with 12 px spacing between children.
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        // initialize sentence with the starter word from HomeView (just the word)
        currentSentence = firstWord.trim();

        // Display the current sentence at top, with a label prefix
        Label sentenceLabel = new Label("Sentence: " + currentSentence);
        sentenceLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label prompt = new Label("Choose the next word:");

        // Create empty buttons for each suggestion – we will fill the text from backend
        Button b1 = new Button();
        Button b2 = new Button();
        Button b3 = new Button();

        HBox choiceRow = new HBox(10, b1, b2, b3);
        choiceRow.setAlignment(Pos.CENTER);

        Button btnReroll = new Button("Re-Roll");
        Button btnFinish = new Button("Finish");
        HBox actionRow = new HBox(10, btnReroll, btnFinish);
        actionRow.setAlignment(Pos.CENTER);

        // Load first set of suggestions from backend and fill button texts
        loadSuggestionsAndFillButtons(b1, b2, b3);

        // When a choice is clicked: append to sentence and refresh suggestions
        b1.setOnAction(e -> handleWordChoice(b1.getText(), sentenceLabel, b1, b2, b3));
        b2.setOnAction(e -> handleWordChoice(b2.getText(), sentenceLabel, b1, b2, b3));
        b3.setOnAction(e -> handleWordChoice(b3.getText(), sentenceLabel, b1, b2, b3));

        // When “Re-Roll” is pressed: show the next 3 words from the same suggestion list
        btnReroll.setOnAction(e -> {
            if (suggestionList.isEmpty()) {
                // If we somehow have no list, reload from backend
                loadSuggestionsAndFillButtons(b1, b2, b3);
            } else {
                // Move the starting index forward by 3
                suggestionIndex += 3;

                // If we go past the end, wrap around to the beginning
                if (suggestionIndex >= suggestionList.size()) {
                    suggestionIndex = 0;
                }

                applyCurrentThree(b1, b2, b3);
            }
        });

        // When “Finish” is pressed: go to FeedbackView with full sentence
        btnFinish.setOnAction(e -> {
            MainView.setCenter(FeedbackView.create(currentSentence), "Feedback");
            // Reset static state for the next session
            currentSentence = null;
            suggestionList.clear();
            suggestionIndex = 0;
        });

        root.getChildren().addAll(sentenceLabel, prompt, choiceRow, actionRow);
        return root;
    }

    /**
     * Handle when the user clicks one of the suggestion buttons.
     * We append the chosen word to the sentence and then reload suggestions
     * based on the new last word.
     * Written by Rida Basit
     */
    // This method handles what happens when the user clicks one of the suggestion buttons
    private static void handleWordChoice(String chosenWord,
                                         Label sentenceLabel,
                                         Button b1, Button b2, Button b3) {
        // If the chosen word is null or just spaces, do nothing and exit the method
        if (chosenWord == null || chosenWord.isBlank()) {
            return;
        }
        // Add a space and the chosen word to the current sentence text
        currentSentence = currentSentence + " " + chosenWord;
        // Update the label on the screen to show the new full sentence
        sentenceLabel.setText("Sentence: " + currentSentence);

        // After adding a word, ask for new suggestions based on the updated sentence
        // and update the three suggestion buttons
        loadSuggestionsAndFillButtons(b1, b2, b3);
    }

    /**
     * Ask SentenceService for the full sorted list of next-word suggestions
     * and then show the first 3 on the buttons.
     * Written by Rida Basit
     */
    private static void loadSuggestionsAndFillButtons(Button b1, Button b2, Button b3) {
        // Get the shared SentenceService instance (singleton)
        SentenceService service = SentenceService.getInstance();
        // Ask the service for a list of next-word suggestions for the current sentence
        suggestionList = service.getNextWordSuggestions(currentSentence);
        // Start from the beginning of the suggestion list (index 0)
        suggestionIndex = 0;
        // Put the first 3 suggestions (if they exist) onto the buttons
        applyCurrentThree(b1, b2, b3);
    }

    /**
     * Put the current group of up to 3 words onto the buttons.
     * If there are fewer than 3 suggestions left, we show a simple placeholder.
     * Written by Rida Basit
     */
    private static void applyCurrentThree(Button b1, Button b2, Button b3) {
        // Text to show if there is no suggestion for a button
        String placeholder = "-";

        // For button 1: if the list has a word at suggestionIndex, use it; otherwise show placeholder
        b1.setText(suggestionList.size() > suggestionIndex
                ? suggestionList.get(suggestionIndex)
                : placeholder);
        // For button 2: if the list has a word at suggestionIndex + 1, use it; otherwise show placeholder
        b2.setText(suggestionList.size() > suggestionIndex + 1
                ? suggestionList.get(suggestionIndex + 1)
                : placeholder);
        // For button 3: if the list has a word at suggestionIndex + 2, use it; otherwise show placeholder
        b3.setText(suggestionList.size() > suggestionIndex + 2
                ? suggestionList.get(suggestionIndex + 2)
                : placeholder);
    }
}
