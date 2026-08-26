import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * Runs Toothless with isolated input and output streams.
     */
    private static void runWithInput(Storage storage, String input) throws IOException {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(OutputStream.nullOutputStream(), true,
                    StandardCharsets.UTF_8));
            Toothless.run(storage);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
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

        Path getDataFile() {
            return dataFile;
        }
    }
}
