/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    private static final String DONE_MARK = "★";
    private static final String NOT_DONE_MARK = " ";

    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description description of the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as incomplete.
     */
    public void unmarkAsDone() {
        this.isDone = false;
    }

    /**
     * Returns whether this task has been completed.
     *
     * @return true if this task is done; false otherwise
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return a star when completed, or a space when incomplete
     */
    public String getStatusIcon() {
        return isDone ? DONE_MARK : NOT_DONE_MARK;
    }

    /**
     * Returns the task description for use by subclasses when displaying a task.
     *
     * @return description of this task
     */
    protected String getDescription() {
        return description;
    }

    /**
     * Returns this task in the format used by the chatbot.
     *
     * @return the status icon and task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
