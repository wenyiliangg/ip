import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves Toothless tasks in a text file that is separate from chatbot input.
 */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";

    private final Path dataFile;

    /**
     * Creates storage that writes to the given data file.
     *
     * @param dataFile file used to store tasks
     */
    public Storage(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Replaces the saved file with the current tasks.
     *
     * <p>The destination directory is expected to exist at this stage.</p>
     *
     * @param taskList tasks to save
     * @throws IOException if the file cannot be written
     */
    public void save(TaskList taskList) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            lines.add(serialize(taskList.getTask(i)));
        }
        Files.write(dataFile, lines, StandardCharsets.UTF_8);
    }

    /**
     * Converts one task into its reversible saved representation.
     */
    private String serialize(Task task) {
        String status = task.isDone() ? "1" : "0";
        if (task instanceof Todo) {
            return joinFields("T", status, escape(task.getDescription()));
        }
        if (task instanceof Deadline deadline) {
            return joinFields("D", status, escape(deadline.getDescription()),
                    escape(deadline.getBy()));
        }
        if (task instanceof Event event) {
            return joinFields("E", status, escape(event.getDescription()),
                    escape(event.getFrom()), escape(event.getTo()));
        }
        throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
    }

    /**
     * Joins saved fields using the storage format's visible separator.
     */
    private String joinFields(String... fields) {
        return String.join(FIELD_SEPARATOR, fields);
    }

    /**
     * Escapes characters that otherwise conflict with line or field boundaries.
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
