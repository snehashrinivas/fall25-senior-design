package frontend.views;

import frontend.views.MainView;
import frontend.views.FeedbackView;
import frontend.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AutocompleteView {
    public static Parent create(String[] options) {
        VBox root = new VBox(12); //main vertical layout with 12 px spacing between children.
        root.setAlignment(Pos.CENTER); //center all children inside this VBox.
        root.setStyle("-fx-padding: 20;"); //add 20px padding around the VBox (inline CSS).

        Label prompt = new Label("Choose the next word:"); //prompt
        //each uses a ternary: if that option exists, use it;
        //otherwise fall back to a placeholder like "word1"
        Button b1 = new Button(options.length > 0 ? options[0] : "word1");
        Button b2 = new Button(options.length > 1 ? options[1] : "word2");
        Button b3 = new Button(options.length > 2 ? options[2] : "word3");

        //lay those three buttons in one row (horizontal), with 10px spacing.
        HBox choiceRow = new HBox(10, b1, b2, b3);
        //center the buttons within that row.
        choiceRow.setAlignment(Pos.CENTER);
        //action buttons below the choices.
        Button btnReroll = new Button("Re-Roll");
        Button btnFinish = new Button("Finish");
        //put the two action buttons on one centered row.
        HBox actionRow = new HBox(10, btnReroll, btnFinish);
        actionRow.setAlignment(Pos.CENTER);
        //when a choice is clicked, call onSelect with the chosen word.
        b1.setOnAction(e -> onSelect(b1.getText()));
        b2.setOnAction(e -> onSelect(b2.getText()));
        b3.setOnAction(e -> onSelect(b3.getText()));

        btnReroll.setOnAction(e -> {
            // TODO: fetch new options from backend and refresh the view
            //when “Re-Roll” is pressed: placeholder action for now.
            //call backend to get three new suggestions and rebuild the view.
            System.out.println("Requesting new words...");
        });

        btnFinish.setOnAction(e ->
            // TODO: finalize with backend
            //when “Finish” is pressed: go to the Feedback screen.
            //currently passes a dummy sentence
            //pass the real built sentence from backend.
            MainView.setCenter(FeedbackView.create("The quick brown fox jumps over the lazy dog."), "Feedback")
        );
        //add the prompt, choices row, and action row to the root VBox.
        root.getChildren().addAll(prompt, choiceRow, actionRow);
        return root;
    }

    private static void onSelect(String word) {
        //helper method for when a word is chosen:
        //prints which word was clicked (for debug)
        //send the selection to backend to update the sentence
        //navigates to Feedback with a placeholder sentence for now
        System.out.println("Selected word: " + word);
        // TODO: send selection to backend and update sentence
        MainView.setCenter(FeedbackView.create("The quick brown fox jumps over the lazy dog."), "Feedback");
    }
}
