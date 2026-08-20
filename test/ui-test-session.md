# UI Test Session

## TC-01: Add and list Todo, Deadline, and Event tasks — PASS

Aim: Verify all three task types, legacy plain-description input, plain-text date/time parsing, task counts, polymorphic listing, and the completed-task star while retaining mark, unmark, delete, and bye behavior.

Command:

```text
/bin/zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && javac -d /tmp/toothless-ui-classes src/main/java/*.java && java -cp /tmp/toothless-ui-classes Toothless'
```

Console input:

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
join sports club
mark 1
mark 4
todo borrow book
list
deadline return book /by Sunday
event project meeting /from Mon 2pm /to no idea :-p
unmark 1
delete 2
list
bye
```

Actual console output:

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
  - deadline [DESCRIPTION] /by [DATE_OR_TIME]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] read book
Now you have 1 task in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] return book (by: June 6th)
Now you have 2 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] join sports club
Now you have 4 tasks in the list. ★
____________________________________________________________
A happy little roar! I've starred this task as done:
  [T][★] read book
____________________________________________________________
A happy little roar! I've starred this task as done:
  [T][★] join sports club
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] borrow book
Now you have 5 tasks in the list. ★
____________________________________________________________
Here are the tasks in your list:
1.[T][★] read book
2.[D][ ] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
4.[T][★] join sports club
5.[T][ ] borrow book
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] return book (by: Sunday)
Now you have 6 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] project meeting (from: Mon 2pm to: no idea :-p)
Now you have 7 tasks in the list. ★
____________________________________________________________
All right, little rider! I've unstarred this task for now:
  [T][ ] read book
____________________________________________________________
All done! Toothless has removed this task:
  [D][ ] return book (by: June 6th)
Now you have 6 tasks in the list. ★
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
2.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
3.[T][★] join sports club
4.[T][ ] borrow book
5.[D][ ] return book (by: Sunday)
6.[E][ ] project meeting (from: Mon 2pm to: no idea :-p)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________

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
  - deadline [DESCRIPTION] /by [DATE_OR_TIME]
  - event [DESCRIPTION] /from [START_DATE_OR_TIME] /to [END_DATE_OR_TIME]
You can also type list to see all our quests. Tiny roar! ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] read book
Now you have 1 task in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] return book (by: June 6th)
Now you have 2 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
Now you have 3 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] join sports club
Now you have 4 tasks in the list. ★
____________________________________________________________
A happy little roar! I've starred this task as done:
  [T][★] read book
____________________________________________________________
A happy little roar! I've starred this task as done:
  [T][★] join sports club
____________________________________________________________
Got it! Toothless has added this task for you:
  [T][ ] borrow book
Now you have 5 tasks in the list. ★
____________________________________________________________
Here are the tasks in your list:
1.[T][★] read book
2.[D][ ] return book (by: June 6th)
3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
4.[T][★] join sports club
5.[T][ ] borrow book
____________________________________________________________
Got it! Toothless has added this task for you:
  [D][ ] return book (by: Sunday)
Now you have 6 tasks in the list. ★
____________________________________________________________
Got it! Toothless has added this task for you:
  [E][ ] project meeting (from: Mon 2pm to: no idea :-p)
Now you have 7 tasks in the list. ★
____________________________________________________________
All right, little rider! I've unstarred this task for now:
  [T][ ] read book
____________________________________________________________
All done! Toothless has removed this task:
  [D][ ] return book (by: June 6th)
Now you have 6 tasks in the list. ★
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
2.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
3.[T][★] join sports club
4.[T][ ] borrow book
5.[D][ ] return book (by: Sunday)
6.[E][ ] project meeting (from: Mon 2pm to: no idea :-p)
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

Exit code: `0` (expected `0`)
