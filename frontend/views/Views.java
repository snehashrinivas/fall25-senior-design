/*
Written by Rida Basit. RXB210086
Holds shared helper methods for creating consistent UI elements
*/
package frontend.views;

import javafx.scene.control.Label;

public class Views {
    //Creates a consistently styled title label for screens.
    public static Label title(String text) {
        Label t = new Label(text);  // create a new label with the given text
        // apply a reusable title style
        t.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");
        return t; // return the styled label to the caller
    }
}
