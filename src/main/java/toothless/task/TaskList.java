package toothless.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import toothless.exception.ToothlessException;

/**
 * Stores tasks and provides focused operations for changing the task list.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list backed by an {@link ArrayList}.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at the given zero-based index.
     *
     * @param index zero-based task index
     * @return selected task
     */
    public Task getTask(int index) {
        return tasks.get(index);
    }

    /**
     * Finds tasks whose descriptions contain the keyword, ignoring letter case.
     *
     * @param keyword text to look for within task descriptions
     * @return matching tasks in their original list order
     */
    public List<Task> findTasks(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<Task> matchingTasks = new ArrayList<>();
        for (Task task : tasks) {
            String normalizedDescription = task.getDescription().toLowerCase(Locale.ROOT);
            if (normalizedDescription.contains(normalizedKeyword)) {
                matchingTasks.add(task);
            }
        }
        return matchingTasks;
    }

    /**
     * Marks the task with the given one-based number as completed.
     *
     * @param taskNumber one-based task number
     * @return marked task
     * @throws ToothlessException if the task number is outside the task list
     */
    public Task markTask(int taskNumber) throws ToothlessException {
        if (tasks.isEmpty()) {
            throw new ToothlessException("Toothless's cave is empty, so there is no task to "
                    + "mark.\nAdd a task first, then try again.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ToothlessException(
                    "Toothless can’t find task " + taskNumber + " in the cave.\n"
                            + "Please choose a number from 1 to " + tasks.size() + ".");
        }
        Task task = tasks.get(taskNumber - 1);
        task.markAsDone();
        return task;
    }

    /**
     * Unmarks the task with the given one-based number when it is completed.
     *
     * @param taskNumber one-based task number
     * @return the selected task and whether its completion state changed
     * @throws ToothlessException if the task number is outside the task list
     */
    public UnmarkResult unmarkTask(int taskNumber) throws ToothlessException {
        if (tasks.isEmpty()) {
            throw new ToothlessException("Toothless's cave is empty, so there is no task to "
                    + "unmark.\nAdd a task first, then try again.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ToothlessException(
                    "Toothless can’t find task " + taskNumber + " in the cave.\n"
                            + "Please choose a number from 1 to " + tasks.size() + ".");
        }

        Task task = tasks.get(taskNumber - 1);
        if (!task.isDone()) {
            return new UnmarkResult(task, false);
        }
        task.unmarkAsDone();
        return new UnmarkResult(task, true);
    }

    /**
     * Removes and returns the task with the given one-based number.
     *
     * @param taskNumber one-based task number
     * @return removed task
     * @throws ToothlessException if the task number is outside the task list
     */
    public Task deleteTask(int taskNumber) throws ToothlessException {
        if (tasks.isEmpty()) {
            throw new ToothlessException("Toothless's cave is empty, so there is no task to "
                    + "delete.\nAdd a task first, then try again.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ToothlessException(
                    "Toothless can’t find task " + taskNumber + " in the cave.\n"
                            + "Please choose a number from 1 to " + tasks.size() + ".");
        }
        return tasks.remove(taskNumber - 1);
    }

    /**
     * Returns the number of tasks currently stored.
     *
     * @return task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the task list is empty.
     *
     * @return true when no tasks are stored
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Describes the outcome of attempting to unmark a task.
     */
    public static class UnmarkResult {
        private final Task task;
        private final boolean wasChanged;

        /**
         * Creates an unmark result for a selected task.
         *
         * @param task selected task
         * @param wasChanged whether the task changed from marked to unmarked
         */
        private UnmarkResult(Task task, boolean wasChanged) {
            this.task = task;
            this.wasChanged = wasChanged;
        }

        /**
         * Returns the task selected by the unmark operation.
         *
         * @return selected task
         */
        public Task getTask() {
            return task;
        }

        /**
         * Returns whether unmarking changed the task's completion state.
         *
         * @return true when a marked task became unmarked
         */
        public boolean wasChanged() {
            return wasChanged;
        }
    }
}
