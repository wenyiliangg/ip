import java.util.ArrayList;
import java.util.List;

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
     * Marks a task as completed and returns it for display.
     *
     * @param index zero-based task index
     * @return marked task
     */
    public Task markTask(int index) {
        Task task = tasks.get(index);
        task.markAsDone();
        return task;
    }

    /**
     * Marks a task as incomplete and returns it for display.
     *
     * @param index zero-based task index
     * @return unmarked task
     */
    public Task unmarkTask(int index) {
        Task task = tasks.get(index);
        task.unmarkAsDone();
        return task;
    }

    /**
     * Removes and returns the task at the given zero-based index.
     *
     * @param index zero-based task index
     * @return removed task
     */
    public Task deleteTask(int index) {
        return tasks.remove(index);
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
}
