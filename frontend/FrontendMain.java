/**
This forms the entry point of the JavaFX app; it initializes the stage and shows the main scene.
 Written by Rida Basit, Sneha Shrinivas, and Khushi Dubey. RXB210086, sxs210371, kad210008
*/

package frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import frontend.views.HomeView;
import frontend.views.MainView;
import frontend.services.SentenceService; // import Sentence Service

public class FrontendMain extends Application {
    @Override
    public void start(Stage stage) {
        // Initialize the SentenceService (backend) before showing UI
        try {
            SentenceService.initialize();
            System.out.println("Backend initialized successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize backend: " + e.getMessage());
            e.printStackTrace();
            // Show error to user (optional: add a JavaFX Alert dialog here)
        }
        // hand the primary Stage to scene manager
        SceneManager.init(stage);

        // title
        SceneManager.show("Sentence Builder", MainView.create());
    }

    @Override
    public void stop() {
        // Clean up database connections when app closes
        SentenceService.shutdown();
        System.out.println("Application shutting down...");
    }

    public static void main(String[] args) {
        // launch the JavaFX application lifecycle (calls start(..) above)
        launch(args);
    }
}
