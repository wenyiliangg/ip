/**
 * Represents an executable command in the Toothless chatbot.
 */
public abstract class Command {
    /**
     * Performs this command using the application's collaborators.
     *
     * @param taskList tasks available to the command
     * @param ui user interface used to present command results
     * @param storage storage used to persist task changes
     * @throws ToothlessException if the command cannot be completed
     */
    public abstract void execute(TaskList taskList, Ui ui, Storage storage)
            throws ToothlessException;

    /**
     * Returns whether this command should end the application loop.
     *
     * @return true when the application should stop after execution
     */
    public boolean isExit() {
        return false;
    }
}
