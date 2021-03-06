package seedu.priorityq.model;

import javafx.collections.ObservableList;
import seedu.priorityq.model.entry.Entry;
import seedu.priorityq.model.entry.EntryViewComparator;
import seedu.priorityq.model.entry.Event;
import seedu.priorityq.model.entry.Task;
import seedu.priorityq.model.entry.UniqueTaskList;
import seedu.priorityq.model.entry.Update;
import seedu.priorityq.model.entry.UniqueTaskList.DuplicateTaskException;
import seedu.priorityq.model.entry.UniqueTaskList.EntryConversionException;
import seedu.priorityq.model.entry.UniqueTaskList.EntryNotFoundException;
import seedu.priorityq.model.tag.Tag;
import seedu.priorityq.model.tag.UniqueTagList;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Wraps all data at the TaskManager level
 * Duplicates are not allowed (by .equals comparison)
 */
public class TaskManager implements ReadOnlyTaskManager {

    private final UniqueTaskList entries;
    private final UniqueTagList tags;

    {
        entries = new UniqueTaskList();
        tags = new UniqueTagList();
    }

    public TaskManager() {}

    /**
     * Entries and Tags are copied into this TaskManager
     */
    public TaskManager(ReadOnlyTaskManager toBeCopied) {
        this(toBeCopied.getUniqueTaskList(), toBeCopied.getUniqueTagList());
    }

    /**
     * Entries and Tags are copied into this TaskManager
     */
    public TaskManager(UniqueTaskList entries, UniqueTagList tags) {
        resetData(entries.getInternalList(), tags.getInternalList());
    }

    public static ReadOnlyTaskManager getEmptyTaskManager() {
        return new TaskManager();
    }

//// list overwrite operations

    public ObservableList<Entry> getSortedEntries() {
        return entries.getInternalList().sorted(new EntryViewComparator());
    }

    public void setEntries(List<Entry> entries) {
        this.entries.getInternalList().setAll(entries);
    }

    public void setTags(Collection<Tag> tags) {
        this.tags.getInternalList().setAll(tags);
    }

    public void resetData(Collection<? extends Entry> newEntries, Collection<Tag> newTags) {
        ArrayList<Entry> copyList = new ArrayList<>();
        for (Entry entry : newEntries) {
            Entry copy;
            if (entry instanceof Event) {
                copy = new Event(entry);
            } else {
                copy = new Task(entry);
            }
            copyList.add(copy);
        }
        setEntries(copyList);
        setTags(newTags);
    }

    public void resetData(ReadOnlyTaskManager newData) {
        resetData(newData.getTaskList(), newData.getTagList());
    }

//// task-level operations

    /**
     * Adds a task to the task manager.
     * Also checks the new task's tags and updates {@link #tags} with any new tags found,
     * and updates the Tag objects in the task to point to those in {@link #tags}.
     *
     * @throws UniqueTaskList.DuplicateTaskException if an equivalent task already exists.
     */
    public void addTask(Entry entry) throws DuplicateTaskException {
        syncTagsWithMasterList(entry);
        entries.add(entry);
    }

    public void editTask(Update update)
            throws EntryNotFoundException, EntryConversionException {
        Entry toEdit = update.getTask();
        syncTagsWithMasterList(toEdit);
        entries.updateTitle(toEdit, update.getNewTitle());
        entries.updateStartTime(toEdit, update.getStartTime());
        entries.updateEndTime(toEdit, update.getEndTime());
        entries.updateTags(toEdit, update.getNewTags());
        entries.updateDescription(toEdit, update.getNewDescription());
        entries.updateLastModifiedTime(toEdit);
    }

    public void markTask(Entry task) throws EntryNotFoundException {
        entries.mark(task);
        entries.updateLastModifiedTime(task);
    }

    public void unmarkTask(Entry task) throws EntryNotFoundException {
        entries.unmark(task);
        entries.updateLastModifiedTime(task);
    }


    /**
     * Ensures that every tag in this task:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     */
    private void syncTagsWithMasterList(Entry entry) {
        final UniqueTagList entryTags = entry.getTags();
        tags.mergeFrom(entryTags);

        // Create map with values = tag object references in the master list
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        for (Tag tag : tags) {
            masterTagObjects.put(tag, tag);
        }

        // Rebuild the list of task tags using references from the master list
        final Set<Tag> commonTagReferences = new HashSet<>();
        for (Tag tag : entryTags) {
            commonTagReferences.add(masterTagObjects.get(tag));
        }
        entry.setTags(new UniqueTagList(commonTagReferences));
    }

    public boolean removeEntry(Entry key) throws EntryNotFoundException {
        if (entries.remove(key)) {
            return true;
        } else {
            throw new EntryNotFoundException();
        }
    }


    public void updateLastModifiedTime(Entry entry) throws EntryNotFoundException {
        entries.updateLastModifiedTime(entry);
    }

    public void updateLastModifiedTime(Entry entry, LocalDateTime localDateTime) throws EntryNotFoundException {
        entries.updateLastModifiedTime(entry, localDateTime);
    }

//// tag-level operations

    public void addTag(Tag t) throws UniqueTagList.DuplicateTagException {
        tags.add(t);
    }


    public void tagTask(Entry taskToTag, UniqueTagList newTags) {
        taskToTag.addTags(newTags);
        syncTagsWithMasterList(taskToTag);
    }

    public void untagTask(Entry taskToUntag, UniqueTagList tagsToRemove) {
        taskToUntag.removeTags(tagsToRemove);
    }

//// util methods

    @Override
    public String toString() {
        return entries.getInternalList().size() + " entries, " + tags.getInternalList().size() +  " tags";
        // TODO: refine later
    }

    @Override
    public List<Entry> getTaskList() {
        return Collections.unmodifiableList(entries.getInternalList().sorted(new EntryViewComparator()));
    }

    @Override
    public List<Entry> getUnsortedTaskList() {
        return Collections.unmodifiableList(entries.getInternalList());
    }

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags.getInternalList());
    }

    @Override
    public UniqueTaskList getUniqueTaskList() {
        return this.entries;
    }

    @Override
    public UniqueTagList getUniqueTagList() {
        return this.tags;
    }


    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof TaskManager // instanceof handles nulls
                && this.entries.equals(((TaskManager) other).entries)
                && this.tags.equals(((TaskManager) other).tags));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(entries, tags);
    }

}
