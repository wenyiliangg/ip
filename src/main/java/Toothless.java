import java.nio.file.Path;

import toothless.command.Command;
import toothless.exception.ToothlessException;
import toothless.parser.Parser;
import toothless.storage.Storage;
import toothless.storage.StorageException;
import toothless.storage.StorageLoadResult;
import toothless.task.TaskList;
import toothless.ui.Ui;

/**
 * Starts the Toothless chatbot application.
 */
public class Toothless {
    private static final Path DATA_FILE = Path.of("data", "toothless.txt");

    /**
     * Runs the chatbot and responds to commands entered by the user.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args) {
        run(new Storage(DATA_FILE));
    }

    /**
     * Runs the chatbot using the supplied storage destination.
     *
     * @param storage storage used after task-list changes
     */
    static void run(Storage storage) {
        Ui ui = new Ui();
        ui.showWelcome();
        TaskList taskList;
        try {
            StorageLoadResult loadResult = storage.load();
            taskList = loadResult.getTaskList();
            if (loadResult.getMalformedLineCount() > 0) {
                ui.showMalformedDataWarning(loadResult.getMalformedLineCount());
            }
        } catch (StorageException exception) {
            taskList = new TaskList();
            ui.showLoadError();
        }

        Parser parser = new Parser();
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                Command command = parser.parse(input, taskList.size());
                command.execute(taskList, ui, storage);
                isExit = command.isExit();
            } catch (ToothlessException exception) {
                ui.showError(exception.getMessage());
            }
            if (!isExit) {
                ui.showDivider();
            }
        }

        ui.close();
    }
}
