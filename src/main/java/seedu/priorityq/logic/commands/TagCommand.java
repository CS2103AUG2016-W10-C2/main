package seedu.priorityq.logic.commands;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import seedu.priorityq.commons.core.Messages;
import seedu.priorityq.commons.core.UnmodifiableObservableList;
import seedu.priorityq.commons.exceptions.IllegalValueException;
import seedu.priorityq.model.entry.Entry;
import seedu.priorityq.model.entry.UniqueTaskList.EntryNotFoundException;
import seedu.priorityq.model.tag.Tag;
import seedu.priorityq.model.tag.UniqueTagList;

//@@author A0121501E
/*
 * Add tags to an entry. Does not overwrite existing tags.
 */
public class TagCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds tags to task. "
            + "Parameters: TASK_ID TAG[,...] "
            + "Example: " + COMMAND_WORD + " 2 #shopping #food";

    public static final String MESSAGE_SUCCESS = "Add %1$s to entry: %2$s";
    public static final String MESSAGE_UNDO_SUCCESS = "Undo add %1$s to entry: %2$s";
    public static final String MESSAGE_ALREADY_EXISTS = "All specified tags already exist on entry: %1$s";

    private final int targetIndex;

    private final UniqueTagList tagsToAdd;
    private Entry taskToTag;
    private LocalDateTime originalLastModifiedTime;

    public TagCommand(int targetIndex, Set<String> tags) throws IllegalValueException {
        this.targetIndex = targetIndex;

        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(new Tag(tagName));
        }
        this.tagsToAdd = new UniqueTagList(tagSet);
    }

    @Override
    public CommandResult execute() {
        assert model != null;
        assert !tagsToAdd.isEmpty(); //should be handled in the parser
        if (getCommandState()==CommandState.PRE_EXECUTION) {
            UnmodifiableObservableList<Entry> lastShownList = model.getFilteredEntryList();
            
            if (lastShownList.size() < targetIndex) {
                indicateAttemptToExecuteIncorrectCommand();
                return new CommandResult(Messages.MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX);
            }
            
            taskToTag = lastShownList.get(targetIndex - 1);
            originalLastModifiedTime = taskToTag.getLastModifiedTime();
            tagsToAdd.removeFrom(taskToTag.getTags());
        }

        if (tagsToAdd.isEmpty()){
            indicateAttemptToExecuteIncorrectCommand();
            return new CommandResult(String.format(MESSAGE_ALREADY_EXISTS, taskToTag));
        }
        try {
            model.tagTask(taskToTag, tagsToAdd);
        } catch (EntryNotFoundException e) {
            assert false : "The target entry cannot be missing";
        }
        setUndoable();
        return new CommandResult(String.format(MESSAGE_SUCCESS, tagsToAdd, taskToTag));
    }

    @Override
    public CommandResult unexecute() {
        if (getCommandState() != CommandState.UNDOABLE){
            return new CommandResult(MESSAGE_UNDO_FAIL);
        }
        assert model != null;
        assert taskToTag != null;
        assert tagsToAdd != null;

        try {
            model.untagTask(taskToTag, tagsToAdd);
            model.updateLastModifiedTime(taskToTag, originalLastModifiedTime);
        } catch (EntryNotFoundException enfe) {
            assert false : "The target entry cannot be missing";
        }
        setRedoable();
        return new CommandResult(String.format(MESSAGE_UNDO_SUCCESS, tagsToAdd, taskToTag));
    }

}
