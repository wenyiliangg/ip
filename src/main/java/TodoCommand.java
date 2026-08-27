/**
 * Coordinates creating a todo and persisting the updated task list.
 */
public class TodoCommand extends Command {
    private final String description;

    /**
     * Creates a todo command for a validated description.
     *
     * @param description todo description obtained from the parser
     */
    public TodoCommand(String description) {
        this.description = description;
    }

    /**
     * Creates and adds the todo, displays it, and saves the updated task list.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        Todo todo = new Todo(description);
        taskList.addTask(todo);
        ui.showTaskAdded(todo, taskList.size());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
