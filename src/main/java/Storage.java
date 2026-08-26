import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeParseException;
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
     * @param taskList tasks to save
     * @throws StorageException if the file cannot be written safely
     */
    public void save(TaskList taskList) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            lines.add(serialize(taskList.getTask(i)));
        }

        Path temporaryFile = null;
        try {
            Path parentDirectory = dataFile.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Path temporaryDirectory = parentDirectory == null ? Path.of(".") : parentDirectory;
            String temporaryPrefix = dataFile.getFileName().toString();
            if (temporaryPrefix.length() < 3) {
                temporaryPrefix = (temporaryPrefix + "___").substring(0, 3);
            }
            temporaryFile = Files.createTempFile(temporaryDirectory, temporaryPrefix, ".tmp");
            Files.write(temporaryFile, lines, StandardCharsets.UTF_8);
            replaceDataFile(temporaryFile);
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryFile);
            throw new StorageException("Unable to save tasks", exception);
        }
    }

    /**
     * Loads every saved task from the data file.
     *
     * @return valid tasks and the number of malformed lines skipped
     * @throws StorageException if an existing file cannot be read
     */
    public StorageLoadResult load() throws StorageException {
        TaskList taskList = new TaskList();
        if (Files.notExists(dataFile)) {
            return new StorageLoadResult(taskList, 0);
        }
        int malformedLineCount = 0;
        try {
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
                try {
                    taskList.addTask(deserialize(line));
                } catch (IllegalArgumentException | DateTimeParseException exception) {
                    malformedLineCount++;
                }
            }
        } catch (IOException exception) {
            throw new StorageException("Unable to load tasks", exception);
        }
        return new StorageLoadResult(taskList, malformedLineCount);
    }

    /**
     * Replaces the data file atomically when the file system supports it.
     */
    private void replaceDataFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, dataFile, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Removes an incomplete temporary file without hiding the original failure.
     */
    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // The original save failure is more useful to the caller.
        }
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
                    deadline.getBy().toString());
        }
        if (task instanceof Event event) {
            return joinFields("E", status, escape(event.getDescription()),
                    escape(event.getFrom()), escape(event.getTo()));
        }
        throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
    }

    /**
     * Reconstructs one task from its saved representation.
     */
    private Task deserialize(String line) {
        if (line.isBlank()) {
            throw new IllegalArgumentException("Saved task line is empty");
        }
        List<String> fields = splitFields(line);
        String taskType = fields.get(0);
        int expectedFieldCount = switch (taskType) {
        case "T" -> 3;
        case "D" -> 4;
        case "E" -> 5;
        default -> throw new IllegalArgumentException("Unknown saved task type: " + taskType);
        };
        if (fields.size() != expectedFieldCount) {
            throw new IllegalArgumentException("Unexpected number of saved task fields");
        }

        String status = fields.get(1);
        if (!status.equals("0") && !status.equals("1")) {
            throw new IllegalArgumentException("Unknown saved completion status");
        }

        Task task = switch (taskType) {
        case "T" -> new Todo(unescape(fields.get(2)));
        case "D" -> new Deadline(unescape(fields.get(2)), DeadlineDate.parse(fields.get(3)));
        case "E" -> new Event(unescape(fields.get(2)), unescape(fields.get(3)),
                unescape(fields.get(4)));
        default -> throw new IllegalStateException("Task type was already validated");
        };
        if (task.getDescription().isEmpty()
                || task instanceof Event event
                        && (event.getFrom().isEmpty() || event.getTo().isEmpty())) {
            throw new IllegalArgumentException("Saved task has an empty required field");
        }
        if (status.equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Splits fields only at separators that have not been escaped.
     */
    private List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaping) {
                field.append(character);
                escaping = false;
            } else if (character == '\\') {
                field.append(character);
                escaping = true;
            } else if (character == '|') {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString().trim());
        return fields;
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

    /**
     * Restores escaped task data without treating it as storage syntax.
     */
    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character != '\\') {
                result.append(character);
                continue;
            }
            if (i + 1 >= value.length()) {
                throw new IllegalArgumentException("Incomplete escape sequence");
            }
            char escapedCharacter = value.charAt(++i);
            switch (escapedCharacter) {
            case '\\', '|' -> result.append(escapedCharacter);
            case 'n' -> result.append('\n');
            case 'r' -> result.append('\r');
            default -> throw new IllegalArgumentException("Unknown escape sequence");
            }
        }
        return result.toString();
    }
}
