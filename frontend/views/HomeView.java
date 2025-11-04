package frontend.views;

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
        Button btnAuto     = new Button("Auto Complete");

        // disable actions when input is empty
        btnGenerate.disableProperty().bind(wordField.textProperty().isEmpty());
        btnAuto.disableProperty().bind(wordField.textProperty().isEmpty());

        // when Generate is clicked: go to Feedback with a placeholder sentence
        // call backend with wordField.getText() to get the real sentence
        btnGenerate.setOnAction(e -> {
            String firstWord = wordField.getText().trim();
            String sentence  = firstWord.isEmpty()
                    ? "The quick brown fox jumps over the lazy dog." //placeholder for now
                    : firstWord + " ... (generated continuation)";
            MainView.setCenter(FeedbackView.create(sentence), "Feedback");
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
