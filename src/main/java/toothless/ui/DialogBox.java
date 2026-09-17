package toothless.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * Displays one chat message beside its speaker's avatar.
 */
public class DialogBox extends HBox {
    private static final double MESSAGE_FONT_SIZE = 14.0;
    private static final Font MESSAGE_FONT = loadMessageFont();

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
        dialog.setFont(MESSAGE_FONT);
        if (!"Comic Neue".equals(MESSAGE_FONT.getFamily())) {
            getStyleClass().add("message-font-fallback");
        }
        dialog.maxWidthProperty().bind(widthProperty().subtract(70.0));
        displayPicture.setImage(image);
    }

    /**
     * Loads the bundled conversation font, using the system font if it is unavailable.
     */
    private static Font loadMessageFont() {
        try (InputStream fontStream = DialogBox.class.getResourceAsStream("/fonts/ComicNeue-Regular.ttf")) {
            if (fontStream == null) {
                return Font.font("System", MESSAGE_FONT_SIZE);
            }
            Font font = Font.loadFont(fontStream, MESSAGE_FONT_SIZE);
            return font != null && "Comic Neue".equals(font.getFamily())
                    ? font : Font.font("System", MESSAGE_FONT_SIZE);
        } catch (IOException exception) {
            return Font.font("System", MESSAGE_FONT_SIZE);
        }
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
     * Creates a Toothless message styled for a validation or storage error.
     *
     * @param text error message to display.
     * @param image Toothless's avatar.
     * @return configured error dialog box
     */
    public static DialogBox getErrorDialog(String text, Image image) {
        DialogBox dialogBox = getToothlessDialog(text, image);
        dialogBox.getStyleClass().add("error-dialog");
        dialogBox.setAccessibleText("Toothless error: " + text);
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
