package seedu.address.logic.commands;

import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.UnmodifiableObservableList;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.task.Entry;
import seedu.address.model.task.UniqueTaskList.EntryNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

/*
 * Add tags to an entry.
 */
public class TagCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds tags to task. "
            + "Parameters: TASK_ID TAG[,...] "
            + "Example: " + COMMAND_WORD + " 2 shopping, food";

    public static final String MESSAGE_SUCCESS = "Tagged entry: %1$s";
    public static final String MESSAGE_UNDO_SUCCESS = "Undo tag entry: %1$s";

    private final int targetIndex;

    private final UniqueTagList tagsToAdd;
    private Entry taskToTag;

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
        UnmodifiableObservableList<Entry> lastShownList = model.getFilteredPersonList();
        
        if (lastShownList.size() < targetIndex) {
            indicateAttemptToExecuteIncorrectCommand();
            return new CommandResult(Messages.MESSAGE_INVALID_ENTRY_DISPLAYED_INDEX);
        }
        
        taskToTag = lastShownList.get(targetIndex - 1);
        tagsToAdd.removeFrom(taskToTag.getTags());
        
        try {
            model.tagTask(taskToTag, tagsToAdd);
        } catch (EntryNotFoundException e) {
            assert false : "The target entry cannot be missing";
        }
        setExecutionIsSuccessful();
        return new CommandResult(String.format(MESSAGE_SUCCESS, taskToTag));
    }

    @Override
    public CommandResult unexecute() {
        if (!executionIsSuccessful){
            return new CommandResult(MESSAGE_UNDO_FAIL);
        };
        assert model != null;
        assert taskToTag != null;
        assert tagsToAdd != null;

        try {
            model.untagTask(taskToTag, tagsToAdd);
        } catch (EntryNotFoundException enfe) {
            assert false : "The target entry cannot be missing";
        }
        return new CommandResult(String.format(MESSAGE_UNDO_SUCCESS, taskToTag));
    }

}
