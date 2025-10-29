package frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import frontend.views.HomeView;

public class FrontendMain extends Application {
    @Override
    public void start(Stage stage) {
        // hand the primary Stage to scene manager
        SceneManager.init(stage);

        // title
        SceneManager.show("Sentence Builder - Home", HomeView.create());
    }

    public static void main(String[] args) {
        // launch the JavaFX application lifecycle (calls start(..) above)
        launch(args);
    }
}
