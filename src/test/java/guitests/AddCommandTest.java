package guitests;

import guitests.guihandles.TaskCardHandle;
import org.junit.Test;

import java.util.Arrays;

import seedu.priorityq.logic.commands.AddCommand;
import seedu.priorityq.commons.core.Messages;
import seedu.priorityq.logic.commands.ClearCommand;
import seedu.priorityq.model.entry.Entry;
import seedu.priorityq.model.entry.EntryViewComparator;
import seedu.priorityq.testutil.TestEntry;
import seedu.priorityq.testutil.TestUtil;

import static org.junit.Assert.assertTrue;

public class AddCommandTest extends TaskManagerGuiTest {

    //@@author A0116603R-reused
    @Test
    public void add() {
        TestEntry[] currentList = td.getTypicalSortedEntries(); // sample entries already present

        //add new entries
        for (TestEntry testEntry : td.getNonSampleEntries()) {
            assertAddSuccess(testEntry, currentList);
            currentList = TestUtil.addEntriesToList(currentList, testEntry);
        }

        assertTrue(currentList.length > 0);

        //add to empty list
        commandBox.runCommand(ClearCommand.COMMAND_WORD);
        assertAddSuccess(currentList[0]);

        //invalid command
        commandBox.runCommand("adds Johnny");
        assertResultMessage(Messages.MESSAGE_UNKNOWN_COMMAND);

    }

    //@@author
    @Test
    public void add_Deadline() {
        commandBox.runCommand("add Deadline end/today");
        assertResultMessage(String.format(AddCommand.MESSAGE_SUCCESS, "Deadline Due: moments ago"));
    }

    @Test
    public void add_Event() {
        commandBox.runCommand("add Event start/6 Nov 2pm end/6 Nov 5pm");
        assertResultMessage(String.format(AddCommand.MESSAGE_SUCCESS, "Event from: Sun, Nov 6 at 14:00 to: Sun, Nov 6 at 17:00"));
    }


    private void assertAddSuccess(TestEntry testEntry, TestEntry... currentList) {
        commandBox.runCommand(testEntry.getAddCommand());

        //confirm the new card contains the right data
        TaskCardHandle addedCard = taskList.navigateToEntry(testEntry.getTitle().fullTitle);
        assertMatching(testEntry, addedCard);
        
        Entry entry = taskList.getEntry(taskList.getTaskIndex(testEntry));
        testEntry.setLastModifiedTime(entry.getLastModifiedTime());

        //confirm the list now contains all previous entries plus the new task
        TestEntry[] expectedList = TestUtil.addEntriesToList(currentList, testEntry);
        Arrays.sort(expectedList, new EntryViewComparator());
        assertTrue(taskList.isListMatching(expectedList));
    }

}
