import toothless.storage.Storage;
import toothless.task.TaskList;

/**
 * Displays Toothless's farewell and ends the current application session.
 */
public class ExitCommand extends Command {
    /**
     * Displays the farewell message for this session.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        ui.showGoodbye();
    }

    /**
     * Identifies this command as the command that ends the application loop.
     *
     * @return true because this command exits the application
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
