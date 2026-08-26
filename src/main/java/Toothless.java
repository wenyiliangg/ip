import java.nio.file.Path;
import java.util.Scanner;

/**
 * Starts the Toothless chatbot application.
 */
public class Toothless {
    private static final String DIVIDER = "____________________________________________________________";
    private static final Path DATA_FILE = Path.of("data", "toothless.txt");

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
    public static void main(String[] args) {
        run(new Storage(DATA_FILE));
    }

    /**
     * Runs the chatbot using the supplied storage destination.
     *
     * @param storage storage used after task-list changes
     */
    static void run(Storage storage) {
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
        System.out.println("  - deadline [DESCRIPTION] /by [yyyy-MM-dd]");
        System.out.println("  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]");
        System.out.println("You can also type list to see all our quests. Tiny roar! ★");
        System.out.println(DIVIDER);

        Scanner scanner = new Scanner(System.in);
        TaskList taskList;
        try {
            StorageLoadResult loadResult = storage.load();
            taskList = loadResult.getTaskList();
            if (loadResult.getMalformedLineCount() > 0) {
                System.out.println(formatMalformedDataMessage(loadResult.getMalformedLineCount()));
                System.out.println("He skipped them and kept every task he could understand.");
                System.out.println(DIVIDER);
            }
        } catch (StorageException exception) {
            taskList = new TaskList();
            System.out.println("Toothless had trouble reading his saved quests.");
            System.out.println("He'll start with an empty cave, but the saved file was left untouched.");
            System.out.println(DIVIDER);
        }

        Parser parser = new Parser();
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            try {
                Parser.ParsedCommand parsedCommand = parser.parse(input);
                Command command = parsedCommand.getCommand();
                String details = parsedCommand.getDetails();

                if (command == Command.BYE) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(DIVIDER);
                    break;
                } else if (command == Command.LIST) {
                    if (taskList.isEmpty()) {
                        System.out.println("Your task list is empty. Ready for a new adventure!");
                    } else {
                        System.out.println("Here are the tasks in your list:");
                        for (int i = 0; i < taskList.size(); i++) {
                            System.out.println((i + 1) + "." + taskList.getTask(i));
                        }
                    }
                } else if (command == Command.MARK) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task markedTask = taskList.markTask(taskIndex);
                    System.out.println("A happy little roar! I've starred this task as done:");
                    System.out.println("  " + markedTask);
                    saveTasks(storage, taskList);
                } else if (command == Command.UNMARK) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task selectedTask = taskList.getTask(taskIndex);
                    if (!selectedTask.isDone()) {
                        System.out.println("This task wasn't marked as done before, little rider:");
                        System.out.println("  " + selectedTask);
                    } else {
                        Task unmarkedTask = taskList.unmarkTask(taskIndex);
                        System.out.println("All right, little rider! I've unstarred this task for now:");
                        System.out.println("  " + unmarkedTask);
                        saveTasks(storage, taskList);
                    }
                } else if (command == Command.DELETE) {
                    int taskIndex = parser.parseTaskIndex(command, details, taskList.size());
                    Task deletedTask = taskList.deleteTask(taskIndex);
                    System.out.println("A tiny farewell roar! Toothless has removed this task:");
                    System.out.println("  " + deletedTask);
                    System.out.println(formatTaskCount(taskList.size()));
                    saveTasks(storage, taskList);
                } else if (command == Command.TODO) {
                    Todo todo = parser.parseTodo(details);
                    taskList.addTask(todo);
                    printTaskAdded(todo, taskList.size());
                    saveTasks(storage, taskList);
                } else if (command == Command.DEADLINE) {
                    Deadline deadline = parser.parseDeadline(details);
                    taskList.addTask(deadline);
                    printTaskAdded(deadline, taskList.size());
                    saveTasks(storage, taskList);
                } else if (command == Command.EVENT) {
                    Event event = parser.parseEvent(details);
                    taskList.addTask(event);
                    printTaskAdded(event, taskList.size());
                    saveTasks(storage, taskList);
                } else {
                    throw new IllegalStateException("Parser returned an unsupported command");
                }
            } catch (ToothlessException exception) {
                System.out.println(exception.getMessage());
            }
            System.out.println(DIVIDER);
        }

        scanner.close();
    }

    /**
     * Saves a changed task list while keeping it available after an expected failure.
     */
    private static void saveTasks(Storage storage, TaskList taskList) {
        try {
            storage.save(taskList);
        } catch (StorageException exception) {
            System.out.println("Toothless couldn’t tuck these changes into his data file.");
            System.out.println("They’re still safe for this adventure, but may not return next time.");
        }
    }

    /**
     * Formats the corrupted-data warning with correct singular or plural grammar.
     */
    private static String formatMalformedDataMessage(int malformedLineCount) {
        String lineWord = malformedLineCount == 1 ? "line" : "lines";
        return "Toothless found " + malformedLineCount + " puzzling " + lineWord
                + " in his saved quests.";
    }
}
