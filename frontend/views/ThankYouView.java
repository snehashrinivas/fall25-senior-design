/*
Written by Rida Basit. RXB210086
Displays a thank you or completion message
after finishing sentence generation or feedback.
 */
package frontend.views;

import frontend.views.MainView;
import frontend.views.HomeView;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class ThankYouView {
    public static Parent create() {
        // main vertical layout with 18px spacing between children
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER); // center all children
        root.setStyle("-fx-padding: 24;"); // outer padding

        // app title at the top
        Label title = new Label("Sentence Builder");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        // thank you message
        Label msg = new Label("Thank you for using Sentence Builder!");
        msg.setStyle("-fx-font-size: 18;");

        // return to Home screen
        Button back = new Button("Back to Home");
        back.setDefaultButton(true);           // pressing Enter activates this
        back.setOnAction(e ->
                MainView.setCenter(HomeView.create(), "Sentence Generation")
        );
        // assemble the screen
        root.getChildren().addAll(title, msg, back);
        return root;
    }
}
