package toothless;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void mainWindow_helpPanel_containsEditExplanationAndExamples() throws IOException {
        try (InputStream input = GuiResourcesTest.class.getResourceAsStream(
                "/view/MainWindow.fxml")) {
            assertNotNull(input);
            String fxml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(fxml.contains("Edit with /description, /by (Deadline), "
                    + "or /from and /to (Event). You can combine fields."));
            assertTrue(fxml.contains("edit 1 /description read the new textbook"));
            assertTrue(fxml.contains("edit 2 /by 2026-09-20"));
            assertTrue(fxml.contains("edit 3 /from 21 September 2026 2pm"));
            assertTrue(fxml.contains("edit 3 /to 21 September 2026 5pm"));
            assertEquals(1, fxml.split("text=\"✎  Edit", -1).length - 1);
        }
    }
}
