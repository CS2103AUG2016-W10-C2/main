package seedu.address.ui;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Generic cell interface for displaying different types of Entries
 */
//@@author A0116603R
public interface EntryViewCell {
    // Default factory method for generating a ListCell
    static <T> Callback<ListView<T>, ListCell<T>> getFactory() {
        return param -> new ListCell<>();
    }
}
