/**
 * Represents a command understood by the Toothless chatbot.
 */
public enum Command {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    BYE("bye"),
    UNKNOWN("");

    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Converts a user-entered keyword into its corresponding command.
     *
     * @param keyword first word entered by the user
     * @return matching command, or {@link #UNKNOWN} when there is no match
     */
    public static Command fromKeyword(String keyword) {
        for (Command command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }
        return UNKNOWN;
    }

    /**
     * Returns the command keyword used in user-facing instructions.
     *
     * @return lowercase command keyword
     */
    @Override
    public String toString() {
        return keyword;
    }
}
