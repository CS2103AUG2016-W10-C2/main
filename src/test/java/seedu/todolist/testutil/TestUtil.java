package seedu.todolist.testutil;

import guitests.guihandles.TaskCardHandle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import junit.framework.AssertionFailedError;
import seedu.todolist.TestApp;
import seedu.todolist.commons.exceptions.IllegalValueException;
import seedu.todolist.commons.util.FileUtil;
import seedu.todolist.commons.util.XmlUtil;
import seedu.todolist.model.TaskManager;
import seedu.todolist.model.task.*;
import seedu.todolist.model.tag.Tag;
import seedu.todolist.model.tag.UniqueTagList;
import seedu.todolist.storage.XmlSerializableTaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for test cases.
 */
public class TestUtil {


    public static void assertThrows(Class<? extends Throwable> expected, Runnable executable) {
        try {
            executable.run();
        }
        catch (Throwable actualException) {
            if (!actualException.getClass().isAssignableFrom(expected)) {
                String message = String.format("Expected thrown: %s, actual: %s", expected.getName(),
                        actualException.getClass().getName());
                throw new AssertionFailedError(message);
            } else return;
        }
        throw new AssertionFailedError(
                String.format("Expected %s to be thrown, but nothing was thrown.", expected.getName()));
    }

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    public static String SANDBOX_FOLDER = FileUtil.getPath("./src/test/data/sandbox/");

    public static final Task[] samplePersonData = getSamplePersonData();

    private static Task[] getSamplePersonData() {
        try {
            return new Task[]{
                    new Task(new Title("Ali Muster"), new UniqueTagList()),
                    new Task(new Title("Boris Mueller"), new UniqueTagList()),
                    new Task(new Title("Carl Kurz"), new UniqueTagList()),
                    new Task(new Title("Daniel Meier"), new UniqueTagList()),
                    new Task(new Title("Elle Meyer"), new UniqueTagList()),
                    new Task(new Title("Fiona Kunz"), new UniqueTagList()),
                    new Task(new Title("George Best"), new UniqueTagList()),
                    new Task(new Title("Hoon Meier"), new UniqueTagList()),
                    new Task(new Title("Ida Mueller"), new UniqueTagList())
            };
        } catch (IllegalValueException e) {
            assert false;
            //not possible
            return null;
        }
    }

    public static final Tag[] sampleTagData = getSampleTagData();

    private static Tag[] getSampleTagData() {
        try {
            return new Tag[]{
                    new Tag("relatives"),
                    new Tag("friends")
            };
        } catch (IllegalValueException e) {
            assert false;
            return null;
            //not possible
        }
    }

    public static List<Task> generateSamplePersonData() {
        return Arrays.asList(samplePersonData);
    }

    /**
     * Appends the file name to the sandbox folder path.
     * Creates the sandbox folder if it doesn't exist.
     * @param fileName
     * @return
     */
    public static String getFilePathInSandboxFolder(String fileName) {
        try {
            FileUtil.createDirs(new File(SANDBOX_FOLDER));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER + fileName;
    }

    public static void createDataFileWithSampleData(String filePath) {
        createDataFileWithData(generateSampleStorageTaskManager(), filePath);
    }

    public static <T> void createDataFileWithData(T data, String filePath) {
        try {
            File saveFileForTesting = new File(filePath);
            FileUtil.createIfMissing(saveFileForTesting);
            XmlUtil.saveDataToFile(saveFileForTesting, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... s) {
        createDataFileWithSampleData(TestApp.SAVE_LOCATION_FOR_TESTING);
    }

    public static TaskManager generateEmptyTaskManager() {
        return new TaskManager(new UniqueTaskList(), new UniqueTagList());
    }

    public static XmlSerializableTaskManager generateSampleStorageTaskManager() {
        return new XmlSerializableTaskManager(generateEmptyTaskManager());
    }

    /**
     * Tweaks the {@code keyCodeCombination} to resolve the {@code KeyCode.SHORTCUT} to their
     * respective platform-specific keycodes
     */
    public static KeyCode[] scrub(KeyCodeCombination keyCodeCombination) {
        List<KeyCode> keys = new ArrayList<>();
        if (keyCodeCombination.getAlt() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.ALT);
        }
        if (keyCodeCombination.getShift() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.SHIFT);
        }
        if (keyCodeCombination.getMeta() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.META);
        }
        if (keyCodeCombination.getControl() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.CONTROL);
        }
        keys.add(keyCodeCombination.getCode());
        return keys.toArray(new KeyCode[]{});
    }

    /**
     * Removes a subset from the list of persons.
     * @param persons The list of persons
     * @param personsToRemove The subset of persons.
     * @return The modified persons after removal of the subset from persons.
     */
    public static TestEntry[] removePersonsFromList(final TestEntry[] persons, TestEntry... personsToRemove) {
        List<TestEntry> listOfPersons = asList(persons);
        listOfPersons.removeAll(asList(personsToRemove));
        return listOfPersons.toArray(new TestEntry[listOfPersons.size()]);
    }

    /**
     * Returns a copy of the list with the task at specified index removed.
     * @param list original list to copy from
     * @param targetIndexInOneIndexedFormat e.g. if the first element to be removed, 1 should be given as index.
     */
    public static TestEntry[] removePersonFromList(final TestEntry[] list, int targetIndexInOneIndexedFormat) {
        return removePersonsFromList(list, list[targetIndexInOneIndexedFormat-1]);
    }

    /**
     * Appends persons to the array of persons.
     * @param persons A array of persons.
     * @param personsToAdd The persons that are to be appended behind the original array.
     * @return The modified array of persons.
     */
    public static TestEntry[] addPersonsToList(final TestEntry[] persons, TestEntry... personsToAdd) {
        List<TestEntry> listOfPersons = asList(persons);
        listOfPersons.addAll(asList(personsToAdd));
        return listOfPersons.toArray(new TestEntry[listOfPersons.size()]);
    }
    
    //@@author A0121501E
    /**
     * Adds persons to the array of sorted persons.
     * @param persons A array of persons.
     * @param personsToAdd The persons that are to be added to the sorted list.
     * @return The modified array of sorted persons.
     */
    public static TestEntry[] addPersonsToSortedList(final TestEntry[] persons, TestEntry... personsToAdd) {
        TestEntry[] testEntry = addPersonsToList(persons, personsToAdd);
        Arrays.sort(testEntry, new EntryViewComparator());
        return testEntry;
    }
    //@@author
    private static <T> List<T> asList(T[] objs) {
        List<T> list = new ArrayList<>();
        for(T obj : objs) {
            list.add(obj);
        }
        return list;
    }

    public static boolean compareCardAndEntry(TaskCardHandle card, Entry person) {
        return card.isSameEntry(person);
    }

    public static Tag[] getTagList(String tags) {

        if ("".equals(tags)) {
            return new Tag[]{};
        }

        final String[] split = tags.split(", ");

        final List<Tag> collect = Arrays.asList(split).stream().map(e -> {
            try {
                return new Tag(e.replaceFirst("Tag: ", ""));
            } catch (IllegalValueException e1) {
                //not possible
                assert false;
                return null;
            }
        }).collect(Collectors.toList());

        return collect.toArray(new Tag[split.length]);
    }

}
