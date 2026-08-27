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
                Parser.ParsedCommand parsedCommand = parser.parse(input);
                CommandType commandType = parsedCommand.getCommandType();
                String details = parsedCommand.getDetails();

                if (commandType == CommandType.BYE) {
                    Command command = new ExitCommand();
                    command.execute(taskList, ui, storage);
                    if (command.isExit()) {
                        break;
                    }
                } else if (commandType == CommandType.LIST) {
                    Command command = new ListCommand();
                    command.execute(taskList, ui, storage);
                } else if (commandType == CommandType.MARK) {
                    int taskNumber = parser.parseTaskNumber(commandType, details, taskList.size());
                    Command command = new MarkCommand(taskNumber);
                    command.execute(taskList, ui, storage);
                } else if (commandType == CommandType.UNMARK) {
                    int taskNumber = parser.parseTaskNumber(commandType, details, taskList.size());
                    Command command = new UnmarkCommand(taskNumber);
                    command.execute(taskList, ui, storage);
                } else if (commandType == CommandType.DELETE) {
                    int taskNumber = parser.parseTaskNumber(commandType, details, taskList.size());
                    Command command = new DeleteCommand(taskNumber);
                    command.execute(taskList, ui, storage);
                } else if (commandType == CommandType.TODO) {
                    String description = parser.parseTodoDescription(details);
                    Command command = new TodoCommand(description);
                    command.execute(taskList, ui, storage);
                } else if (commandType == CommandType.DEADLINE) {
                    Deadline deadline = parser.parseDeadline(details);
                    taskList.addTask(deadline);
                    ui.showTaskAdded(deadline, taskList.size());
                    saveTasks(storage, taskList, ui);
                } else if (commandType == CommandType.EVENT) {
                    Event event = parser.parseEvent(details);
                    taskList.addTask(event);
                    ui.showTaskAdded(event, taskList.size());
                    saveTasks(storage, taskList, ui);
                } else {
                    throw new IllegalStateException("Parser returned an unsupported command");
                }
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
