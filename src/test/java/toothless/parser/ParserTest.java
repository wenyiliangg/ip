package toothless.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import toothless.command.Command;
import toothless.command.DeleteCommand;
import toothless.command.ExitCommand;
import toothless.command.FindCommand;
import toothless.command.ListCommand;
import toothless.command.MarkCommand;
import toothless.command.UnmarkCommand;
import toothless.exception.ToothlessException;
import toothless.storage.Storage;
import toothless.task.Deadline;
import toothless.task.Event;
import toothless.task.TaskList;
import toothless.task.Todo;
import toothless.ui.Ui;

/**
 * Tests command parsing, parsed values, and malformed-input validation.
 */
public class ParserTest {
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Toothless tilted his head—he doesn’t recognise that command.\n"
                    + "Try todo, deadline, event, list, find, mark, unmark, delete, or bye.";

    @TempDir
    private Path temporaryDirectory;

    /**
     * Verifies commands without arguments map to their corresponding command classes.
     */
    @Test
    public void parse_commandsWithoutArguments_returnsMatchingCommandTypes()
            throws ToothlessException {
        Parser parser = new Parser();

        Command listCommand = parser.parse("  list  ", 2);
        Command exitCommand = parser.parse("bye", 2);

        assertInstanceOf(ListCommand.class, listCommand);
        assertInstanceOf(ExitCommand.class, exitCommand);
        assertFalse(listCommand.isExit());
        assertTrue(exitCommand.isExit());
    }

    /**
     * Verifies descriptions, dates, and event times survive parsing into created tasks.
     */
    @Test
    public void parse_addCommands_createsTasksWithParsedValues() throws ToothlessException {
        Parser parser = new Parser();
        TaskList taskList = new TaskList();

        execute(parser.parse("todo read book", 0), taskList);
        execute(parser.parse("deadline return book /by 2019-12-02", 1), taskList);
        execute(parser.parse("event project meeting /from Monday 2pm /to Monday 3pm", 2),
                taskList);

        Todo todo = assertInstanceOf(Todo.class, taskList.getTask(0));
        Deadline deadline = assertInstanceOf(Deadline.class, taskList.getTask(1));
        Event event = assertInstanceOf(Event.class, taskList.getTask(2));
        assertEquals("read book", todo.getDescription());
        assertEquals("return book", deadline.getDescription());
        assertEquals(LocalDate.of(2019, 12, 2), deadline.getBy());
        assertEquals("project meeting", event.getDescription());
        assertEquals("Monday 2pm", event.getFrom());
        assertEquals("Monday 3pm", event.getTo());
    }

    /**
     * Verifies flexible surrounding whitespace is removed from parsed task values.
     */
    @Test
    public void parse_addCommandsWithExtraWhitespace_trimsParsedValues()
            throws ToothlessException {
        Parser parser = new Parser();
        TaskList taskList = new TaskList();

        execute(parser.parse("  todo    read book   ", 0), taskList);
        execute(parser.parse(" deadline   spaced date   /by   2020-02-29 ", 1), taskList);
        execute(parser.parse(" event   meeting   /from   2pm   /to   3pm  ", 2), taskList);

        assertEquals("read book", taskList.getTask(0).getDescription());
        Deadline deadline = assertInstanceOf(Deadline.class, taskList.getTask(1));
        Event event = assertInstanceOf(Event.class, taskList.getTask(2));
        assertEquals("spaced date", deadline.getDescription());
        assertEquals(LocalDate.of(2020, 2, 29), deadline.getBy());
        assertEquals("meeting", event.getDescription());
        assertEquals("2pm", event.getFrom());
        assertEquals("3pm", event.getTo());
    }

    /**
     * Verifies parsed one-based task numbers are carried into mutating commands.
     */
    @Test
    public void parse_taskNumberCommands_executeAgainstSelectedTask() throws ToothlessException {
        Parser parser = new Parser();
        TaskList taskList = createThreeTaskList();

        Command markCommand = parser.parse("mark 2", taskList.size());
        execute(markCommand, taskList);
        Command unmarkCommand = parser.parse("unmark +2", taskList.size());
        execute(unmarkCommand, taskList);
        Command deleteCommand = parser.parse("delete 2", taskList.size());
        execute(deleteCommand, taskList);

        assertInstanceOf(MarkCommand.class, markCommand);
        assertInstanceOf(UnmarkCommand.class, unmarkCommand);
        assertInstanceOf(DeleteCommand.class, deleteCommand);
        assertFalse(taskList.getTask(0).isDone());
        assertEquals(2, taskList.size());
        assertEquals("first", taskList.getTask(0).getDescription());
        assertEquals("third", taskList.getTask(1).getDescription());
    }

    /**
     * Verifies a find keyword is trimmed and carried into a read-only find command.
     */
    @Test
    public void parse_findWithKeyword_returnsCommandUsingTrimmedKeyword()
            throws ToothlessException {
        Parser parser = new Parser();
        TaskList taskList = new TaskList();
        taskList.addTask(new Todo("read book"));

        Command command = parser.parse("   find    READ book   ", taskList.size());
        String output = execute(command, taskList);

        assertInstanceOf(FindCommand.class, command);
        assertEquals("Here are the matching tasks in your list:\n"
                + "1.[T][ ] read book\n", output);
        assertEquals(1, taskList.size());
        assertFalse(taskList.getTask(0).isDone());
    }

    /**
     * Verifies blank input and unknown command words produce distinct guidance.
     */
    @Test
    public void parse_blankAndUnknownInput_throwFocusedErrors() {
        Parser parser = new Parser();

        ToothlessException blankException = assertThrows(ToothlessException.class,
                () -> parser.parse("   ", 0));
        ToothlessException unknownException = assertThrows(ToothlessException.class,
                () -> parser.parse("fly", 0));

        assertEquals("Toothless heard a tiny silence. What should he do?\n"
                + "Try todo, deadline, event, list, find, mark, unmark, delete, or bye.",
                blankException.getMessage());
        assertEquals(UNKNOWN_COMMAND_MESSAGE, unknownException.getMessage());
    }

    /**
     * Verifies commands that forbid details reject trailing input.
     */
    @Test
    public void parse_byeAndListWithExtraDetails_throwFocusedErrors() {
        Parser parser = new Parser();

        ToothlessException byeException = assertThrows(ToothlessException.class,
                () -> parser.parse("bye later", 1));
        ToothlessException listException = assertThrows(ToothlessException.class,
                () -> parser.parse("list now", 1));

        assertEquals(UNKNOWN_COMMAND_MESSAGE, byeException.getMessage());
        assertEquals("The list command doesn't need extra words.\nTry: list",
                listException.getMessage());
    }

    /**
     * Verifies task-number commands prioritise the empty-list error for unusable input.
     */
    @Test
    public void parse_taskNumberCommandsForEmptyList_throwCommandSpecificErrors() {
        Parser parser = new Parser();

        ToothlessException markException = assertThrows(ToothlessException.class,
                () -> parser.parse("mark", 0));
        ToothlessException unmarkException = assertThrows(ToothlessException.class,
                () -> parser.parse("unmark anything", 0));
        ToothlessException deleteException = assertThrows(ToothlessException.class,
                () -> parser.parse("delete 1", 0));

        assertEquals("Toothless's cave is empty, so there is no task to mark.\n"
                + "Add a task first, then try again.", markException.getMessage());
        assertEquals("Toothless's cave is empty, so there is no task to unmark.\n"
                + "Add a task first, then try again.", unmarkException.getMessage());
        assertEquals("Toothless's cave is empty, so there is no task to delete.\n"
                + "Add a task first, then try again.", deleteException.getMessage());
    }

    /**
     * Verifies an omitted task number names the command whose input is incomplete.
     */
    @Test
    public void parse_taskNumberCommandsWithoutNumber_throwCommandSpecificErrors() {
        Parser parser = new Parser();

        ToothlessException markException = assertThrows(ToothlessException.class,
                () -> parser.parse("mark", 3));
        ToothlessException unmarkException = assertThrows(ToothlessException.class,
                () -> parser.parse("unmark", 3));
        ToothlessException deleteException = assertThrows(ToothlessException.class,
                () -> parser.parse("delete", 3));

        assertEquals("Toothless needs a task number to mark.\nTry: mark 1",
                markException.getMessage());
        assertEquals("Toothless needs a task number to unmark.\nTry: unmark 1",
                unmarkException.getMessage());
        assertEquals("Toothless needs a task number to delete.\nTry: delete 1",
                deleteException.getMessage());
    }

    /**
     * Verifies decimals, words, and multiple arguments cannot be task numbers.
     */
    @Test
    public void parse_nonIntegerTaskNumbers_throwWholeNumberError() {
        Parser parser = new Parser();
        String expectedMessage = "That task number looks a little unusual.\n"
                + "Please use a whole number, like: mark 1";

        ToothlessException decimalException = assertThrows(ToothlessException.class,
                () -> parser.parse("mark 1.5", 3));
        ToothlessException wordException = assertThrows(ToothlessException.class,
                () -> parser.parse("mark second", 3));
        ToothlessException extraArgumentException = assertThrows(ToothlessException.class,
                () -> parser.parse("mark 1 extra", 3));

        assertEquals(expectedMessage, decimalException.getMessage());
        assertEquals(expectedMessage, wordException.getMessage());
        assertEquals(expectedMessage, extraArgumentException.getMessage());
    }

    /**
     * Verifies syntactically numeric input outside the integer range has specific guidance.
     */
    @Test
    public void parse_taskNumberLargerThanInteger_throwsTooLargeError() {
        Parser parser = new Parser();

        ToothlessException exception = assertThrows(ToothlessException.class,
                () -> parser.parse("delete 999999999999999999999", 3));

        assertEquals("That task number is too large for Toothless to count.\n"
                + "Please choose a number from 1 to 3.", exception.getMessage());
    }

    /**
     * Verifies a missing todo description is rejected without producing a command.
     */
    @Test
    public void parse_todoWithoutDescription_throwsDescriptionError() {
        Parser parser = new Parser();

        ToothlessException exception = assertThrows(ToothlessException.class,
                () -> parser.parse("todo", 0));

        assertEquals("Toothless couldn’t find a description for that todo.\n"
                + "Try: todo borrow book", exception.getMessage());
    }

    /**
     * Verifies a missing or blank find keyword produces focused guidance.
     */
    @Test
    public void parse_findWithoutKeyword_throwsKeywordError() {
        Parser parser = new Parser();
        String expectedMessage = "Toothless needs a keyword to sniff out matching tasks.\n"
                + "Try: find book";

        ToothlessException missingException = assertThrows(ToothlessException.class,
                () -> parser.parse("find", 0));
        ToothlessException blankException = assertThrows(ToothlessException.class,
                () -> parser.parse("   find     ", 2));

        assertEquals(expectedMessage, missingException.getMessage());
        assertEquals(expectedMessage, blankException.getMessage());
    }

    /**
     * Verifies missing and empty deadline parts produce focused errors.
     */
    @Test
    public void parse_deadlineWithMissingParts_throwsFocusedErrors() {
        Parser parser = new Parser();

        ToothlessException separatorException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline return book", 0));
        ToothlessException descriptionException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline /by 2019-12-02", 0));
        ToothlessException dateException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline return book /by", 0));

        assertEquals("This deadline is missing '/by' and its date.\n"
                + "Try: deadline return book /by 2019-12-02", separatorException.getMessage());
        assertEquals("Toothless couldn’t find a description for that deadline.\n"
                + "Try: deadline return book /by 2019-12-02", descriptionException.getMessage());
        assertEquals("This deadline is missing its date.\n"
                + "Try: deadline return book /by 2019-12-02", dateException.getMessage());
    }

    /**
     * Verifies ambiguous deadline separators are rejected as malformed structure.
     */
    @Test
    public void parse_deadlineWithConflictingSeparators_throwsFormatError() {
        Parser parser = new Parser();
        String expectedMessage = "This deadline's format has Toothless puzzled.\n"
                + "Please use: deadline DESCRIPTION /by yyyy-MM-dd";

        ToothlessException duplicateByException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline task /by 2019-12-02 /by 2020-01-01", 0));
        ToothlessException eventSeparatorException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline task /by 2019-12-02 /to evening", 0));

        assertEquals(expectedMessage, duplicateByException.getMessage());
        assertEquals(expectedMessage, eventSeparatorException.getMessage());
    }

    /**
     * Verifies strict deadline calendar validation is surfaced as a friendly parse error.
     */
    @Test
    public void parse_deadlineWithInvalidDate_throwsDateError() {
        Parser parser = new Parser();
        String expectedMessage = "That deadline date made Toothless tilt his head.\n"
                + "Please use a real date in yyyy-MM-dd format.";

        ToothlessException impossibleDateException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline task /by 2019-02-30", 0));
        ToothlessException wrongFormatException = assertThrows(ToothlessException.class,
                () -> parser.parse("deadline task /by 02-12-2019", 0));

        assertEquals(expectedMessage, impossibleDateException.getMessage());
        assertEquals(expectedMessage, wrongFormatException.getMessage());
    }

    /**
     * Verifies separator-like description text is accepted unless it is a complete token.
     */
    @Test
    public void parse_deadlineWithSeparatorPrefixInDescription_preservesDescription()
            throws ToothlessException {
        Parser parser = new Parser();
        TaskList taskList = new TaskList();

        execute(parser.parse("deadline review /bypass rules /by 2019-12-02", 0), taskList);

        Deadline deadline = assertInstanceOf(Deadline.class, taskList.getTask(0));
        assertEquals("review /bypass rules", deadline.getDescription());
        assertEquals(LocalDate.of(2019, 12, 2), deadline.getBy());
    }

    /**
     * Verifies absent event separators identify which time is missing.
     */
    @Test
    public void parse_eventWithMissingSeparators_throwsFocusedErrors() {
        Parser parser = new Parser();

        ToothlessException fromException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /to 3pm", 0));
        ToothlessException toException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /from 2pm", 0));

        assertEquals("This event is missing its starting time after '/from'.\n"
                + "Try: event DESCRIPTION /from START /to END", fromException.getMessage());
        assertEquals("This event is missing its ending time after '/to'.\n"
                + "Try: event DESCRIPTION /from START /to END", toException.getMessage());
    }

    /**
     * Verifies empty event description, start, and end values are distinguished.
     */
    @Test
    public void parse_eventWithEmptyValues_throwsFocusedErrors() {
        Parser parser = new Parser();

        ToothlessException descriptionException = assertThrows(ToothlessException.class,
                () -> parser.parse("event /from 2pm /to 3pm", 0));
        ToothlessException fromException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /from /to 3pm", 0));
        ToothlessException toException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /from 2pm /to", 0));

        assertEquals("Toothless couldn’t find a description for that event.\n"
                + "Try: event DESCRIPTION /from START /to END", descriptionException.getMessage());
        assertEquals("This event is missing its starting time.\n"
                + "Try: event DESCRIPTION /from START /to END", fromException.getMessage());
        assertEquals("This event is missing its ending time.\n"
                + "Try: event DESCRIPTION /from START /to END", toException.getMessage());
    }

    /**
     * Verifies out-of-order and duplicate event separators cannot be ambiguous.
     */
    @Test
    public void parse_eventWithConflictingSeparators_throwsStructureError() {
        Parser parser = new Parser();

        ToothlessException orderException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /to 3pm /from 2pm", 0));
        ToothlessException duplicateException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /from 1pm /from 2pm /to 3pm", 0));
        ToothlessException deadlineSeparatorException = assertThrows(ToothlessException.class,
                () -> parser.parse("event meeting /from 2pm /to 3pm /by Friday", 0));

        assertEquals("The event's '/from' must come before '/to'.\n"
                + "Try: event DESCRIPTION /from START /to END", orderException.getMessage());
        assertEquals("This event's format has Toothless puzzled.\n"
                + "Try: event DESCRIPTION /from START /to END", duplicateException.getMessage());
        assertEquals("This event's format has Toothless puzzled.\n"
                + "Try: event DESCRIPTION /from START /to END",
                deadlineSeparatorException.getMessage());
    }

    /**
     * Executes a parsed command with isolated output and temporary storage.
     */
    private String execute(Command command, TaskList taskList) throws ToothlessException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = new Ui(new ByteArrayInputStream(new byte[0]),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        Storage storage = new Storage(temporaryDirectory.resolve("parser-tasks.txt"));

        command.execute(taskList, ui, storage);
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Creates a representative ordered list for parsed task-number commands.
     */
    private TaskList createThreeTaskList() {
        TaskList taskList = new TaskList();
        taskList.addTask(new Todo("first"));
        taskList.addTask(new Todo("second"));
        taskList.addTask(new Todo("third"));
        return taskList;
    }
}
