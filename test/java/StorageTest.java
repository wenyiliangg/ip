import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import toothless.task.Deadline;
import toothless.task.Event;
import toothless.task.TaskList;
import toothless.task.Todo;

/**
 * Runs focused storage tests without requiring an external test framework.
 */
public class StorageTest {
    /**
     * Runs all storage tests and reports success when every assertion passes.
     *
     * @param args command-line arguments; they are not used
     * @throws Exception if a storage operation or assertion fails
     */
    public static void main(String[] args) throws Exception {
        writesEveryTaskTypeAndCompletionStatus();
        savesAfterEveryMutatingCommand();
        loadsEveryTaskTypeAndCompletionStatus();
        roundTripsAMixedTaskList();
        loadsSavedTasksWhenToothlessStarts();
        startsWithoutADataDirectory();
        startsWithoutADataFile();
        createsMissingPathsOnFirstSave();
        reportsExpectedReadAndWriteFailures();
        keepsChangedTasksInMemoryAfterSaveFailure();
        skipsMalformedDataWithoutRewritingTheFile();
        reportsMalformedDataAtStartup();
        preservesMarkedStatusAfterRestart();
        System.out.println("All Storage tests passed.");
    }

    /**
     * Verifies the saved format for every task type and both completion states.
     */
    private static void writesEveryTaskTypeAndCompletionStatus() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-storage-save-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList tasks = new TaskList();

        Todo todo = new Todo("read | chapter \\ one");
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 12, 2));
        Event event = new Event("project meeting", "Monday 2pm", "Monday 3pm");
        deadline.markAsDone();
        tasks.addTask(todo);
        tasks.addTask(deadline);
        tasks.addTask(event);

        storage.save(tasks);

        List<String> savedLines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
        assertEquals(List.of(
                "T | 0 | read \\| chapter \\\\ one",
                "D | 1 | return book | 2019-12-02",
                "E | 0 | project meeting | Monday 2pm | Monday 3pm"),
                savedLines, "all task fields should be saved");
        assertTrue(deadline.toString().contains("★"),
                "a completed task should keep the chatbot's completion display");
        assertTrue(!savedLines.get(1).contains("★"),
                "the display symbol should not be used as storage syntax");
    }

    /**
     * Verifies that each command which changes the list triggers a save.
     */
    private static void savesAfterEveryMutatingCommand() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-command-save-");
        CountingStorage storage = new CountingStorage(testDirectory.resolve("tasks.txt"));
        storage.save(new TaskList());
        storage.resetSaveCount();

        runWithInput(storage, "todo first task\n"
                + "deadline second task /by 2019-12-02\n"
                + "event third task /from 2pm /to 3pm\n"
                + "mark 0\n"
                + "mark -1\n"
                + "mark 99\n"
                + "mark nope\n"
                + "mark\n"
                + "mark 1\n"
                + "mark 1\n"
                + "unmark 1\n"
                + "mark 1\n"
                + "unmark 0\n"
                + "unmark -1\n"
                + "unmark 99\n"
                + "unmark nope\n"
                + "unmark\n"
                + "unmark 1\n"
                + "unmark 1\n"
                + "delete 0\n"
                + "delete -1\n"
                + "delete 99\n"
                + "delete nope\n"
                + "delete\n"
                + "delete 3\n"
                + "bye\n");

        assertEquals(9, storage.getSaveCount(),
                "only successful add, mark, unmark, and delete commands should save");
        assertEquals(List.of(
                "T | 0 | first task",
                "D | 0 | second task | 2019-12-02"),
                Files.readAllLines(storage.getDataFile(), StandardCharsets.UTF_8),
                "the last save should contain the final task list");

        TaskList reloadedTasks = new Storage(storage.getDataFile()).load().getTaskList();
        assertEquals(2, reloadedTasks.size(),
                "the deleted task should remain absent after reloading");
        assertEquals("first task", reloadedTasks.getTask(0).getDescription(),
                "the first remaining task should keep its position");
        assertTrue(!reloadedTasks.getTask(0).isDone(),
                "the unmarked task should remain unmarked after reloading");
        assertEquals("second task", reloadedTasks.getTask(1).getDescription(),
                "the second remaining task should keep its position");
    }

    /**
     * Verifies loading all concrete task types, values, and completion states.
     */
    private static void loadsEveryTaskTypeAndCompletionStatus() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-storage-load-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Files.write(dataFile, List.of(
                "T | 0 | read \\| chapter \\\\ one",
                "D | 1 | return book | 2019-12-02",
                "E | 1 | project meeting | Monday 2pm | Monday 3pm"),
                StandardCharsets.UTF_8);

        TaskList loadedTasks = new Storage(dataFile).load().getTaskList();

        assertEquals(3, loadedTasks.size(), "every valid line should load");
        assertTrue(loadedTasks.getTask(0) instanceof Todo, "the todo type should be restored");
        assertEquals("read | chapter \\ one", loadedTasks.getTask(0).getDescription(),
                "escaped todo text should be restored exactly");
        assertTrue(loadedTasks.getTask(1) instanceof Deadline,
                "the deadline type should be restored");
        Deadline deadline = (Deadline) loadedTasks.getTask(1);
        assertEquals(LocalDate.of(2019, 12, 2), deadline.getBy(),
                "the deadline date should be restored");
        assertTrue(deadline.isDone(), "a completed deadline should remain completed");
        assertTrue(loadedTasks.getTask(2) instanceof Event, "the event type should be restored");
        Event event = (Event) loadedTasks.getTask(2);
        assertEquals("Monday 2pm", event.getFrom(), "the event start should be restored");
        assertEquals("Monday 3pm", event.getTo(), "the event end should be restored");
        assertTrue(event.isDone(), "a completed event should remain completed");
    }

    /**
     * Verifies that saving and loading a mixed list is reversible.
     */
    private static void roundTripsAMixedTaskList() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-storage-round-trip-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList originalTasks = new TaskList();
        originalTasks.addTask(new Todo("borrow book"));
        originalTasks.addTask(new Deadline("return book", LocalDate.of(2019, 12, 6)));
        originalTasks.addTask(new Event("team meeting", "Tuesday 2pm", "Tuesday 3pm"));
        originalTasks.markTask(1);
        originalTasks.markTask(3);

        storage.save(originalTasks);
        TaskList loadedTasks = storage.load().getTaskList();

        assertEquals(originalTasks.size(), loadedTasks.size(),
                "the round trip should preserve the task count");
        for (int i = 0; i < originalTasks.size(); i++) {
            assertEquals(originalTasks.getTask(i).toString(), loadedTasks.getTask(i).toString(),
                    "the round trip should preserve task " + (i + 1));
        }
    }

    /**
     * Verifies that Toothless displays tasks loaded during startup.
     */
    private static void loadsSavedTasksWhenToothlessStarts() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-startup-load-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList tasks = new TaskList();
        Todo todo = new Todo("borrow book");
        todo.markAsDone();
        tasks.addTask(todo);
        storage.save(tasks);

        String output = runWithInput(storage, "list\nbye\n");

        assertTrue(output.contains("1.[T][★] borrow book"),
                "startup loading should preserve the completed-task display");
    }

    /**
     * Verifies startup uses an empty list when the data directory is absent.
     */
    private static void startsWithoutADataDirectory() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-no-directory-");
        Path dataFile = testDirectory.resolve("missing").resolve("tasks.txt");

        TaskList loadedTasks = new Storage(dataFile).load().getTaskList();

        assertTrue(loadedTasks.isEmpty(), "a missing data directory should load an empty list");
        assertTrue(Files.notExists(dataFile.getParent()),
                "loading should not create a missing data directory");
    }

    /**
     * Verifies startup uses an empty list when only the data file is absent.
     */
    private static void startsWithoutADataFile() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-no-file-");
        Path dataFile = testDirectory.resolve("tasks.txt");

        TaskList loadedTasks = new Storage(dataFile).load().getTaskList();

        assertTrue(loadedTasks.isEmpty(), "a missing data file should load an empty list");
        assertTrue(Files.notExists(dataFile), "loading should not create a missing data file");
    }

    /**
     * Verifies the first save creates its directory and file using Path operations.
     */
    private static void createsMissingPathsOnFirstSave() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-first-save-");
        Path dataFile = testDirectory.resolve("nested").resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("first saved task"));

        storage.save(tasks);

        assertTrue(Files.isDirectory(dataFile.getParent()),
                "the first save should create the data directory");
        assertTrue(Files.isRegularFile(dataFile), "the first save should create the data file");
        assertEquals("[T][ ] first saved task",
                storage.load().getTaskList().getTask(0).toString(),
                "the first saved task should remain readable");
    }

    /**
     * Verifies expected file-system failures use storage errors and preserve other state.
     */
    private static void reportsExpectedReadAndWriteFailures() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-io-failures-");
        Path directoryAsFile = Files.createDirectory(testDirectory.resolve("directory-as-file"));
        assertStorageFailure(() -> new Storage(directoryAsFile).load(),
                "reading a directory as a data file should fail safely");

        Path blockingParent = testDirectory.resolve("blocking-parent");
        Files.writeString(blockingParent, "unrelated state", StandardCharsets.UTF_8);
        Storage failingStorage = new Storage(blockingParent.resolve("tasks.txt"));
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("keep in memory"));

        assertStorageFailure(() -> failingStorage.save(tasks),
                "writing below a regular file should fail safely");
        assertEquals(1, tasks.size(), "a failed save should not change the task list");
        assertEquals("unrelated state", Files.readString(blockingParent, StandardCharsets.UTF_8),
                "a failed save should not corrupt unrelated file state");
    }

    /**
     * Verifies Toothless reports a save failure and keeps the changed list usable.
     */
    private static void keepsChangedTasksInMemoryAfterSaveFailure() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-save-message-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "", StandardCharsets.UTF_8);
        Storage storage = new FailingSaveStorage(dataFile);

        String output = runWithInput(storage, "todo keep this task\nlist\nbye\n");

        assertTrue(output.contains("Toothless couldn’t tuck these changes into his data file."),
                "a write failure should have a friendly message");
        assertTrue(output.contains("1.[T][ ] keep this task"),
                "the changed task should remain available after a write failure");
        assertTrue(!output.contains("Exception"), "expected failures should not print a stack trace");
    }

    /**
     * Verifies malformed entries are skipped while valid entries and source data remain intact.
     */
    private static void skipsMalformedDataWithoutRewritingTheFile() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-malformed-load-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        List<String> originalLines = List.of(
                "T | 1 | borrow book",
                "X | 0 | unknown type",
                "D | 0 | missing time",
                "T | maybe | invalid status",
                "E | 0 | truncated event | 2pm",
                "",
                "T | 0 | unexpected data | extra field",
                "T | 0 | invalid \\q escape",
                "T | 0 | ",
                "D | 0 | wrong date | 02-12-2019",
                "D | 0 | impossible date | 2019-02-30",
                "D | 0 | return book | 2019-12-06");
        Files.write(dataFile, originalLines, StandardCharsets.UTF_8);

        StorageLoadResult loadResult = new Storage(dataFile).load();

        assertEquals(10, loadResult.getMalformedLineCount(),
                "every malformed saved entry should be counted");
        assertEquals(2, loadResult.getTaskList().size(),
                "valid entries around malformed data should be preserved");
        assertEquals("[T][★] borrow book", loadResult.getTaskList().getTask(0).toString(),
                "the valid completed todo should load unchanged");
        assertEquals("[D][ ] return book (by: Dec 6 2019)",
                loadResult.getTaskList().getTask(1).toString(),
                "the valid deadline after malformed entries should load unchanged");
        assertEquals(originalLines, Files.readAllLines(dataFile, StandardCharsets.UTF_8),
                "loading malformed data should not rewrite the original file");
    }

    /**
     * Verifies Toothless reports skipped saved data without exposing a raw error.
     */
    private static void reportsMalformedDataAtStartup() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-malformed-message-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Files.write(dataFile, List.of("T | 0 | valid task", "unknown line"),
                StandardCharsets.UTF_8);

        String output = runWithInput(new Storage(dataFile), "list\nbye\n");

        assertTrue(output.contains("Toothless found 1 puzzling line in his saved quests."),
                "corrupted data should have a clear startup warning");
        assertTrue(output.contains("1.[T][ ] valid task"),
                "valid saved tasks should remain available");
        assertTrue(!output.contains("Exception"),
                "malformed saved data should not print a stack trace");
    }

    /**
     * Verifies a command-marked task still displays as completed after restart.
     */
    private static void preservesMarkedStatusAfterRestart() throws Exception {
        Path testDirectory = Files.createTempDirectory("toothless-mark-restart-");
        Storage storage = new Storage(testDirectory.resolve("data").resolve("tasks.txt"));

        runWithInput(storage, "todo borrow book\nmark 1\nbye\n");
        String restartedOutput = runWithInput(storage, "list\nbye\n");

        assertTrue(restartedOutput.contains("1.[T][★] borrow book"),
                "marking, saving, and restarting should preserve the completion display");
    }

    /**
     * Runs Toothless with isolated input and output streams.
     */
    private static String runWithInput(Storage storage, String input) {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Toothless.run(storage);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Fails the test if two values are not equal.
     */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "\nExpected: " + expected + "\nActual: " + actual);
        }
    }

    /**
     * Fails the test if the condition is false.
     */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Fails the test unless the operation reports an expected storage error.
     */
    private static void assertStorageFailure(StorageOperation operation, String message) {
        try {
            operation.run();
        } catch (StorageException exception) {
            return;
        }
        throw new AssertionError(message);
    }

    /**
     * Represents a storage operation that may fail in an expected way.
     */
    @FunctionalInterface
    private interface StorageOperation {
        void run() throws StorageException;
    }

    /**
     * Records how often Toothless asks storage to persist a changed list.
     */
    private static class CountingStorage extends Storage {
        private final Path dataFile;
        private int saveCount;

        CountingStorage(Path dataFile) {
            super(dataFile);
            this.dataFile = dataFile;
        }

        @Override
        public void save(TaskList taskList) throws StorageException {
            saveCount++;
            super.save(taskList);
        }

        int getSaveCount() {
            return saveCount;
        }

        void resetSaveCount() {
            saveCount = 0;
        }

        Path getDataFile() {
            return dataFile;
        }
    }

    /**
     * Simulates a predictable write failure after startup loading succeeds.
     */
    private static class FailingSaveStorage extends Storage {
        FailingSaveStorage(Path dataFile) {
            super(dataFile);
        }

        @Override
        public void save(TaskList taskList) throws StorageException {
            throw new StorageException("Expected test failure", new IllegalStateException());
        }
    }
}
