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
                Command command = parsedCommand.getCommand();
                String details = parsedCommand.getDetails();

                if (command == Command.BYE) {
                    ui.showGoodbye();
                    break;
                } else if (command == Command.LIST) {
                    ui.showTaskList(taskList);
                } else if (command == Command.MARK) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task markedTask = taskList.markTask(taskIndex);
                    ui.showTaskMarked(markedTask);
                    saveTasks(storage, taskList, ui);
                } else if (command == Command.UNMARK) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task selectedTask = taskList.getTask(taskIndex);
                    if (!selectedTask.isDone()) {
                        ui.showTaskAlreadyUnmarked(selectedTask);
                    } else {
                        Task unmarkedTask = taskList.unmarkTask(taskIndex);
                        ui.showTaskUnmarked(unmarkedTask);
                        saveTasks(storage, taskList, ui);
                    }
                } else if (command == Command.DELETE) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task deletedTask = taskList.deleteTask(taskIndex);
                    ui.showTaskDeleted(deletedTask, taskList.size());
                    saveTasks(storage, taskList, ui);
                } else if (command == Command.TODO) {
                    Todo todo = parser.parseTodo(details);
                    taskList.addTask(todo);
                    ui.showTaskAdded(todo, taskList.size());
                    saveTasks(storage, taskList, ui);
                } else if (command == Command.DEADLINE) {
                    Deadline deadline = parser.parseDeadline(details);
                    taskList.addTask(deadline);
                    ui.showTaskAdded(deadline, taskList.size());
                    saveTasks(storage, taskList, ui);
                } else if (command == Command.EVENT) {
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
