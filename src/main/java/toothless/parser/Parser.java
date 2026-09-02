package toothless.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import toothless.command.Command;
import toothless.command.CommandType;
import toothless.command.DeadlineCommand;
import toothless.command.DeleteCommand;
import toothless.command.EventCommand;
import toothless.command.ExitCommand;
import toothless.command.FindCommand;
import toothless.command.ListCommand;
import toothless.command.MarkCommand;
import toothless.command.TodoCommand;
import toothless.command.UnmarkCommand;
import toothless.exception.ToothlessException;
import toothless.task.DeadlineDate;

/**
 * Interprets user input and validates the details supplied to Toothless commands.
 */
public class Parser {
    private static final String COMMANDS =
            "todo, deadline, event, list, find, mark, unmark, delete, or bye";

    /**
     * Creates a stateless parser for commands entered during a Toothless session.
     */
    public Parser() {
    }

    /**
     * Interprets one line of input and constructs its executable command.
     *
     * @param input complete line entered by the user.
     * @param taskCount current number of tasks.
     * @return executable command represented by the input
     * @throws ToothlessException if the input is empty, unknown, or malformed
     */
    public Command parse(String input, int taskCount) throws ToothlessException {
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

        switch (commandType) {
            case BYE:
                return new ExitCommand();
            case LIST:
                return new ListCommand();
            case FIND:
                return new FindCommand(parseFindKeyword(details));
            case MARK:
                return new MarkCommand(parseTaskNumber(commandType, details, taskCount));
            case UNMARK:
                return new UnmarkCommand(parseTaskNumber(commandType, details, taskCount));
            case DELETE:
                return new DeleteCommand(parseTaskNumber(commandType, details, taskCount));
            case TODO:
                return new TodoCommand(parseTodoDescription(details));
            case DEADLINE:
                ParsedDeadline deadline = parseDeadlineDetails(details);
                return new DeadlineCommand(deadline.getDescription(), deadline.getBy());
            case EVENT:
                ParsedEvent event = parseEventDetails(details);
                return new EventCommand(event.getDescription(), event.getFrom(), event.getTo());
            default:
                throw new IllegalStateException("Unsupported command type: " + commandType);
        }
    }

    /**
     * Parses the one-based task number supplied to a task command.
     *
     * @param commandType command type whose argument is being checked.
     * @param argument text following the command name.
     * @param taskCount current number of tasks.
     * @return parsed one-based task number
     * @throws ToothlessException if the task number is absent or not numeric
     */
    private int parseTaskNumber(CommandType commandType, String argument, int taskCount)
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
     * @param details text following the todo command.
     * @return validated todo description
     * @throws ToothlessException if the description is empty
     */
    private String parseTodoDescription(String details) throws ToothlessException {
        if (details.isBlank()) {
            throw new ToothlessException("Toothless couldn’t find a description for that todo.\n"
                    + "Try: todo borrow book");
        }
        return details.trim();
    }

    /**
     * Parses a search keyword after confirming that it is present.
     *
     * @param details text following the find command
     * @return validated search keyword
     * @throws ToothlessException if the keyword is empty
     */
    private String parseFindKeyword(String details) throws ToothlessException {
        if (details.isBlank()) {
            throw new ToothlessException("Toothless needs a keyword to sniff out matching tasks.\n"
                    + "Try: find book");
        }
        return details.trim();
    }

    /**
     * Parses and validates a deadline description and finishing date.
     *
     * @param details text following the deadline command.
     * @return validated deadline details
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    private ParsedDeadline parseDeadlineDetails(String details) throws ToothlessException {
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
    private static final class ParsedDeadline {
        private final String description;
        private final LocalDate by;

        /**
         * Groups a validated deadline description with its parsed due date.
         *
         * @param description validated text describing the deadline task
         * @param by parsed date by which the task should be completed
         */
        private ParsedDeadline(String description, LocalDate by) {
            this.description = description;
            this.by = by;
        }

        /**
         * Returns the parsed deadline description.
         *
         * @return deadline description
         */
        private String getDescription() {
            return description;
        }

        /**
         * Returns the parsed deadline date.
         *
         * @return deadline date
         */
        private LocalDate getBy() {
            return by;
        }
    }

    /**
     * Parses and validates an event description, start, and end.
     *
     * @param details text following the event command.
     * @return validated event details
     * @throws ToothlessException if the command structure is incomplete or ambiguous
     */
    private ParsedEvent parseEventDetails(String details) throws ToothlessException {
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
    private static final class ParsedEvent {
        private final String description;
        private final String from;
        private final String to;

        /**
         * Groups the validated text fields needed to create an event task.
         *
         * @param description validated text describing the event
         * @param from validated event starting time
         * @param to validated event ending time
         */
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
        private String getDescription() {
            return description;
        }

        /**
         * Returns the parsed event starting time.
         *
         * @return event starting time
         */
        private String getFrom() {
            return from;
        }

        /**
         * Returns the parsed event ending time.
         *
         * @return event ending time
         */
        private String getTo() {
            return to;
        }
    }

    /**
     * Returns whether a separator occurs as a separate input token.
     *
     * @param text complete command details being inspected
     * @param index starting index of the separator occurrence
     * @param separator separator token expected at the given index
     * @return true when whitespace or a text boundary surrounds the separator
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
     *
     * @param text complete command details being inspected
     * @param separator separator token to locate
     * @return true when the separator occurs as a complete token
     */
    private boolean containsSeparator(String text, String separator) {
        return findSeparator(text, separator, 0) >= 0;
    }

    /**
     * Finds a separator that occurs as a complete token rather than inside a value.
     *
     * @param text complete command details being searched
     * @param separator separator token to locate
     * @param fromIndex index from which to begin searching
     * @return index of the next complete separator token, or {@code -1} if none exists
     */
    private int findSeparator(String text, String separator, int fromIndex) {
        int index = text.indexOf(separator, fromIndex);
        while (index >= 0 && !isSeparatorAt(text, index, separator)) {
            index = text.indexOf(separator, index + 1);
        }
        return index;
    }

}
