package toothless;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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

    private final Parser parser;
    private final Storage storage;
    private final TaskList taskList;
    private final String startupMessage;
    private boolean hasExited;

    /**
     * Creates a Toothless session backed by the default data file.
     */
    public Toothless() {
        this(new Storage(DATA_FILE));
    }

    /**
     * Creates a Toothless session backed by the supplied storage.
     *
     * @param storage storage used to load and save this session's tasks.
     */
    Toothless(Storage storage) {
        this.parser = new Parser();
        this.storage = storage;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = createResponseUi(output);
        this.taskList = loadTasks(storage, ui);
        this.startupMessage = output.toString(StandardCharsets.UTF_8).stripTrailing();
        ui.close();
    }

    /**
     * Runs the chatbot and responds to commands entered by the user.
     *
     * @param args command-line arguments; they are not used.
     */
    public static void main(String[] args) {
        run(new Storage(DATA_FILE));
    }

    /**
     * Runs the chatbot using the supplied storage destination.
     *
     * @param storage storage used after task-list changes.
     */
    static void run(Storage storage) {
        Ui ui = new Ui();
        ui.showWelcome();
        TaskList taskList = loadTasks(storage, ui);

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
            ui.showDivider();
        }

        ui.close();
    }

    /**
     * Executes one user command and returns Toothless's complete response.
     *
     * @param input complete command entered by the user.
     * @return response text produced by the existing command workflow
     */
    public String getResponse(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = createResponseUi(output);
        try {
            Command command = parser.parse(input, taskList.size());
            command.execute(taskList, ui, storage);
            hasExited = command.isExit();
        } catch (ToothlessException exception) {
            ui.showError(exception.getMessage());
        }
        ui.close();
        return output.toString(StandardCharsets.UTF_8).stripTrailing();
    }

    /**
     * Returns a warning produced while loading saved tasks for this session.
     *
     * @return startup warning, or an empty string when loading succeeded cleanly
     */
    public String getStartupMessage() {
        return startupMessage;
    }

    /**
     * Returns whether the latest successful command ended this session.
     *
     * @return true after a valid {@code bye} command
     */
    public boolean hasExited() {
        return hasExited;
    }

    /**
     * Loads saved tasks and reports recoverable problems through the supplied UI.
     *
     * @param storage storage from which tasks should be loaded.
     * @param ui user interface that receives any loading warning.
     * @return recovered tasks, or an empty task list after a read failure
     */
    private static TaskList loadTasks(Storage storage, Ui ui) {
        try {
            StorageLoadResult loadResult = storage.load();
            if (loadResult.getMalformedLineCount() > 0) {
                ui.showMalformedDataWarning(loadResult.getMalformedLineCount());
            }
            return loadResult.getTaskList();
        } catch (StorageException exception) {
            ui.showLoadError();
            return new TaskList();
        }
    }

    /**
     * Creates a console UI whose output can be returned to the JavaFX controller.
     *
     * @param output buffer that receives one response.
     * @return isolated UI for executing one command
     */
    private static Ui createResponseUi(ByteArrayOutputStream output) {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        return new Ui(input, printStream, false);
    }
}
