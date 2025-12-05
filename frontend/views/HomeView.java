/**
The main screen where the user starts building a sentence,
now using a dropdown to select a starting word and then either selecting the options of sentence generation or
word completion.
 Written by Rida Basit and Sneha Shrinivas. RXB210086, sxs210371, edited by Ezzah Qureshi eaq210000
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeView {

    /**
     * Creates the Home view UI where users can select a starting word
     * and choose the sentence generation algorithm.
     * @return Parent node containing the Home view UI.
     * Written by Rida Basit and Sneha Shrinivas, edited by Ezzah Qureshi.
     */
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

        startingWords = DatabaseManager.getTopStartingWords(10);

        // If DB returned nothing, default list fallback
        if (startingWords.isEmpty()) {
            startingWords = List.of("A", "Her", "His", "My",  "Our", "The", "Their", "This",  "Your");
        }

        // Alphabetize them for the dropdown
        Collections.sort(startingWords, String.CASE_INSENSITIVE_ORDER);

        // Capitalize for display purposes in dropdown
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

        // Put the edited versions into the dropdown
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

        // Algorithm Selection with Radio Buttons
        Label algorithmLabel = new Label("Choose generation algorithm:");
        algorithmLabel.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: " + Views.TEXT_DEFAULT + ";"
        );

        // Create ToggleGroup for radio buttons (mutually exclusive selection)
        ToggleGroup algorithmGroup = new ToggleGroup();

        // Create three radio buttons for each algorithm
        RadioButton radioWeighted = new RadioButton("Weighted (Probability-based)");
        radioWeighted.setToggleGroup(algorithmGroup);
        radioWeighted.setSelected(true); // Default selection
        radioWeighted.setStyle("-fx-font-size: 12; -fx-text-fill: " + Views.TEXT_DEFAULT + ";");

        RadioButton radioThreeRandom = new RadioButton("Three Random (Top 3 Random)");
        radioThreeRandom.setToggleGroup(algorithmGroup);
        radioThreeRandom.setStyle("-fx-font-size: 12; -fx-text-fill: " + Views.TEXT_DEFAULT + ";");

        RadioButton radioTopOne = new RadioButton("Top One (Deterministic)");
        radioTopOne.setToggleGroup(algorithmGroup);
        radioTopOne.setStyle("-fx-font-size: 12; -fx-text-fill: " + Views.TEXT_DEFAULT + ";");

        // Create a VBox to hold the radio buttons
        VBox radioBox = new VBox(8, radioWeighted, radioThreeRandom, radioTopOne);
        radioBox.setStyle("-fx-padding: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-border-width: 1;");

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
                //String generatedSentence = service.generateSentence(firstWord);

                // Determine which algorithm to use based on selected radio button
                String generatedSentence;
                if (radioWeighted.isSelected()) {
                    generatedSentence = service.generateSentenceWeighted(firstWord);
                } else if (radioThreeRandom.isSelected()) {
                    generatedSentence = service.generateSentenceThreeRandom(firstWord);
                } else {
                    generatedSentence = service.generateSentenceTopOne(firstWord);
                }

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
                new Label(""),
                algorithmLabel,
                radioBox,
                new Label(""),
                row
        );
        card.setMaxWidth(420);

        root.getChildren().add(card);
        return root;
    }
}
