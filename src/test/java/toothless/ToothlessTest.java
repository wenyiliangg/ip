package toothless;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import toothless.storage.Storage;
import toothless.storage.StorageException;
import toothless.task.Deadline;
import toothless.task.TaskList;
import toothless.task.Todo;

/**
 * Tests application-level command, storage, and restart behavior.
 */
@ResourceLock("java.lang.System.in")
@ResourceLock(Resources.SYSTEM_OUT)
public class ToothlessTest {
    @TempDir
    private Path temporaryDirectory;

    /**
     * Verifies only successful commands that change task state trigger persistence.
     */
    @Test
    public void run_mutatingCommands_savesOnlySuccessfulStateChanges() throws Exception {
        CountingStorage storage = new CountingStorage(temporaryDirectory.resolve("tasks.txt"));
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

        assertEquals(9, storage.getSaveCount());
        assertEquals(List.of(
                "T | 0 | first task",
                "D | 0 | second task | 2019-12-02"),
                Files.readAllLines(storage.getDataFile(), StandardCharsets.UTF_8));

        TaskList reloadedTasks = new Storage(storage.getDataFile()).load().getTaskList();
        assertEquals(2, reloadedTasks.size());
        assertEquals("first task", reloadedTasks.getTask(0).getDescription());
        assertFalse(reloadedTasks.getTask(0).isDone());
        assertEquals("second task", reloadedTasks.getTask(1).getDescription());
    }

    /**
     * Verifies tasks loaded at startup are immediately available to commands.
     */
    @Test
    public void run_savedCompletedTask_displaysLoadedTaskAtStartup() throws Exception {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        TaskList tasks = new TaskList();
        Todo todo = new Todo("borrow book");
        todo.markAsDone();
        tasks.addTask(todo);
        storage.save(tasks);

        String output = runWithInput(storage, "list\nbye\n");

        assertTrue(output.contains("1.[T][★] borrow book"));
    }

    /**
     * Verifies a save failure is reported while changed in-memory tasks remain usable.
     */
    @Test
    public void run_saveFailure_reportsFriendlyMessageAndKeepsTaskInMemory() throws Exception {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "", StandardCharsets.UTF_8);
        Storage storage = new FailingSaveStorage(dataFile);

        String output = runWithInput(storage, "todo keep this task\nlist\nbye\n");

        assertTrue(output.contains("Toothless couldn’t tuck these changes into his data file."));
        assertTrue(output.contains("1.[T][ ] keep this task"));
        assertFalse(output.contains("Exception"));
    }

    /**
     * Verifies malformed storage is reported while valid saved tasks remain usable.
     */
    @Test
    public void run_malformedSavedData_reportsWarningAndKeepsValidTasks() throws Exception {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.write(dataFile, List.of("T | 0 | valid task", "unknown line"),
                StandardCharsets.UTF_8);

        String output = runWithInput(new Storage(dataFile), "list\nbye\n");

        assertTrue(output.contains("Toothless found 1 puzzling line in his saved quests."));
        assertTrue(output.contains("1.[T][ ] valid task"));
        assertFalse(output.contains("Exception"));
    }

    /**
     * Verifies a task marked through a command remains completed after restarting Toothless.
     */
    @Test
    public void run_markTaskThenRestart_preservesCompletedStatus() {
        Storage storage = new Storage(
                temporaryDirectory.resolve("data").resolve("tasks.txt"));

        runWithInput(storage, "todo borrow book\nmark 1\nbye\n");
        String restartedOutput = runWithInput(storage, "list\nbye\n");

        assertTrue(restartedOutput.contains("1.[T][★] borrow book"));
    }

    /**
     * Verifies consecutive finds show fresh results without saving or changing tasks.
     */
    @Test
    public void run_findCommands_displayFreshResultsWithoutSavingOrMutatingTasks()
            throws Exception {
        CountingStorage storage = new CountingStorage(temporaryDirectory.resolve("tasks.txt"));
        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.addTask(todo);
        tasks.addTask(new Deadline("return book", LocalDate.of(2019, 12, 6)));
        tasks.addTask(new Todo("write report"));
        storage.save(tasks);
        storage.resetSaveCount();

        String output = runWithInput(storage, "find Book\n"
                + "find dragon\n"
                + "find return book\n"
                + "list\n"
                + "bye\n");

        assertTrue(output.contains("Here are the matching tasks in your list:\n"
                + "1.[T][★] read book\n"
                + "2.[D][ ] return book (by: Dec 6 2019)"));
        assertTrue(output.contains("Toothless couldn’t find any matching tasks in the cave.\n"
                + "Try another keyword and he'll sniff around again!"));
        assertTrue(output.contains("Here are the matching tasks in your list:\n"
                + "1.[D][ ] return book (by: Dec 6 2019)"));
        assertTrue(output.contains("Here are the tasks in your list:\n"
                + "1.[T][★] read book\n"
                + "2.[D][ ] return book (by: Dec 6 2019)\n"
                + "3.[T][ ] write report"));
        assertEquals(0, storage.getSaveCount());
        assertEquals(List.of(
                "T | 1 | read book",
                "D | 0 | return book | 2019-12-06",
                "T | 0 | write report"),
                Files.readAllLines(storage.getDataFile(), StandardCharsets.UTF_8));
    }

    /**
     * Runs Toothless with isolated input and output streams.
     */
    private String runWithInput(Storage storage, String input) {
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
