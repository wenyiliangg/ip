import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Runs focused tests for deadline date parsing and display formatting.
 */
public class DeadlineDateTest {
    /**
     * Runs every deadline date test.
     *
     * @param args command-line arguments; they are not used
     */
    public static void main(String[] args) {
        parsesIsoDates();
        parsesLeapDayInALeapYear();
        formatsDatesForDisplay();
        rejectsNonLeapDay();
        rejectsImpossibleDates();
        rejectsIncorrectFormats();
        rejectsAdditionalContent();
        System.out.println("All DeadlineDate tests passed.");
    }

    /**
     * Verifies ordinary ISO input becomes the matching date.
     */
    private static void parsesIsoDates() {
        assertEquals(LocalDate.of(2019, 12, 2), DeadlineDate.parse("2019-12-02"),
                "an ISO date should parse without changing its value");
    }

    /**
     * Verifies valid leap days are accepted.
     */
    private static void parsesLeapDayInALeapYear() {
        assertEquals(LocalDate.of(2020, 2, 29), DeadlineDate.parse("2020-02-29"),
                "February 29 should parse in a leap year");
    }

    /**
     * Verifies dates use the required friendly display format.
     */
    private static void formatsDatesForDisplay() {
        assertEquals("Dec 2 2019", DeadlineDate.format(LocalDate.of(2019, 12, 2)),
                "the display date should use MMM d yyyy");
    }

    /**
     * Verifies February 29 is rejected outside a leap year.
     */
    private static void rejectsNonLeapDay() {
        assertParseFails("2019-02-29", "February 29 should fail outside a leap year");
    }

    /**
     * Verifies calendar dates that cannot occur are rejected.
     */
    private static void rejectsImpossibleDates() {
        assertParseFails("2019-02-30", "an impossible calendar date should fail");
    }

    /**
     * Verifies non-ISO date ordering is rejected.
     */
    private static void rejectsIncorrectFormats() {
        assertParseFails("02-12-2019", "a date outside yyyy-MM-dd format should fail");
    }

    /**
     * Verifies extra text cannot be mistaken for part of a valid date.
     */
    private static void rejectsAdditionalContent() {
        assertParseFails("2019-12-02 evening", "additional date content should fail");
    }

    /**
     * Fails the test unless parsing the supplied text reports an invalid date.
     */
    private static void assertParseFails(String value, String message) {
        try {
            DeadlineDate.parse(value);
        } catch (DateTimeParseException exception) {
            return;
        }
        throw new AssertionError(message);
    }

    /**
     * Fails the test if two values are not equal.
     */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "\nExpected: " + expected + "\nActual: " + actual);
        }
    }
}
