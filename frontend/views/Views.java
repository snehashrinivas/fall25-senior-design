/**
Holds shared helper methods for creating consistent UI elements
 Written by Rida Basit and Sneha Shrinivas. RXB210086, sxs210371
*/
package frontend.views;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Views {

    // Simple design tokens
    public static final String APP_BG       = "#f3f4f6";   // light gray background
    public static final String CARD_BG      = "#ffffff";   // white card
    public static final String ACCENT       = "#2563eb";   // blue
    public static final String ACCENT_SOFT  = "#dbeafe";   // soft blue
    public static final String TEXT_MUTED   = "#6b7280";   // gray text
    public static final String TEXT_DEFAULT = "#111827";   // dark text

    /**
     * Creates a consistently styled title label for screens.
     * @param text
     * @return the stylized label button
     * Written by Sneha Shrinivas and Rida Basit
     */
    // Creates a consistently styled title label for screens.
    public static Label title(String text) {
        Label t = new Label(text);
        t.setStyle(
                "-fx-font-size: 22;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + TEXT_DEFAULT + ";"
        );
        return t;
    }

    /**
    Creates a primary button with filled styling.
    @param text the button text
    @return the stylized button
    Written by Sneha Shrinivas
     */
    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + ACCENT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13;" +
                        "-fx-font-weight: 600;" +
                        "-fx-padding: 8 18 8 18;" +
                        "-fx-background-radius: 999;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e ->
                b.setStyle(
                        "-fx-background-color: #1d4ed8;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 13;" +
                                "-fx-font-weight: 600;" +
                                "-fx-padding: 8 18 8 18;" +
                                "-fx-background-radius: 999;" +
                                "-fx-cursor: hand;"
                )
        );
        b.setOnMouseExited(e ->
                b.setStyle(
                        "-fx-background-color: " + ACCENT + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 13;" +
                                "-fx-font-weight: 600;" +
                                "-fx-padding: 8 18 8 18;" +
                                "-fx-background-radius: 999;" +
                                "-fx-cursor: hand;"
                )
        );
        return b;
    }

    /**
     * Creates a secondary button with outlined styling.
     * @param text the button text
     * @return the stylized button
     * Written by Sneha Shrinivas
     */
    public static Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT_DEFAULT + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-padding: 8 16 8 16;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e ->
                b.setStyle(
                        "-fx-background-color: #f9fafb;" +
                                "-fx-text-fill: " + TEXT_DEFAULT + ";" +
                                "-fx-font-size: 13;" +
                                "-fx-padding: 8 16 8 16;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: #9ca3af;" +
                                "-fx-border-width: 1;" +
                                "-fx-cursor: hand;"
                )
        );
        b.setOnMouseExited(e ->
                b.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + TEXT_DEFAULT + ";" +
                                "-fx-font-size: 13;" +
                                "-fx-padding: 8 16 8 16;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: #d1d5db;" +
                                "-fx-border-width: 1;" +
                                "-fx-cursor: hand;"
                )
        );
        return b;
    }

    /**
     * Creates a card container in the center of the screen with consistent styling.
     * @param children
     * @return the styled VBox card
     * Written by Sneha Shrinivas
     */
    public static VBox card(Node... children) {
        VBox card = new VBox(12);
        card.getChildren().addAll(children);
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 18, 0, 0, 6);"
        );
        return card;
    }
}
