import java.util.Scanner;

/**
 * Starts the Toothless chatbot application.
 */
public class Toothless {
    private static final int MAX_TASKS = 100;

    /**
     * Prints a friendly confirmation after a task is added.
     *
     * @param task task that was added
     * @param taskCount current number of tasks
     */
    private static void printTaskAdded(Task task, int taskCount) {
        System.out.println("Got it! Toothless has added this task for you:");
        System.out.println("  " + task);
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + taskCount + " " + taskWord + " in the list. ★");
    }
    /**
     * Runs the chatbot and responds to commands entered by the user.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args) {
        String divider = "____________________________________________________________";
        String banner = "  __/\\__           __/\\__\n"
                + " /     \\_________/     \\\n"
                + "/   /\\   O     O   /\\   \\\n"
                + "\\__/  \\     ^     /  \\__/\n"
                + "       \\  \\___/  /\n"
                + "    ____|       |____\n"
                + " __/    |       |    \\__\n"
                + "/___/   /|_______|\\   \\___\\\n"
                + "        /_/     \\_\\";

        System.out.println(divider);
        System.out.println(banner);
        System.out.println();
        System.out.println("Hi there! I'm Toothless. It's wonderful to meet you!");
        System.out.println("What can I do for you today?");
        System.out.println("Ready for our next little adventure? Tell me what to remember:");
        System.out.println("  - todo [DESCRIPTION]");
        System.out.println("  - deadline [DESCRIPTION] /by [DATE_OR_TIME]");
        System.out.println("  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]");
        System.out.println("You can also type list to see all our quests. Tiny roar! ★");
        System.out.println(divider);

        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            }

            if (command.equals("list")) {
                if (taskCount == 0) {
                    System.out.println("Your task list is empty. Ready for a new adventure!");
                } else {
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + "." + tasks[i]);
                    }
                }
            } else if (command.equals("mark") || command.startsWith("mark ")) {
                String taskNumberText = command.substring("mark".length()).trim();
                try {
                    int taskIndex = Integer.parseInt(taskNumberText) - 1;
                    if (taskIndex < 0 || taskIndex >= taskCount) {
                        System.out.println("Oops! I can't find that task in our adventure list.");
                    } else {
                        tasks[taskIndex].markAsDone();
                        System.out.println("A happy little roar! I've starred this task as done:");
                        System.out.println("  " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException exception) {
                    System.out.println("Please tell me which task to star, like this: mark 2");
                }
            } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                String taskNumberText = command.substring("unmark".length()).trim();
                try {
                    int taskIndex = Integer.parseInt(taskNumberText) - 1;
                    if (taskIndex < 0 || taskIndex >= taskCount) {
                        System.out.println("Oops! I can't find that task in our adventure list.");
                    } else if (!tasks[taskIndex].isDone()) {
                        System.out.println("This task wasn't marked as done before, little rider:");
                        System.out.println("  " + tasks[taskIndex]);
                    } else {
                        tasks[taskIndex].unmarkAsDone();
                        System.out.println("All right, little rider! I've unstarred this task for now:");
                        System.out.println("  " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException exception) {
                    System.out.println("Please tell me which task to unstar, like this: unmark 2");
                }
            } else if (command.equals("delete") || command.startsWith("delete ")) {
                String taskNumberText = command.substring("delete".length()).trim();
                try {
                    int taskIndex = Integer.parseInt(taskNumberText) - 1;
                    if (taskIndex < 0 || taskIndex >= taskCount) {
                        System.out.println("Oops! I can't find that task in our adventure list.");
                    } else {
                        Task deletedTask = tasks[taskIndex];
                        for (int i = taskIndex; i < taskCount - 1; i++) {
                            tasks[i] = tasks[i + 1];
                        }
                        tasks[taskCount - 1] = null;
                        taskCount--;
                        System.out.println("All done! Toothless has removed this task:");
                        System.out.println("  " + deletedTask);
                        System.out.println("Now you have " + taskCount + " tasks in the list. ★");
                    }
                } catch (NumberFormatException exception) {
                    System.out.println("Please tell me which task to remove, like this: delete 2");
                }
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                String description = command.substring("todo".length()).trim();
                tasks[taskCount] = new Todo(description);
                taskCount++;
                printTaskAdded(tasks[taskCount - 1], taskCount);
            } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                String details = command.substring("deadline".length()).trim();
                int byIndex = details.indexOf("/by");
                if (byIndex < 0) {
                    System.out.println("A deadline needs '/by', little rider. Try: deadline TASK /by TIME");
                } else {
                    String description = details.substring(0, byIndex).trim();
                    String by = details.substring(byIndex + "/by".length()).trim();
                    tasks[taskCount] = new Deadline(description, by);
                    taskCount++;
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
            } else if (command.equals("event") || command.startsWith("event ")) {
                String details = command.substring("event".length()).trim();
                int fromIndex = details.indexOf("/from");
                int toIndex = details.indexOf("/to", fromIndex + 1);
                if (fromIndex < 0 || toIndex < 0 || toIndex < fromIndex) {
                    System.out.println("An event needs '/from' and '/to'. Try: event TASK /from START /to END");
                } else {
                    String description = details.substring(0, fromIndex).trim();
                    String from = details.substring(fromIndex + "/from".length(), toIndex).trim();
                    String to = details.substring(toIndex + "/to".length()).trim();
                    tasks[taskCount] = new Event(description, from, to);
                    taskCount++;
                    printTaskAdded(tasks[taskCount - 1], taskCount);
                }
            } else {
                // Preserve Toothless's original behavior: a plain line is a Todo description.
                tasks[taskCount] = new Todo(command);
                taskCount++;
                printTaskAdded(tasks[taskCount - 1], taskCount);
            }
            System.out.println(divider);
        }

        scanner.close();
    }
}
