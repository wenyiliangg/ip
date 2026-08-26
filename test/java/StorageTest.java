import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        System.out.println("All Storage tests passed.");
    }

    /**
     * Verifies the saved format for every task type and both completion states.
     */
    private static void writesEveryTaskTypeAndCompletionStatus() throws IOException {
        Path testDirectory = Files.createTempDirectory("toothless-storage-save-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList tasks = new TaskList();

        Todo todo = new Todo("read | chapter \\ one");
        Deadline deadline = new Deadline("return book", "Sunday 5pm");
        Event event = new Event("project meeting", "Monday 2pm", "Monday 3pm");
        deadline.markAsDone();
        tasks.addTask(todo);
        tasks.addTask(deadline);
        tasks.addTask(event);

        storage.save(tasks);

        List<String> savedLines = Files.readAllLines(dataFile, StandardCharsets.UTF_8);
        assertEquals(List.of(
                "T | 0 | read \\| chapter \\\\ one",
                "D | 1 | return book | Sunday 5pm",
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
                + "deadline second task /by Sunday\n"
                + "event third task /from 2pm /to 3pm\n"
                + "mark 1\n"
                + "unmark 1\n"
                + "delete 3\n"
                + "bye\n");

        assertEquals(6, storage.getSaveCount(),
                "add, mark, unmark, and delete commands should each save once");
        assertEquals(List.of(
                "T | 0 | first task",
                "D | 0 | second task | Sunday"),
                Files.readAllLines(storage.getDataFile(), StandardCharsets.UTF_8),
                "the last save should contain the final task list");
    }

    /**
     * Verifies loading all concrete task types, values, and completion states.
     */
    private static void loadsEveryTaskTypeAndCompletionStatus() throws IOException {
        Path testDirectory = Files.createTempDirectory("toothless-storage-load-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Files.write(dataFile, List.of(
                "T | 0 | read \\| chapter \\\\ one",
                "D | 1 | return book | Sunday 5pm",
                "E | 1 | project meeting | Monday 2pm | Monday 3pm"),
                StandardCharsets.UTF_8);

        TaskList loadedTasks = new Storage(dataFile).load();

        assertEquals(3, loadedTasks.size(), "every valid line should load");
        assertTrue(loadedTasks.getTask(0) instanceof Todo, "the todo type should be restored");
        assertEquals("read | chapter \\ one", loadedTasks.getTask(0).getDescription(),
                "escaped todo text should be restored exactly");
        assertTrue(loadedTasks.getTask(1) instanceof Deadline,
                "the deadline type should be restored");
        Deadline deadline = (Deadline) loadedTasks.getTask(1);
        assertEquals("Sunday 5pm", deadline.getBy(), "the deadline time should be restored");
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
    private static void roundTripsAMixedTaskList() throws IOException {
        Path testDirectory = Files.createTempDirectory("toothless-storage-round-trip-");
        Path dataFile = testDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList originalTasks = new TaskList();
        originalTasks.addTask(new Todo("borrow book"));
        originalTasks.addTask(new Deadline("return book", "Friday 6pm"));
        originalTasks.addTask(new Event("team meeting", "Tuesday 2pm", "Tuesday 3pm"));
        originalTasks.markTask(0);
        originalTasks.markTask(2);

        storage.save(originalTasks);
        TaskList loadedTasks = storage.load();

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
     * Runs Toothless with isolated input and output streams.
     */
    private static String runWithInput(Storage storage, String input) throws IOException {
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
        public void save(TaskList taskList) throws IOException {
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
}
