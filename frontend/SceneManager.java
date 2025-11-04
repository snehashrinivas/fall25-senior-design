
package frontend;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    // single, shared window for the whole app
    private static Stage stage;

    // initialize the manager with the app's primary Stage
    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setWidth(420);   // default window width
        stage.setHeight(480);  // default window height
    }

    // replace the current screen with a new one
    public static void show(String title, Parent root) {
        stage.setTitle(title);            // set the window title
        stage.setScene(new Scene(root));  // wrap the root node in a Scene and set it
        stage.show();                     // make sure the window is visible
    }
}
