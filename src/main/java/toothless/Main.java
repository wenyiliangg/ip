package toothless;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Displays the Toothless JavaFX application.
 */
public class Main extends Application {
    /**
     * Creates a JavaFX application that can be instantiated by the runtime.
     */
    public Main() {
    }

    @Override
    public void start(Stage stage) {
        Label welcomeLabel = new Label("Toothless is getting ready for an adventure!");
        StackPane root = new StackPane(welcomeLabel);
        Scene scene = new Scene(root, 520, 680);

        stage.setTitle("Toothless");
        stage.setMinWidth(420);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }
}
