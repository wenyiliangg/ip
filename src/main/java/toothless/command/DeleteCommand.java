package toothless.command;

import toothless.exception.ToothlessException;
import toothless.storage.Storage;
import toothless.storage.StorageException;
import toothless.task.Task;
import toothless.task.TaskList;
import toothless.ui.Ui;

/**
 * Coordinates deleting one task and persisting the updated task list.
 */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a delete command for a parsed one-based task number.
     *
     * @param taskNumber one-based task number obtained from the parser
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Deletes the selected task, displays it, and saves the updated task list.
     *
     * @param taskList list containing the task selected for deletion
     * @param ui user interface used to confirm the deletion or report a save failure
     * @param storage storage used to persist the updated task list
     * @throws ToothlessException if the selected task no longer exists
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage)
            throws ToothlessException {
        Task deletedTask = taskList.deleteTask(taskNumber);
        ui.showTaskDeleted(deletedTask, taskList.size());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
