package toothless;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
