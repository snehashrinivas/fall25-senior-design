/*
Written by Rida Basit. RXB210086
Shows the final generated sentence or results after user interaction.
*/
package frontend.views;
import frontend.views.Views;
import frontend.views.MainView;
import frontend.views.HomeView;
import frontend.views.ThankYouView;
import frontend.views.StarRating;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FeedbackView {

    public static Parent create(String sentence) {
        // full background
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: " + Views.APP_BG + ";" +
                        "-fx-padding: 32;"
        );

        // title + helper text
        Label appTitle = Views.title("Sentence Builder");
        Label subtitle = new Label("Here’s the sentence we generated based on your choices.");
        subtitle.setStyle("-fx-text-fill: " + Views.TEXT_MUTED + "; -fx-font-size: 13;");

        Label lbl1 = new Label("Generated Sentence");
        lbl1.setStyle("-fx-font-size: 14; -fx-font-weight: 600;");

        Label lblSentence = new Label(sentence);
        lblSentence.setWrapText(true);
        lblSentence.setStyle(
                "-fx-font-size: 15;" +
                        "-fx-padding: 14;" +
                        "-fx-background-color: " + Views.ACCENT_SOFT + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #bfdbfe;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );
        lblSentence.setMaxWidth(420);

        Label lbl2 = new Label("Rate this sentence:");
        lbl2.setStyle("-fx-font-size: 13; -fx-text-fill: " + Views.TEXT_MUTED + ";");

        StarRating stars = new StarRating();
        HBox ratingRow = new HBox(stars);
        ratingRow.setAlignment(Pos.CENTER);

        Button btnRegenerate = Views.secondaryButton("Regenerate");
        Button btnFinish     = Views.primaryButton("Finish");

        btnRegenerate.setOnAction(e ->
                MainView.setCenter(HomeView.create(), "Sentence Builder - Home")
        );

        btnFinish.setOnAction(e -> {
            int rating = stars.getRating();
            System.out.println("User rated the sentence: " + rating + " stars");
            System.out.println("Generated sentence was: " + sentence);
            MainView.setCenter(ThankYouView.create(), "Thank You");
        });

        HBox buttonRow = new HBox(10, btnRegenerate, btnFinish);
        buttonRow.setAlignment(Pos.CENTER);

        VBox card = Views.card(
                appTitle,
                subtitle,
                new Label(""),
                lbl1,
                lblSentence,
                lbl2,
                ratingRow,
                buttonRow
        );
        card.setMaxWidth(480);

        root.getChildren().add(card);
        return root;
    }
}
