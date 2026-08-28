package toothless.command;

import toothless.storage.Storage;
import toothless.storage.StorageException;
import toothless.task.Task;
import toothless.task.TaskList;
import toothless.ui.Ui;

/**
 * Provides the shared workflow for commands that create and add a task.
 */
public abstract class AddCommand extends Command {
    /**
     * Creates the command-specific task before it is added to the task list.
     *
     * @return task created by the concrete command
     */
    protected abstract Task createTask();

    /**
     * Adds the created task, displays it, and saves the updated task list.
     *
     * @param taskList list that receives the newly created task
     * @param ui user interface used to confirm the addition or report a save failure
     * @param storage storage used to persist the updated task list
     */
    @Override
    public final void execute(TaskList taskList, Ui ui, Storage storage) {
        Task task = createTask();
        taskList.addTask(task);
        ui.showTaskAdded(task, taskList.size());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
