import java.time.LocalDate;

/**
 * Coordinates creating a deadline and persisting the updated task list.
 */
public class DeadlineCommand extends Command {
    private final String description;
    private final LocalDate by;

    /**
     * Creates a deadline command from validated task details.
     *
     * @param description deadline description obtained from the parser
     * @param by date obtained from the parser
     */
    public DeadlineCommand(String description, LocalDate by) {
        this.description = description;
        this.by = by;
    }

    /**
     * Creates and adds the deadline, displays it, and saves the task list.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        Deadline deadline = new Deadline(description, by);
        taskList.addTask(deadline);
        ui.showTaskAdded(deadline, taskList.size());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
