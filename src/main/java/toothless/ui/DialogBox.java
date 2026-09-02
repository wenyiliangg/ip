package toothless.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Displays one chat message beside its speaker's avatar.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box from its reusable FXML layout.
     *
     * @param text message to display.
     * @param image avatar belonging to the speaker.
     */
    private DialogBox(String text, Image image) {
        URL dialogResource = Objects.requireNonNull(
                DialogBox.class.getResource("/view/DialogBox.fxml"),
                "DialogBox.fxml must be available on the classpath");
        FXMLLoader fxmlLoader = new FXMLLoader(dialogResource);
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load a Toothless dialog box", exception);
        }

        dialog.setText(text);
        displayPicture.setImage(image);
    }

    /**
     * Creates a right-aligned message written by the user.
     *
     * @param text message to display.
     * @param image user's avatar.
     * @return configured user dialog box
     */
    public static DialogBox getUserDialog(String text, Image image) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.getStyleClass().add("user-dialog");
        dialogBox.setAccessibleText("You said: " + text);
        return dialogBox;
    }

    /**
     * Creates a left-aligned message written by Toothless.
     *
     * @param text message to display.
     * @param image Toothless's avatar.
     * @return configured Toothless dialog box
     */
    public static DialogBox getToothlessDialog(String text, Image image) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.flip();
        dialogBox.getStyleClass().add("toothless-dialog");
        dialogBox.setAccessibleText("Toothless said: " + text);
        return dialogBox;
    }

    /**
     * Places Toothless's avatar on the left of his message.
     */
    private void flip() {
        getChildren().remove(displayPicture);
        getChildren().add(0, displayPicture);
        setAlignment(Pos.TOP_LEFT);
    }
}
