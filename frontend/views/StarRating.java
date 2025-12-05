/**
Provides a rating component (user feedback) with clickable stars.
 Written by Rida Basit. RXB210086
*/
package frontend.views;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

public class StarRating extends HBox {
    private final IntegerProperty rating = new SimpleIntegerProperty(0); // observable
    private final Button[] stars = new Button[5];
    private final int fontSize;

    public StarRating() { this(0, 22); } // default: 0 stars, 22px
    public StarRating(int initialRating) { this(initialRating, 22); }
    public StarRating(int initialRating, int fontSizePx) {
        this.fontSize = fontSizePx;
        setAlignment(Pos.CENTER);
        setSpacing(6);
        setFocusTraversable(true); // enable keyboard focus

        for (int i = 0; i < 5; i++) {
            final int value = i + 1;
            Button b = new Button("☆");
            b.setFocusTraversable(false); // focus stays on the whole control
            b.setOnAction(e -> setRating(value));
            stars[i] = b;
        }
        getChildren().addAll(stars);

        // react to rating changes and refresh UI
        rating.addListener((obs, oldV, newV) -> refresh());
        setRating(initialRating);  // clamp and initial paint

        // simple keyboard controls
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.RIGHT) setRating(getRating() + 1);
            else if (e.getCode() == KeyCode.LEFT) setRating(getRating() - 1);
            else if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                // noop or hook a “confirm” if you want
            }
        });
    }

    public int getRating() { return rating.get(); }
    public void setRating(int value) { rating.set(Math.max(0, Math.min(5, value))); }
    public IntegerProperty ratingProperty() { return rating; } // bind/observe

    public void clear() { setRating(0); }

    // rendering
    private void refresh() {
        for (int i = 0; i < 5; i++) {
            if (i < getRating()) {
                stars[i].setText("★");
                stars[i].setStyle(selectedStyle());
            } else {
                stars[i].setText("☆");
                stars[i].setStyle(unselectedStyle());
            }
        }
    }

    private String base()            { return "-fx-background-color: transparent; -fx-font-size: " + fontSize + "px;"; }
    private String selectedStyle()   { return base() + "-fx-text-fill: #F5C518;"; } // gold
    private String unselectedStyle() { return base() + "-fx-text-fill: #BDBDBD;"; } // gray
}
