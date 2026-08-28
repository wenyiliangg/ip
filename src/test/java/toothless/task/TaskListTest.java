package toothless.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import toothless.exception.ToothlessException;

/**
 * Tests task-list mutations and their one-based task-number validation.
 */
public class TaskListTest {
    /**
     * Verifies marking selects one-based task numbers without changing list order.
     */
    @Test
    public void markTask_firstAndLastTask_marksOnlySelectedTasks() throws ToothlessException {
        TaskList taskList = createThreeTaskList();

        Task firstMarkedTask = taskList.markTask(1);
        Task lastMarkedTask = taskList.markTask(3);

        assertSame(taskList.getTask(0), firstMarkedTask);
        assertSame(taskList.getTask(2), lastMarkedTask);
        assertTrue(taskList.getTask(0).isDone());
        assertFalse(taskList.getTask(1).isDone());
        assertTrue(taskList.getTask(2).isDone());
        assertEquals(3, taskList.size());
    }

    /**
     * Verifies marking an empty list reports the focused empty-list error.
     */
    @Test
    public void markTask_emptyList_throwsToothlessException() {
        TaskList taskList = new TaskList();

        ToothlessException exception = assertThrows(ToothlessException.class,
                () -> taskList.markTask(1));

        assertEquals("Toothless's cave is empty, so there is no task to mark.\n"
                + "Add a task first, then try again.", exception.getMessage());
        assertTrue(taskList.isEmpty());
    }

    /**
     * Verifies invalid mark numbers neither select nor mutate a task.
     */
    @Test
    public void markTask_numbersOutsideRange_throwAndPreserveState() {
        TaskList taskList = createThreeTaskList();

        ToothlessException zeroException = assertThrows(ToothlessException.class,
                () -> taskList.markTask(0));
        ToothlessException largeException = assertThrows(ToothlessException.class,
                () -> taskList.markTask(4));

        assertEquals("Toothless can’t find task 0 in the cave.\n"
                + "Please choose a number from 1 to 3.", zeroException.getMessage());
        assertEquals("Toothless can’t find task 4 in the cave.\n"
                + "Please choose a number from 1 to 3.", largeException.getMessage());
        assertFalse(taskList.getTask(0).isDone());
        assertFalse(taskList.getTask(1).isDone());
        assertFalse(taskList.getTask(2).isDone());
    }

    /**
     * Verifies unmarking a completed task changes its state and reports that change.
     */
    @Test
    public void unmarkTask_markedTask_unmarksAndReportsChanged() throws ToothlessException {
        TaskList taskList = createThreeTaskList();
        taskList.markTask(2);

        TaskList.UnmarkResult result = taskList.unmarkTask(2);

        assertSame(taskList.getTask(1), result.getTask());
        assertTrue(result.wasChanged());
        assertFalse(result.getTask().isDone());
    }

    /**
     * Verifies unmarking an incomplete task leaves it unchanged and reports that outcome.
     */
    @Test
    public void unmarkTask_unmarkedTask_preservesStateAndReportsUnchanged()
            throws ToothlessException {
        TaskList taskList = createThreeTaskList();

        TaskList.UnmarkResult result = taskList.unmarkTask(2);

        assertSame(taskList.getTask(1), result.getTask());
        assertFalse(result.wasChanged());
        assertFalse(result.getTask().isDone());
        assertEquals(3, taskList.size());
    }

    /**
     * Verifies invalid unmark numbers report the current range and preserve completion state.
     */
    @Test
    public void unmarkTask_numbersOutsideRange_throwAndPreserveState()
            throws ToothlessException {
        TaskList taskList = createThreeTaskList();
        taskList.markTask(1);

        ToothlessException negativeException = assertThrows(ToothlessException.class,
                () -> taskList.unmarkTask(-1));
        ToothlessException largeException = assertThrows(ToothlessException.class,
                () -> taskList.unmarkTask(99));

        assertEquals("Toothless can’t find task -1 in the cave.\n"
                + "Please choose a number from 1 to 3.", negativeException.getMessage());
        assertEquals("Toothless can’t find task 99 in the cave.\n"
                + "Please choose a number from 1 to 3.", largeException.getMessage());
        assertTrue(taskList.getTask(0).isDone());
        assertEquals(3, taskList.size());
    }

    /**
     * Verifies deleting a middle task returns it and closes the resulting list gap.
     */
    @Test
    public void deleteTask_middleTask_removesReturnsAndRenumbersTasks() throws ToothlessException {
        TaskList taskList = createThreeTaskList();
        Task middleTask = taskList.getTask(1);

        Task deletedTask = taskList.deleteTask(2);

        assertSame(middleTask, deletedTask);
        assertEquals(2, taskList.size());
        assertEquals("first", taskList.getTask(0).getDescription());
        assertEquals("third", taskList.getTask(1).getDescription());
    }

    /**
     * Verifies first and last deletions continue to use the current one-based numbering.
     */
    @Test
    public void deleteTask_firstThenLast_removesTasksUsingCurrentNumbering()
            throws ToothlessException {
        TaskList taskList = createThreeTaskList();

        Task firstDeletedTask = taskList.deleteTask(1);
        Task lastDeletedTask = taskList.deleteTask(2);

        assertEquals("first", firstDeletedTask.getDescription());
        assertEquals("third", lastDeletedTask.getDescription());
        assertEquals(1, taskList.size());
        assertEquals("second", taskList.getTask(0).getDescription());
    }

    /**
     * Verifies invalid deletion leaves every task in its original order.
     */
    @Test
    public void deleteTask_numbersOutsideRange_throwAndPreserveOrder() {
        TaskList taskList = createThreeTaskList();

        ToothlessException zeroException = assertThrows(ToothlessException.class,
                () -> taskList.deleteTask(0));
        ToothlessException largeException = assertThrows(ToothlessException.class,
                () -> taskList.deleteTask(4));

        assertEquals("Toothless can’t find task 0 in the cave.\n"
                + "Please choose a number from 1 to 3.", zeroException.getMessage());
        assertEquals("Toothless can’t find task 4 in the cave.\n"
                + "Please choose a number from 1 to 3.", largeException.getMessage());
        assertEquals(3, taskList.size());
        assertEquals("first", taskList.getTask(0).getDescription());
        assertEquals("second", taskList.getTask(1).getDescription());
        assertEquals("third", taskList.getTask(2).getDescription());
    }

    /**
     * Verifies a partial-word search ignores case and can return one match.
     */
    @Test
    public void findTasks_caseInsensitivePartialKeyword_returnsOneMatch() {
        TaskList taskList = createThreeTaskList();

        List<Task> matchingTasks = taskList.findTasks("IrS");

        assertEquals(1, matchingTasks.size());
        assertSame(taskList.getTask(0), matchingTasks.get(0));
    }

    /**
     * Verifies a multi-word search returns every match in its original order.
     */
    @Test
    public void findTasks_multiWordKeyword_returnsMatchesInOriginalOrder() {
        TaskList taskList = new TaskList();
        taskList.addTask(new Todo("read project book"));
        taskList.addTask(new Todo("write project plan"));
        taskList.addTask(new Todo("review project book notes"));

        List<Task> matchingTasks = taskList.findTasks("project book");

        assertEquals(2, matchingTasks.size());
        assertSame(taskList.getTask(0), matchingTasks.get(0));
        assertSame(taskList.getTask(2), matchingTasks.get(1));
    }

    /**
     * Verifies searches inspect only descriptions and never mutate task state or order.
     */
    @Test
    public void findTasks_interleavedMatchesAndNoMatches_preservesTaskList()
            throws ToothlessException {
        TaskList taskList = new TaskList();
        Todo todo = new Todo("read book");
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 12, 6));
        Event event = new Event("book club", "Monday 2pm", "Monday 3pm");
        taskList.addTask(todo);
        taskList.addTask(deadline);
        taskList.addTask(event);
        taskList.markTask(2);

        List<Task> bookMatches = taskList.findTasks("BOOK");
        List<Task> dateMatches = taskList.findTasks("2019");
        List<Task> periodMatches = taskList.findTasks("Monday");
        List<Task> typeMatches = taskList.findTasks("[D]");
        List<Task> statusMatches = taskList.findTasks("★");
        List<Task> laterMatches = taskList.findTasks("club");

        assertEquals(List.of(todo, deadline, event), bookMatches);
        assertTrue(dateMatches.isEmpty());
        assertTrue(periodMatches.isEmpty());
        assertTrue(typeMatches.isEmpty());
        assertTrue(statusMatches.isEmpty());
        assertEquals(List.of(event), laterMatches);
        assertEquals(3, taskList.size());
        assertSame(todo, taskList.getTask(0));
        assertSame(deadline, taskList.getTask(1));
        assertSame(event, taskList.getTask(2));
        assertFalse(todo.isDone());
        assertTrue(deadline.isDone());
        assertFalse(event.isDone());
    }

    /**
     * Creates a representative ordered list for mutation tests.
     */
    private TaskList createThreeTaskList() {
        TaskList taskList = new TaskList();
        taskList.addTask(new Todo("first"));
        taskList.addTask(new Todo("second"));
        taskList.addTask(new Todo("third"));
        return taskList;
    }
}
