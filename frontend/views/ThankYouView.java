/*
Written by Rida Basit and Sneha Shrinivas. RXB210086, sxs210371
Displays a thank you or completion message
after finishing sentence generation or feedback.
 */
package frontend.views;
import frontend.views.MainView;
import frontend.views.HomeView;
import frontend.views.Views;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ThankYouView {

    public static Parent create() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: " + Views.APP_BG + ";" +
                        "-fx-padding: 32;"
        );

        Label title = Views.title("Sentence Builder");
        Label msg   = new Label("Thank you for using Sentence Builder!");
        msg.setStyle("-fx-font-size: 16; -fx-text-fill: " + Views.TEXT_DEFAULT + ";");

        Label sub = new Label("You can go back and create another sentence anytime.");
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: " + Views.TEXT_MUTED + ";");

        Button back = Views.primaryButton("Back to Home");
        back.setDefaultButton(true);
        back.setOnAction(e ->
                MainView.setCenter(HomeView.create(), "Sentence Generation")
        );

        VBox card = Views.card(
                title,
                msg,
                sub,
                back
        );
        card.setSpacing(12);
        card.setMaxWidth(420);

        root.getChildren().add(card);
        return root;
    }
}
