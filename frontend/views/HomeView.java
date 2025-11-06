package frontend.views;
//import SentenceService;

import frontend.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import frontend.views.MainView;
import frontend.views.FeedbackView;
import frontend.views.AutocompleteView;
import frontend.services.SentenceService;

public class HomeView {
    public static Parent create() {
        // main vertical layout with 12px spacing
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);  // center all children
        root.setStyle("-fx-padding: 20;"); // outer padding

        // app title at the top
        Label appTitle = new Label("Sentence Builder");
        appTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        // prompt and text input
        Label title = new Label("Enter your first word:");
        TextField wordField = new TextField();
        wordField.setPromptText("Type your first word...");

        // primary actions
        Button btnGenerate = new Button("Generate Sentence");
        Button btnAuto = new Button("Auto Complete");

        // disable actions when input is empty
        btnGenerate.disableProperty().bind(wordField.textProperty().isEmpty());
        btnAuto.disableProperty().bind(wordField.textProperty().isEmpty());

        // when Generate is clicked: go to Feedback with a placeholder sentence
        // call backend with wordField.getText() to get the real sentence
        btnGenerate.setOnAction(e -> {
            String firstWord = wordField.getText().trim();
            if (!firstWord.isEmpty()) {
                Label loading = new Label("Generating sentence...");
                loading.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");
                MainView.setCenter(loading, "Generating...");
            }

            try {
                // Get the SentenceService instance
                SentenceService service = SentenceService.getInstance();

                // Generate sentence with the user's input
                String generatedSentence = service.generateSentence(firstWord);

                // Navigate to Feedback view with the generated sentence
                MainView.setCenter(FeedbackView.create(generatedSentence), "Feedback");

            } catch (Exception ex) {
                // If something goes wrong, show an error message
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace();

                Label errorLabel = new Label("Error: Could not generate sentence.\nPlease try again.");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14;");
                MainView.setCenter(errorLabel, "Error");
        }

    });

        // when Auto Complete is clicked: go to Autocomplete with 3 options
        // fetch real suggestions using firstWord
        btnAuto.setOnAction(e -> {
            String firstWord = wordField.getText().trim();
            String[] options = {"quick", "dog", "man"}; //placeholder options
            MainView.setCenter(AutocompleteView.create(options), "Word Completion");
        });

        //allow pressing Enter in the text field to trigger Generate
        wordField.setOnAction(e -> btnGenerate.fire());

        // put buttons on one row
        HBox row = new HBox(10, btnGenerate, btnAuto);
        row.setAlignment(Pos.CENTER);

        // assemble screen
        root.getChildren().addAll(appTitle, title, wordField, row);
        return root;  // return UI tree for SceneManager
    }
}
