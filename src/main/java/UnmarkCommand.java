import toothless.exception.ToothlessException;
import toothless.storage.Storage;
import toothless.storage.StorageException;
import toothless.task.TaskList;

/**
 * Coordinates unmarking one task and persisting the updated task list.
 */
public class UnmarkCommand extends Command {
    private final int taskNumber;

    /**
     * Creates an unmark command for a parsed one-based task number.
     *
     * @param taskNumber one-based task number obtained from the parser
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Unmarks the selected task, displays the result, and saves changed state.
     *
     * @throws ToothlessException if the selected task does not exist
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage)
            throws ToothlessException {
        TaskList.UnmarkResult result = taskList.unmarkTask(taskNumber);
        if (!result.wasChanged()) {
            ui.showTaskAlreadyUnmarked(result.getTask());
            return;
        }

        ui.showTaskUnmarked(result.getTask());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
