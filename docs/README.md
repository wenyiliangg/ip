# Toothless User Guide

Toothless helps you remember todos, deadlines, and events through a friendly chat interface.

## Getting started

1. Start Toothless with `./gradlew run`.
2. Type a command in the box at the bottom.
3. Press Enter or select **Send**.
4. Select **Help** at any time to reveal clickable command examples.
5. Select **Edit task** inside Help to reveal description, deadline, and event edit examples.

Selecting an example fills the input box without sending it. Edit the example if needed, then send it when ready.

## Commands

| Purpose | Format | Example |
| --- | --- | --- |
| Show every task | `list` | `list` |
| Add a todo | `todo DESCRIPTION` | `todo read a book` |
| Add a deadline | `deadline DESCRIPTION /by yyyy-MM-dd` | `deadline return book /by 2026-12-31` |
| Add an event | `event DESCRIPTION /from START /to END` | `event project meeting /from 2pm /to 4pm` |
| Find matching tasks | `find KEYWORD` | `find book` |
| Mark a task done | `mark TASK_NUMBER` | `mark 1` |
| Mark a task not done | `unmark TASK_NUMBER` | `unmark 1` |
| Edit a description | `edit TASK_NUMBER /description NEW_DESCRIPTION` | `edit 1 /description read the new textbook` |
| Edit a deadline date | `edit TASK_NUMBER /by yyyy-MM-dd` | `edit 2 /by 2026-09-20` |
| Edit an event start | `edit TASK_NUMBER /from START` | `edit 3 /from 21 September 2026 2pm` |
| Edit an event end | `edit TASK_NUMBER /to END` | `edit 3 /to 21 September 2026 5pm` |
| Delete a task | `delete TASK_NUMBER` | `delete 1` |
| End the session | `bye` | `bye` |

Task numbers come from `list`. Toothless responds with a friendly explanation when a command is incomplete or uses
an invalid task number.

An `edit` command changes only the fields you provide. You can combine supported fields, such as:

```text
edit 3 /description project consultation /to 21 September 2026 6pm
```

Todos support `/description`; Deadlines support `/description` and `/by`; Events support `/description`, `/from`,
and `/to`. Editing keeps the task type, completion status, and list position unchanged.

## Dates and saved tasks

Deadline dates use the ISO `yyyy-MM-dd` format. For example:

```text
deadline submit report /by 2026-10-15
```

Tasks are saved automatically after a successful add, edit, mark, unmark, or delete command. Toothless loads them
the next time the application starts.

## Ending a session

Send `bye` to receive Toothless's farewell. The input field, Send button, and command examples are then disabled for
that finished session. Close and reopen the window to begin a new session.
