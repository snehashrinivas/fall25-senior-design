/*
Written by Rida Basit. RXB210086
The entry point of the JavaFX app;
initializes the stage and shows the main scene.
*/

package frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import frontend.views.HomeView;
import frontend.views.MainView;

public class FrontendMain extends Application {
    @Override
    public void start(Stage stage) {
        // hand the primary Stage to scene manager
        SceneManager.init(stage);

        // title
        SceneManager.show("Sentence Builder", MainView.create());
    }

    public static void main(String[] args) {
        // launch the JavaFX application lifecycle (calls start(..) above)
        launch(args);
    }
}
