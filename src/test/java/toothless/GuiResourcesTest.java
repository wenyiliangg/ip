package toothless;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Verifies that the resources needed to launch the JavaFX window are packaged.
 */
class GuiResourcesTest {
    @Test
    void guiResources_allRequiredFilesArePresent() {
        assertNotNull(GuiResourcesTest.class.getResource("/view/MainWindow.fxml"));
        assertNotNull(GuiResourcesTest.class.getResource("/view/DialogBox.fxml"));
        assertNotNull(GuiResourcesTest.class.getResource("/css/toothless.css"));
        assertNotNull(GuiResourcesTest.class.getResource("/images/night-sky-header.png"));
        assertNotNull(GuiResourcesTest.class.getResource("/images/toothless-avatar.png"));
        assertNotNull(GuiResourcesTest.class.getResource("/images/user-avatar.png"));
    }

    @Test
    void mainWindow_helpPanel_containsNestedEditExamples() throws IOException {
        try (InputStream input = GuiResourcesTest.class.getResourceAsStream(
                "/view/MainWindow.fxml")) {
            assertNotNull(input);
            String fxml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertEquals(1, fxml.split("text=\"✎  Edit task\"", -1).length - 1);
            assertTrue(fxml.contains("onAction=\"#toggleEditHelp\""));
            assertTrue(fxml.contains("fx:id=\"editCommandButtons\""));
            assertTrue(fxml.contains("text=\"✎  Edit description\""));
            assertTrue(fxml.contains("text=\"◷  Edit deadline\""));
            assertTrue(fxml.contains("text=\"✦  Edit event\""));
            assertTrue(fxml.contains("edit 1 /description read the new textbook"));
            assertTrue(fxml.contains("edit 2 /by 2026-09-20"));
            assertTrue(fxml.contains("edit 3 /from 21 September 2026 2pm "
                    + "/to 21 September 2026 5pm"));
            assertFalse(fxml.contains("Any task: edit"));
        }
    }

    @Test
    void errorDialog_stylesBubbleWithoutChangingNormalReplies() throws IOException {
        try (InputStream input = GuiResourcesTest.class.getResourceAsStream("/css/toothless.css")) {
            assertNotNull(input);
            String css = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(css.contains(".toothless-dialog .dialog-bubble"));
            assertTrue(css.contains(".error-dialog .dialog-bubble"));
            assertTrue(css.contains("-fx-background-color: #f9e4dd"));
            assertTrue(css.contains("-fx-border-color: #a3483e"));
        }
    }
}
