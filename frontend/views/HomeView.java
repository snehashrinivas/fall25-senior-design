/*
Written by Rida Basit and Sneha Shrinivas. RXB210086, sxs210371
edited by Ezzah eaq210000
The main screen where the user starts building a sentence,
now using a dropdown to select a starting word and then either selecting the options of sentence generation or
word completion.
*/

package frontend.views;
import frontend.views.Views;
import frontend.views.MainView;
import frontend.views.FeedbackView;
import frontend.views.AutocompleteView;
import frontend.SceneManager;
import backend.DatabaseManager;

import frontend.services.SentenceService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeView {

    public static Parent create() {
        // full-screen background
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: " + Views.APP_BG + ";" +
                        "-fx-padding: 32;"
        );

        // Title + subtitle
        Label appTitle = Views.title("Sentence Builder");

        Label subtitle = new Label("Start by choosing your first word and let the app help you build a sentence.");
        subtitle.setStyle(
                "-fx-text-fill: " + Views.TEXT_MUTED + ";" +
                        "-fx-font-size: 13;"
        );

        // Prompt and dropdown menu
        Label title = new Label("Choose your first word:");
        title.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: " + Views.TEXT_DEFAULT + ";"
        );

        ComboBox<String> wordDropdown = new ComboBox<>();

        // Load top 10 starting words from the database
        List<String> startingWords = new ArrayList<>();
        //DatabaseManager db = DatabaseManager.getInstance();
        //startingWords = db.getTopStartingWords(10);
        startingWords = DatabaseManager.getTopStartingWords(10);

        // If DB returned nothing, you can optionally fall back to a default list
        if (startingWords.isEmpty()) {
            startingWords = List.of("A", "Her", "His", "My",  "Our", "The", "Their", "This",  "Your");
        }

        // Alphabetize them for the dropdown
        Collections.sort(startingWords, String.CASE_INSENSITIVE_ORDER);

        /// 2) Capitalize for display (so we see “He”, “I”, “My”, etc.)
        List<String> displayWords = new ArrayList<>();
        for (String w : startingWords) {
            if (w == null || w.isBlank()) continue;
            String lower = w.toLowerCase();
            if (lower.equals("i")) {
                displayWords.add("I");
            } else {
                displayWords.add(Character.toUpperCase(lower.charAt(0)) + lower.substring(1));
            }
        }

        // Put the pretty versions into the dropdown
        wordDropdown.getItems().setAll(displayWords);

        wordDropdown.setPromptText("Select your first word");
        wordDropdown.setEditable(false);
        wordDropdown.setPrefWidth(240);
        wordDropdown.setStyle(
                "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-padding: 4 10 4 10;"
        );

        // primary actions (use shared button styles)
        Button btnGenerate = Views.primaryButton("Generate Sentence");
        Button btnAuto     = Views.secondaryButton("Word Completion");

        // disable actions when input is empty
        btnGenerate.disableProperty().bind(wordDropdown.valueProperty().isNull());
        btnAuto.disableProperty().bind(wordDropdown.valueProperty().isNull());

        // Generate Sentence
        btnGenerate.setOnAction(e -> {
            String firstWord = wordDropdown.getValue();
            if (firstWord == null) return;
            firstWord = firstWord.trim();
            if (firstWord.isEmpty()) return;

            Label loading = new Label("Generating sentence...");
            loading.setStyle("-fx-font-size: 14; -fx-text-fill: " + Views.TEXT_MUTED + ";");
            MainView.setCenter(loading, "Generating...");

            try {
                SentenceService service = SentenceService.getInstance();
                String generatedSentence = service.generateSentence(firstWord);
                MainView.setCenter(FeedbackView.create(generatedSentence), "Feedback");
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace();
                Label errorLabel = new Label("Error: Could not generate sentence.\nPlease try again.");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
                MainView.setCenter(errorLabel, "Error");
            }
        });

        // Word completion
        btnAuto.setOnAction(e -> {
            String firstWord = wordDropdown.getValue();
            if (firstWord == null || firstWord.trim().isEmpty()) return;

            String[] options = {"quick", "dog", "man"}; // placeholder options
            MainView.setCenter(AutocompleteView.create(firstWord, options), "Word Completion");
        });

        // Allow Enter to trigger Generate
        wordDropdown.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && wordDropdown.getValue() != null) {
                btnGenerate.fire();
            }
        });

        // buttons row
        HBox row = new HBox(10, btnGenerate, btnAuto);
        row.setAlignment(Pos.CENTER);

        // build card content
        VBox card = Views.card(
                appTitle,
                subtitle,
                new Label(""),
                title,
                wordDropdown,
                row
        );
        card.setMaxWidth(420);

        root.getChildren().add(card);
        return root;
    }
}
