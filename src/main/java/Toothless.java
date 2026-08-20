import java.util.Scanner;

/**
 * Starts the Toothless chatbot application.
 */
public class Toothless {
    private static final int MAX_TASKS = 100;
    /** Cute responses are rotated so adding several tasks does not feel repetitive. */
    private static final String[] ADD_RESPONSES = {
        "Got it, little rider! I've added: %s",
        "A tiny happy roar! I'll remember: %s",
        "Safely tucked under my wing: %s",
        "Another quest for us! I've noted: %s"
    };
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
            } else {
                tasks[taskCount] = new Task(command);
                taskCount++;
                int responseIndex = (taskCount - 1) % ADD_RESPONSES.length;
                System.out.printf((ADD_RESPONSES[responseIndex]) + "%n", command);
            }
            System.out.println(divider);
        }

        scanner.close();
    }
}
