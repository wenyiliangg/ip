/**
 * Coordinates creating an event and persisting the updated task list.
 */
public class EventCommand extends Command {
    private final String description;
    private final String from;
    private final String to;

    /**
     * Creates an event command from validated task details.
     *
     * @param description event description obtained from the parser
     * @param from event starting time obtained from the parser
     * @param to event ending time obtained from the parser
     */
    public EventCommand(String description, String from, String to) {
        this.description = description;
        this.from = from;
        this.to = to;
    }

    /**
     * Creates and adds the event, displays it, and saves the task list.
     */
    @Override
    public void execute(TaskList taskList, Ui ui, Storage storage) {
        Event event = new Event(description, from, to);
        taskList.addTask(event);
        ui.showTaskAdded(event, taskList.size());
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            ui.showSaveError();
        }
    }
}
