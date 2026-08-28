package toothless.command;

import toothless.storage.Storage;
import toothless.task.TaskList;
import toothless.ui.Ui;

/**
 * Coordinates displaying the current task list.
 */
public class ListCommand extends Command {
    /**
     * Asks the user interface to display the tasks without changing them.
     *
     * @param taskList tasks to display in their current order
     * @param ui user interface used to display the task list
     * @param storage configured storage, which is not needed by this command
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        ui.showTaskList(taskList);
    }
}
