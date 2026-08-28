package toothless.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
