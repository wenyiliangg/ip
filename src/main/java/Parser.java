import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Interprets user input and validates the details supplied to Toothless commands.
 */
public class Parser {
    private static final String COMMANDS =
            "todo, deadline, event, list, mark, unmark, delete, or bye";

    /**
     * Splits one line of user input into its command and remaining details.
     *
     * @param input complete line entered by the user
     * @return parsed command and its details
     * @throws ToothlessException if the input is empty or does not name a valid command
     */
    public ParsedCommand parse(String input) throws ToothlessException {
        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            throw new ToothlessException("Toothless heard a tiny silence. What should he do?\n"
                    + "Try " + COMMANDS + ".");
        }

        String[] commandParts = trimmedInput.split("\\s+", 2);
        CommandType commandType = CommandType.fromKeyword(commandParts[0]);
        String details = commandParts.length == 2 ? commandParts[1].trim() : "";
        if (commandType == CommandType.UNKNOWN
                || commandType == CommandType.BYE && !details.isEmpty()) {
            throw new ToothlessException(
                    "Toothless tilted his head—he doesn’t recognise that command.\n"
                            + "Try " + COMMANDS + ".");
        }
        if (commandType == CommandType.LIST && !details.isEmpty()) {
            throw new ToothlessException("The list command doesn't need extra words.\n"
                    + "Try: list");
        }
        return new ParsedCommand(commandType, details);
    }

    /**
     * Validates and returns the task index supplied to a task command.
     *
     * @param commandType command type whose argument is being checked
     * @param argument text following the command name
     * @param taskCount current number of tasks
     * @return zero-based index of the selected task
     * @throws ToothlessException if the task number is absent or invalid
     */
    public int parseTaskIndex(CommandType commandType, String argument, int taskCount)
            throws ToothlessException {
        int taskNumber = parseTaskNumber(commandType, argument, taskCount);
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new ToothlessException(
                    "Toothless can’t find task " + taskNumber + " in the cave.\n"
                            + "Please choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }

    /**
     * Parses the one-based task number supplied to a task command.
     *
     * @param commandType command type whose argument is being checked
     * @param argument text following the command name
     * @param taskCount current number of tasks
     * @return parsed one-based task number
     * @throws ToothlessException if the task number is absent or not numeric
     */
    public int parseTaskNumber(CommandType commandType, String argument, int taskCount)
            throws ToothlessException {
        String commandName = commandType.toString();
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
        return taskNumber;
    }

    /**
     * Parses a todo description after confirming that it is present.
     *
     * @param details text following the todo command
     * @return validated todo description
     * @throws ToothlessException if the description is empty
     */
    public String parseTodoDescription(String details) throws ToothlessException {
        if (details.isBlank()) {
            throw new ToothlessException("Toothless couldn’t find a description for that todo.\n"
                    + "Try: todo borrow book");
        }
        return details.trim();
    }

    /**
     * Parses and validates a deadline description and finishing date.
     *
     * @param details text following the deadline command
     * @return validated deadline details
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    public ParsedDeadline parseDeadlineDetails(String details) throws ToothlessException {
        String trimmed = details.trim();
        int byIndex = findSeparator(trimmed, "/by", 0);
        if (byIndex < 0) {
            throw new ToothlessException("This deadline is missing '/by' and its date.\n"
                    + "Try: deadline return book /by 2019-12-02");
        }
        if (findSeparator(trimmed, "/by", byIndex + 3) >= 0
                || containsSeparator(trimmed, "/from") || containsSeparator(trimmed, "/to")) {
            throw new ToothlessException("This deadline's format has Toothless puzzled.\n"
                    + "Please use: deadline DESCRIPTION /by yyyy-MM-dd");
        }
        String description = trimmed.substring(0, byIndex).trim();
        String by = trimmed.substring(byIndex + 3).trim();
        if (description.isEmpty()) {
            throw new ToothlessException(
                    "Toothless couldn’t find a description for that deadline.\n"
                            + "Try: deadline return book /by 2019-12-02");
        }
        if (by.isEmpty()) {
            throw new ToothlessException("This deadline is missing its date.\n"
                    + "Try: deadline return book /by 2019-12-02");
        }
        try {
            LocalDate date = DeadlineDate.parse(by);
            return new ParsedDeadline(description, date);
        } catch (DateTimeParseException exception) {
            throw new ToothlessException("That deadline date made Toothless tilt his head.\n"
                    + "Please use a real date in yyyy-MM-dd format.");
        }
    }

    /**
     * Holds validated values parsed from a deadline command.
     */
    public static final class ParsedDeadline {
        private final String description;
        private final LocalDate by;

        private ParsedDeadline(String description, LocalDate by) {
            this.description = description;
            this.by = by;
        }

        /**
         * Returns the parsed deadline description.
         *
         * @return deadline description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the parsed deadline date.
         *
         * @return deadline date
         */
        public LocalDate getBy() {
            return by;
        }
    }

    /**
     * Parses and validates an event description, start, and end.
     *
     * @param details text following the event command
     * @return validated event details
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    public ParsedEvent parseEventDetails(String details) throws ToothlessException {
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
        return new ParsedEvent(description, from, to);
    }

    /**
     * Holds validated values parsed from an event command.
     */
    public static final class ParsedEvent {
        private final String description;
        private final String from;
        private final String to;

        private ParsedEvent(String description, String from, String to) {
            this.description = description;
            this.from = from;
            this.to = to;
        }

        /**
         * Returns the parsed event description.
         *
         * @return event description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the parsed event starting time.
         *
         * @return event starting time
         */
        public String getFrom() {
            return from;
        }

        /**
         * Returns the parsed event ending time.
         *
         * @return event ending time
         */
        public String getTo() {
            return to;
        }
    }

    /**
     * Returns whether a separator occurs as a separate input token.
     */
    private boolean isSeparatorAt(String text, int index, String separator) {
        boolean hasLeftBoundary = index == 0 || Character.isWhitespace(text.charAt(index - 1));
        int endIndex = index + separator.length();
        boolean hasRightBoundary = endIndex == text.length()
                || Character.isWhitespace(text.charAt(endIndex));
        return hasLeftBoundary && hasRightBoundary;
    }

    /**
     * Returns whether the text contains the given separator token.
     */
    private boolean containsSeparator(String text, String separator) {
        return findSeparator(text, separator, 0) >= 0;
    }

    /**
     * Finds a separator that occurs as a complete token rather than inside a value.
     */
    private int findSeparator(String text, String separator, int fromIndex) {
        int index = text.indexOf(separator, fromIndex);
        while (index >= 0 && !isSeparatorAt(text, index, separator)) {
            index = text.indexOf(separator, index + 1);
        }
        return index;
    }

    /**
     * Holds the command keyword and remaining details parsed from one input line.
     */
    public static final class ParsedCommand {
        private final CommandType commandType;
        private final String details;

        private ParsedCommand(CommandType commandType, String details) {
            this.commandType = commandType;
            this.details = details;
        }

        /**
         * Returns the command type selected by the user.
         *
         * @return parsed command type
         */
        public CommandType getCommandType() {
            return commandType;
        }

        /**
         * Returns the trimmed text that followed the command keyword.
         *
         * @return command details, or an empty string when none were supplied
         */
        public String getDetails() {
            return details;
        }
    }
}
