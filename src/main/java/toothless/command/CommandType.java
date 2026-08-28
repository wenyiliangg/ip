package toothless.command;

/**
 * Identifies a command keyword understood by the Toothless chatbot.
 */
public enum CommandType {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    FIND("find"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    BYE("bye"),
    UNKNOWN("");

    private final String keyword;

    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Converts a user-entered keyword into its corresponding command type.
     *
     * @param keyword first word entered by the user
     * @return matching command type, or {@link #UNKNOWN} when there is no match
     */
    public static CommandType fromKeyword(String keyword) {
        for (CommandType commandType : values()) {
            if (commandType.keyword.equals(keyword)) {
                return commandType;
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
