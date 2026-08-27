import java.nio.file.Path;

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
        while (ui.hasNextCommand()) {
            String input = ui.readCommand();

            try {
                Command command = parser.parse(input, taskList.size());
                if (command.isExit()) {
                    command.execute(taskList, ui, storage);
                    break;
                }
                command.execute(taskList, ui, storage);
            } catch (ToothlessException exception) {
                ui.showError(exception.getMessage());
            }
            ui.showDivider();
        }

        ui.close();
    }

    /**
     * Saves a changed task list while keeping it available after an expected failure.
     */
    private static void saveTasks(Storage storage, TaskList taskList, Ui ui) {
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
