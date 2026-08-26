# UI Test Plan

## TC-01: Interleaved valid and invalid commands preserve task state

Aim: Verify empty and unknown input, every task format, malformed separators, all task-number errors, additional spaces, continuation after errors, and unchanged task count, ordering, and star statuses.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text

blah
list extra
mark
todo
todo
todo    read book
deadline
deadline return book
deadline /by Sunday
deadline return book /by
deadline return book /by Sunday /by Monday
deadline return book /by 2019-12-02
event
event project meeting
event project meeting /from Mon 2pm
event project meeting /to 4pm
event /from Mon 2pm /to 4pm
event project meeting /from /to 4pm
event project meeting /from Mon 2pm /to
event project meeting /to 4pm /from Mon 2pm
event project meeting /from Mon /from Tue /to 4pm
event project meeting /from tomorrow /to 4pm
mark
mark abc
mark 0
mark -1
mark 2.5
mark 999
mark 1
unmark hello
unmark 1
mark 2
delete
delete 0
delete 999
delete 1
list
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Toothless heard a tiny silence. What should he do?
Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
Toothless tilted his head—he doesn’t recognise that command.
Try todo, deadline, event, list, mark, unmark, delete, or bye.
____________________________________________________________
The list command doesn't need extra words.
Try: list
____________________________________________________________
Toothless's cave is empty, so there is no task to mark.
Add a task first, then try again.
____________________________________________________________
Toothless couldn’t find a description for that todo.
Try: todo borrow book
____________________________________________________________
Toothless couldn’t find a description for that todo.
Try: todo borrow book
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] read book
Now you have 1 task in the list. ★
____________________________________________________________
This deadline is missing '/by' and its date.
Try: deadline return book /by 2019-12-02
____________________________________________________________
This deadline is missing '/by' and its date.
Try: deadline return book /by 2019-12-02
____________________________________________________________
Toothless couldn’t find a description for that deadline.
Try: deadline return book /by 2019-12-02
____________________________________________________________
This deadline is missing its date.
Try: deadline return book /by 2019-12-02
____________________________________________________________
This deadline's format has Toothless puzzled.
Please use: deadline DESCRIPTION /by yyyy-MM-dd
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] return book (by: Dec 2 2019)
Now you have 2 tasks in the list. ★
____________________________________________________________
This event is missing its starting time after '/from'.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event is missing its starting time after '/from'.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event is missing its ending time after '/to'.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event is missing its starting time after '/from'.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
Toothless couldn’t find a description for that event.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event is missing its starting time.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event is missing its ending time.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
The event's '/from' must come before '/to'.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
This event's format has Toothless puzzled.
Try: event DESCRIPTION /from START /to END
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] project meeting (from: tomorrow to: 4pm)
Now you have 3 tasks in the list. ★
____________________________________________________________
Toothless needs a task number to mark.
Try: mark 1
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: mark 1
____________________________________________________________
Toothless can’t find task 0 in the cave.
Please choose a number from 1 to 3.
____________________________________________________________
Toothless can’t find task -1 in the cave.
Please choose a number from 1 to 3.
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: mark 1
____________________________________________________________
Toothless can’t find task 999 in the cave.
Please choose a number from 1 to 3.
____________________________________________________________
A happy little roar! I've starred this task as done:
  [T][★] read book
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: unmark 1
____________________________________________________________
All right, little rider! I've unstarred this task for now:
  [T][ ] read book
____________________________________________________________
A happy little roar! I've starred this task as done:
  [D][★] return book (by: Dec 2 2019)
____________________________________________________________
Toothless needs a task number to delete.
Try: delete 1
____________________________________________________________
Toothless can’t find task 0 in the cave.
Please choose a number from 1 to 3.
____________________________________________________________
Toothless can’t find task 999 in the cave.
Please choose a number from 1 to 3.
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [T][ ] read book
Now you have 2 tasks in the list.
____________________________________________________________
Here are the tasks in your list:
1.[D][★] return book (by: Dec 2 2019)
2.[E][ ] project meeting (from: tomorrow to: 4pm)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```


## TC-02: Validate deadline dates without changing task state

Aim: Verify missing, incorrectly formatted, impossible, non-leap, and malformed dates are rejected without stack traces or task-list changes; leap-day and whitespace-surrounded dates remain usable through listing, marking, unmarking, and deletion.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text
deadline no separator
deadline no date /by
deadline wrong format /by 02-12-2019
deadline impossible /by 2019-02-30
deadline extra content /by 2019-12-02 evening
deadline extra separator /by 2019-12-02 /to evening
deadline leap day /by 2020-02-29
deadline non-leap day /by 2019-02-29
deadline    spaced date    /by    2019-12-02
list
mark 1
unmark 1
mark 2
delete 1
list
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
This deadline is missing '/by' and its date.
Try: deadline return book /by 2019-12-02
____________________________________________________________
This deadline is missing its date.
Try: deadline return book /by 2019-12-02
____________________________________________________________
That deadline date made Toothless tilt his head.
Please use a real date in yyyy-MM-dd format.
____________________________________________________________
That deadline date made Toothless tilt his head.
Please use a real date in yyyy-MM-dd format.
____________________________________________________________
That deadline date made Toothless tilt his head.
Please use a real date in yyyy-MM-dd format.
____________________________________________________________
This deadline's format has Toothless puzzled.
Please use: deadline DESCRIPTION /by yyyy-MM-dd
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] leap day (by: Feb 29 2020)
Now you have 1 task in the list. ★
____________________________________________________________
That deadline date made Toothless tilt his head.
Please use a real date in yyyy-MM-dd format.
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] spaced date (by: Dec 2 2019)
Now you have 2 tasks in the list. ★
____________________________________________________________
Here are the tasks in your list:
1.[D][ ] leap day (by: Feb 29 2020)
2.[D][ ] spaced date (by: Dec 2 2019)
____________________________________________________________
A happy little roar! I've starred this task as done:
  [D][★] leap day (by: Feb 29 2020)
____________________________________________________________
All right, little rider! I've unstarred this task for now:
  [D][ ] leap day (by: Feb 29 2020)
____________________________________________________________
A happy little roar! I've starred this task as done:
  [D][★] spaced date (by: Dec 2 2019)
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [D][ ] leap day (by: Feb 29 2020)
Now you have 1 task in the list.
____________________________________________________________
Here are the tasks in your list:
1.[D][★] spaced date (by: Dec 2 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```


## TC-03: Delete tasks safely from an ArrayList

Aim: Verify deletion from an empty list; invalid, missing, decimal, negative, zero, out-of-range, and extra arguments; deletion of first, middle, and last Todo, Deadline, and Event tasks; completed-task deletion; re-numbering; order; singular/plural counts; multiple deletions; and other commands after deletion.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text
delete
todo first task
deadline middle deadline /by 2019-12-06
event last event /from 2pm /to 3pm
todo tail task
mark 2
delete
delete abc
delete 1.5
delete 0
delete -1
delete 99
delete 2 extra
list
delete 2
list
delete 1
delete 2
list
mark 1
delete 1
list
delete 1
todo new task
delete 1
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Toothless's cave is empty, so there is no task to delete.
Add a task first, then try again.
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] first task
Now you have 1 task in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] middle deadline (by: Dec 6 2019)
Now you have 2 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] last event (from: 2pm to: 3pm)
Now you have 3 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] tail task
Now you have 4 tasks in the list. ★
____________________________________________________________
A happy little roar! I've starred this task as done:
  [D][★] middle deadline (by: Dec 6 2019)
____________________________________________________________
Toothless needs a task number to delete.
Try: delete 1
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: delete 1
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: delete 1
____________________________________________________________
Toothless can’t find task 0 in the cave.
Please choose a number from 1 to 4.
____________________________________________________________
Toothless can’t find task -1 in the cave.
Please choose a number from 1 to 4.
____________________________________________________________
Toothless can’t find task 99 in the cave.
Please choose a number from 1 to 4.
____________________________________________________________
That task number looks a little unusual.
Please use a whole number, like: delete 1
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] first task
2.[D][★] middle deadline (by: Dec 6 2019)
3.[E][ ] last event (from: 2pm to: 3pm)
4.[T][ ] tail task
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [D][★] middle deadline (by: Dec 6 2019)
Now you have 3 tasks in the list.
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] first task
2.[E][ ] last event (from: 2pm to: 3pm)
3.[T][ ] tail task
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [T][ ] first task
Now you have 2 tasks in the list.
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [T][ ] tail task
Now you have 1 task in the list.
____________________________________________________________
Here are the tasks in your list:
1.[E][ ] last event (from: 2pm to: 3pm)
____________________________________________________________
A happy little roar! I've starred this task as done:
  [E][★] last event (from: 2pm to: 3pm)
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [E][★] last event (from: 2pm to: 3pm)
Now you have 0 tasks in the list.
____________________________________________________________
Your task list is empty. Ready for a new adventure!
____________________________________________________________
Toothless's cave is empty, so there is no task to delete.
Add a task first, then try again.
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] new task
Now you have 1 task in the list. ★
____________________________________________________________
A tiny farewell roar! Toothless has removed this task:
  [T][ ] new task
Now you have 0 tasks in the list.
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```


## TC-04: Load valid saved tasks at startup

Aim: Verify startup restores every task type, its date or time values, and the completed-task display.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && mkdir "$TOOTHLESS_TEST_ROOT/data" && printf "%s\n" "T | 1 | borrow book" "D | 0 | return book | 2019-12-02" "E | 1 | project meeting | Monday 2pm | Monday 3pm" > "$TOOTHLESS_TEST_ROOT/data/toothless.txt" && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text
list
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Here are the tasks in your list:
1.[T][★] borrow book
2.[D][ ] return book (by: Dec 2 2019)
3.[E][★] project meeting (from: Monday 2pm to: Monday 3pm)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```


## TC-05: Keep tasks usable after storage failures

Aim: Verify expected read and write failures show friendly messages without a stack trace or loss of in-memory task state.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && mkdir -p "$TOOTHLESS_TEST_ROOT/data/toothless.txt" && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text
list
todo keep this task
list
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Toothless had trouble reading his saved quests.
He'll start with an empty cave, but the saved file was left untouched.
____________________________________________________________
Your task list is empty. Ready for a new adventure!
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] keep this task
Now you have 1 task in the list. ★
Toothless couldn’t tuck these changes into his data file.
They’re still safe for this adventure, but may not return next time.
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] keep this task
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```


## TC-06: Skip malformed saved tasks safely

Aim: Verify malformed saved entries produce one friendly warning while valid entries retain their type, values, order, and completion display.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && TOOTHLESS_TEST_ROOT=$(mktemp -d) && javac -d "$TOOTHLESS_TEST_ROOT/classes" src/main/java/*.java && mkdir "$TOOTHLESS_TEST_ROOT/data" && printf "%s\n" "T | 1 | borrow book" "X | 0 | unknown type" "D | 0 | missing time" "T | maybe | invalid status" "E | 0 | truncated event | 2pm" "" "T | 0 | unexpected data | extra field" "D | 0 | return book | 2019-12-06" > "$TOOTHLESS_TEST_ROOT/data/toothless.txt" && cd "$TOOTHLESS_TEST_ROOT" && java -cp classes Toothless'
```

Input:

```text
list
bye
```

Expected output:

```text
____________________________________________________________
  __/\__           __/\__
 /     \_________/     \
/   /\   O     O   /\   \
\__/  \     ^     /  \__/
       \  \___/  /
    ____|       |____
 __/    |       |    \__
/___/   /|_______|\   \___\
        /_/     \_\

Hi there! I'm Toothless. It's wonderful to meet you!
What can I do for you today?
Ready for our next little adventure? Tell me what to remember:
  - todo [DESCRIPTION]
  - deadline [DESCRIPTION] /by [yyyy-MM-dd]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Toothless found 6 puzzling lines in his saved quests.
He skipped them and kept every task he could understand.
____________________________________________________________
Here are the tasks in your list:
1.[T][★] borrow book
2.[D][ ] return book (by: Dec 6 2019)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```
