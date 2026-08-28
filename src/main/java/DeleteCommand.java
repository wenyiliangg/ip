import toothless.exception.ToothlessException;
import toothless.task.Task;
import toothless.task.TaskList;

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
