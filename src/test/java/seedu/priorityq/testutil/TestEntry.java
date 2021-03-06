package seedu.priorityq.testutil;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import seedu.priorityq.model.entry.Entry;
import seedu.priorityq.logic.commands.AddCommand;
import seedu.priorityq.model.tag.UniqueTagList;

import java.time.LocalDateTime;

import static seedu.priorityq.logic.commands.AddCommand.TAG_FLAG;

/**
 * A mutable task object. For testing only.
 */
public class TestEntry extends Entry {
    public TestEntry() {
        title = new SimpleObjectProperty<>();
        tags = new SimpleObjectProperty<>(new UniqueTagList());
        description = new SimpleStringProperty("");
        isMarked = new SimpleBooleanProperty(false);
        lastModifiedTime = new SimpleObjectProperty<>();
    }

    @Override
    public String toString() {
        return getAsText();
    }

    public String getAddCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("add " + this.getTitle().fullTitle + " ");
        if (!this.getTags().isEmpty()) {
            this.getTags().getInternalList().forEach(s -> sb.append(" " + TAG_FLAG).append(s.tagName));
        }
        if (!description.getValue().isEmpty()) {
            sb.append(" ");
            sb.append(AddCommand.DESC_FLAG);
            sb.append(description.getValue());
        }
        return sb.toString();
    }
    @Override
    public String getDateDisplay(LocalDateTime dateTime) {
        return "";
    }

    @Override
    public LocalDateTime getComparableTime() {
        return lastModifiedTime.get();
    }
}
