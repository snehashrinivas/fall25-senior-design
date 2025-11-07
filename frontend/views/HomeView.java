/*
Written by Rida Basit. RXB210086
The main screen where the user starts building a sentence,
now using a dropdown to select a starting word.
*/
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
import javafx.scene.control.ComboBox;

public class HomeView {
    public static Parent create() {
        // main vertical layout with 12px spacing
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);  // center all children
        root.setStyle("-fx-padding: 20;"); // outer padding

        // app title at the top
        Label appTitle = new Label("Sentence Builder");
        appTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        // prompt and dropdown menu
        Label title = new Label("Choose your first word:");
        ComboBox<String> wordDropdown = new ComboBox<>();
        wordDropdown.getItems().addAll(
                "The", "A", "My", "This", "Our", "Your", "His", "Her", "Their"
        );
        wordDropdown.setPromptText("Select your first word");

        // primary actions
        Button btnGenerate = new Button("Generate Sentence");
        Button btnAuto     = new Button("Word Completion"); //autocomplete

        // disable actions when input is empty
        btnGenerate.disableProperty().bind(wordDropdown.valueProperty().isNull());
        btnAuto.disableProperty().bind(wordDropdown.valueProperty().isNull());

        // when Generate is clicked: go to Feedback with a placeholder sentence
        // call backend with wordField.getText() to get the real sentence
        btnGenerate.setOnAction(e -> {
            String firstWord = wordDropdown.getValue();
            String sentence  = (firstWord == null || firstWord.trim().isEmpty())
                    ? "The quick brown fox jumps over the lazy dog." // placeholder
                    : firstWord + " ... (generated sentence)";
            MainView.setCenter(FeedbackView.create(sentence), "Feedback");
        });

        // when Auto Complete is clicked: go to Autocomplete with 3 options
        // fetch real suggestions using firstWord
        btnAuto.setOnAction(e -> {
            String firstWord = wordDropdown.getValue();
            String[] options = {"quick", "dog", "man"}; // placeholder options
            MainView.setCenter(AutocompleteView.create(firstWord, options), "Word Completion");
        });


        //allow pressing Enter in the text field to trigger Generate
        wordDropdown.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER:
                    if (wordDropdown.isEditable() && wordDropdown.getValue() != null)
                        btnGenerate.fire();
                    break;
                default:
                    break;
            }
        });


        // put buttons on one row
        HBox row = new HBox(10, btnGenerate, btnAuto);
        row.setAlignment(Pos.CENTER);

        // assemble screen
        root.getChildren().addAll(appTitle, title, wordDropdown, row);
        return root;  // return UI tree for SceneManager
    }
}
