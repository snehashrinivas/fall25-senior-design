/*
Written by Rida Basit. RXB210086
Shows the final generated sentence or results after user interaction.
*/
package frontend.views;

import frontend.views.MainView;
import frontend.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FeedbackView {
    public static Parent create(String sentence) {
        // main vertical layout with 12 px spacing between children
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER); // center all children inside this VBox
        root.setStyle("-fx-padding: 20;"); // add 20px padding around the VBox

        // big app title at the top
        Label appTitle = new Label("Sentence Builder");
        appTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        // show the generated sentence
        Label lbl1 = new Label("Generated Sentence:");
        Label lblSentence = new Label(sentence); // the sentence passed in from previous screen

        // ask for feedback
        Label lbl2 = new Label("Provide Feedback:");

        // clickable 1–5 star rating control (defined in StarRating.java)
        StarRating stars = new StarRating();

        // put the star control in a centered row (handy if you add more later)
        HBox row = new HBox(6, stars);
        row.setAlignment(Pos.CENTER);

        //regenerate (go back to Home) or finish (thank-you page)
        Button btnRegenerate = new Button("Regenerate");
        Button btnFinish = new Button("Finish");

        // go back to Home so the user can try again
        btnRegenerate.setOnAction(e ->
                MainView.setCenter(HomeView.create(), "Sentence Builder - Home")
        );

        // on Finish: (optionally) get rating, then navigate to the Thank You page
        btnFinish.setOnAction(e ->
                MainView.setCenter(ThankYouView.create(), "Thank You")
        );

        // assemble the screen in order
        root.getChildren().addAll(
                appTitle,         // title
                lbl1, lblSentence,// generated sentence section
                lbl2, row,        // feedback section with stars
                btnRegenerate,    // actions
                btnFinish
        );

        return root;          // return the UI tree so SceneManager can display it
    }
}
