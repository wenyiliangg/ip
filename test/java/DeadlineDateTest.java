import java.time.LocalDate;

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
     * Fails the test if two values are not equal.
     */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + "\nExpected: " + expected + "\nActual: " + actual);
        }
    }
}
