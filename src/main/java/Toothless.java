import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Starts the Toothless chatbot application.
 */
public class Toothless {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String COMMANDS = "todo, deadline, event, list, mark, unmark, delete, or bye";
    private static final Path DATA_FILE = Path.of("data", "toothless.txt");

    /**
     * Validates and returns the task index supplied to a task command.
     *
     * @param commandName command whose argument is being checked
     * @param argument text following the command name
     * @param taskCount current number of tasks
     * @return zero-based index of the selected task
     * @throws ToothlessException if the task number is absent or invalid
     */
    private static int parseTaskIndex(String commandName, String argument, int taskCount)
            throws ToothlessException {
        if (taskCount == 0) {
            throw new ToothlessException("Toothless's cave is empty, so there is no task to "
                    + commandName + ".\nAdd a task first, then try again.");
        }
        if (argument.isBlank()) {
            throw new ToothlessException("Toothless needs a task number to " + commandName + ".\n"
                    + "Try: " + commandName + " 1");
        }
        if (!argument.matches("[+-]?\\d+")) {
            throw new ToothlessException("That task number looks a little unusual.\n"
                    + "Please use a whole number, like: " + commandName + " 1");
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            throw new ToothlessException("That task number is too large for Toothless to count.\n"
                    + "Please choose a number from 1 to " + taskCount + ".");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new ToothlessException("Toothless can’t find task " + taskNumber + " in the cave.\n"
                    + "Please choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }

    /**
     * Parses a Todo only after confirming that its description is present.
     *
     * @param details text following the todo command
     * @return validated Todo
     * @throws ToothlessException if the description is empty
     */
    private static Todo parseTodo(String details) throws ToothlessException {
        if (details.isBlank()) {
            throw new ToothlessException("Toothless couldn’t find a description for that todo.\n"
                    + "Try: todo borrow book");
        }
        return new Todo(details.trim());
    }

    /**
     * Parses a Deadline and validates its description and finishing time.
     *
     * @param details text following the deadline command
     * @return validated Deadline
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    private static Deadline parseDeadline(String details) throws ToothlessException {
        String trimmed = details.trim();
        int byIndex = findSeparator(trimmed, "/by", 0);
        if (byIndex < 0) {
            throw new ToothlessException("This deadline is missing '/by' and its finishing time.\n"
                    + "Try: deadline return book /by Sunday");
        }
        if (findSeparator(trimmed, "/by", byIndex + 3) >= 0 || containsSeparator(trimmed, "/from")
                || containsSeparator(trimmed, "/to")) {
            throw new ToothlessException("This deadline's format has Toothless puzzled.\n"
                    + "Try: deadline DESCRIPTION /by TIME");
        }
        String description = trimmed.substring(0, byIndex).trim();
        String by = trimmed.substring(byIndex + 3).trim();
        if (description.isEmpty()) {
            throw new ToothlessException("Toothless couldn’t find a description for that deadline.\n"
                    + "Try: deadline return book /by Sunday");
        }
        if (by.isEmpty()) {
            throw new ToothlessException("This deadline is missing its finishing time.\n"
                    + "Try: deadline return book /by Sunday");
        }
        return new Deadline(description, by);
    }

    /**
     * Parses an Event and validates its description, start, and end.
     *
     * @param details text following the event command
     * @return validated Event
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    private static Event parseEvent(String details) throws ToothlessException {
        String trimmed = details.trim();
        int fromIndex = findSeparator(trimmed, "/from", 0);
        int toIndex = findSeparator(trimmed, "/to", 0);
        if (fromIndex < 0) {
            throw new ToothlessException("This event is missing its starting time after '/from'.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        if (toIndex < 0) {
            throw new ToothlessException("This event is missing its ending time after '/to'.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        if (toIndex < fromIndex) {
            throw new ToothlessException("The event's '/from' must come before '/to'.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        if (findSeparator(trimmed, "/from", fromIndex + 5) >= 0
                || findSeparator(trimmed, "/to", toIndex + 3) >= 0
                || containsSeparator(trimmed, "/by")) {
            throw new ToothlessException("This event's format has Toothless puzzled.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        String description = trimmed.substring(0, fromIndex).trim();
        String from = trimmed.substring(fromIndex + 5, toIndex).trim();
        String to = trimmed.substring(toIndex + 3).trim();
        if (description.isEmpty()) {
            throw new ToothlessException("Toothless couldn’t find a description for that event.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        if (from.isEmpty()) {
            throw new ToothlessException("This event is missing its starting time.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        if (to.isEmpty()) {
            throw new ToothlessException("This event is missing its ending time.\n"
                    + "Try: event DESCRIPTION /from START /to END");
        }
        return new Event(description, from, to);
    }

    /**
     * Returns whether a separator occurs as a separate input token.
     */
    private static boolean isSeparatorAt(String text, int index, String separator) {
        boolean hasLeftBoundary = index == 0 || Character.isWhitespace(text.charAt(index - 1));
        int endIndex = index + separator.length();
        boolean hasRightBoundary = endIndex == text.length()
                || Character.isWhitespace(text.charAt(endIndex));
        return hasLeftBoundary && hasRightBoundary;
    }

    /**
     * Returns whether the text contains the given separator token.
     */
    private static boolean containsSeparator(String text, String separator) {
        return findSeparator(text, separator, 0) >= 0;
    }

    /**
     * Finds a separator that occurs as a complete token rather than inside a value.
     */
    private static int findSeparator(String text, String separator, int fromIndex) {
        int index = text.indexOf(separator, fromIndex);
        while (index >= 0 && !isSeparatorAt(text, index, separator)) {
            index = text.indexOf(separator, index + 1);
        }
        return index;
    }

    /**
     * Prints a friendly confirmation after a task is added.
     *
     * @param task task that was added
     * @param taskCount current number of tasks
     */
    private static void printTaskAdded(Task task, int taskCount) {
        System.out.println("Got it! Toothless has added this task for you:");
        System.out.println("  " + task);
        System.out.println(formatTaskCount(taskCount) + " ★");
    }

    /**
     * Formats the current task count with correct singular or plural grammar.
     *
     * @param taskCount current number of tasks
     * @return sentence describing the current task count
     */
    private static String formatTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        return "Now you have " + taskCount + " " + taskWord + " in the list.";
    }

    /**
     * Runs the chatbot and responds to commands entered by the user.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args) throws IOException {
        run(new Storage(DATA_FILE));
    }

    /**
     * Runs the chatbot using the supplied storage destination.
     *
     * @param storage storage used after task-list changes
     * @throws IOException if the changed task list cannot be saved
     */
    static void run(Storage storage) throws IOException {
        String banner = "  __/\\__           __/\\__\n"
                + " /     \\_________/     \\\n"
                + "/   /\\   O     O   /\\   \\\n"
                + "\\__/  \\     ^     /  \\__/\n"
                + "       \\  \\___/  /\n"
                + "    ____|       |____\n"
                + " __/    |       |    \\__\n"
                + "/___/   /|_______|\\   \\___\\\n"
                + "        /_/     \\_\\";

        System.out.println(DIVIDER);
        System.out.println(banner);
        System.out.println();
        System.out.println("Hi there! I'm Toothless. It's wonderful to meet you!");
        System.out.println("What can I do for you today?");
        System.out.println("Ready for our next little adventure? Tell me what to remember:");
        System.out.println("  - todo [DESCRIPTION]");
        System.out.println("  - deadline [DESCRIPTION] /by [DATE_OR_TIME]");
        System.out.println("  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]");
        System.out.println("You can also type list to see all our quests. Tiny roar! ★");
        System.out.println(DIVIDER);

        Scanner scanner = new Scanner(System.in);
        TaskList taskList = storage.load();

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            try {
                if (input.isEmpty()) {
                    throw new ToothlessException("Toothless heard a tiny silence. What should he do?\n"
                            + "Try " + COMMANDS + ".");
                }
                String[] commandParts = input.split("\\s+", 2);
                Command command = Command.fromKeyword(commandParts[0]);
                String details = commandParts.length == 2 ? commandParts[1].trim() : "";

                if (command == Command.BYE && details.isEmpty()) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(DIVIDER);
                    break;
                } else if (command == Command.LIST) {
                    if (!details.isEmpty()) {
                        throw new ToothlessException("The list command doesn't need extra words.\n"
                                + "Try: list");
                    }
                    if (taskList.isEmpty()) {
                        System.out.println("Your task list is empty. Ready for a new adventure!");
                    } else {
                        System.out.println("Here are the tasks in your list:");
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + "." + taskList.getTask(i));
                        }
                    }
                } else if (command == Command.MARK) {
                    int taskIndex = parseTaskIndex(command.toString(), details, taskList.size());
                    Task markedTask = taskList.markTask(taskIndex);
                    storage.save(taskList);
                    System.out.println("A happy little roar! I've starred this task as done:");
                    System.out.println("  " + markedTask);
                } else if (command == Command.UNMARK) {
                    int taskIndex = parseTaskIndex(command.toString(), details, taskList.size());
                    Task selectedTask = taskList.getTask(taskIndex);
                    if (!selectedTask.isDone()) {
                        System.out.println("This task wasn't marked as done before, little rider:");
                        System.out.println("  " + selectedTask);
                    } else {
                        Task unmarkedTask = taskList.unmarkTask(taskIndex);
                        storage.save(taskList);
                        System.out.println("All right, little rider! I've unstarred this task for now:");
                        System.out.println("  " + unmarkedTask);
                    }
                } else if (command == Command.DELETE) {
                    int taskIndex = parseTaskIndex(command.toString(), details, taskList.size());
                    Task deletedTask = taskList.deleteTask(taskIndex);
                    storage.save(taskList);
                    System.out.println("A tiny farewell roar! Toothless has removed this task:");
                    System.out.println("  " + deletedTask);
                    System.out.println(formatTaskCount(taskList.size()));
                } else if (command == Command.TODO) {
                    Todo todo = parseTodo(details);
                    taskList.addTask(todo);
                    storage.save(taskList);
                    printTaskAdded(todo, taskList.size());
                } else if (command == Command.DEADLINE) {
                    Deadline deadline = parseDeadline(details);
                    taskList.addTask(deadline);
                    storage.save(taskList);
                    printTaskAdded(deadline, taskList.size());
                } else if (command == Command.EVENT) {
                    Event event = parseEvent(details);
                    taskList.addTask(event);
                    storage.save(taskList);
                    printTaskAdded(event, taskList.size());
                } else {
                    throw new ToothlessException("Toothless tilted his head—he doesn’t recognise that command.\n"
                            + "Try " + COMMANDS + ".");
                }
            } catch (ToothlessException exception) {
                System.out.println(exception.getMessage());
            }
            System.out.println(DIVIDER);
        }

        scanner.close();
    }
}
