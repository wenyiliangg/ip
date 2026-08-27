/**
 * Coordinates displaying the current task list.
 */
public class ListCommand extends Command {
    /**
     * Asks the user interface to display the tasks without changing them.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        ui.showTaskList(taskList);
    }
}
