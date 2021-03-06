package seedu.priorityq.ui;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import seedu.priorityq.commons.core.EventsCenter;
import seedu.priorityq.commons.core.LogsCenter;
import seedu.priorityq.commons.core.Messages;
import seedu.priorityq.commons.events.ui.DidMarkTaskEvent;
import seedu.priorityq.commons.events.ui.FocusCommandLineEvent;
import seedu.priorityq.commons.events.ui.IncorrectCommandAttemptedEvent;
import seedu.priorityq.logic.Logic;
import seedu.priorityq.logic.commands.CommandHistory;
import seedu.priorityq.logic.commands.CommandResult;

import java.util.logging.Logger;

import static seedu.priorityq.ui.util.GuiUtil.ERROR_STYLE_CLASS;

// @@author A0116603R
/**
 * Represents the Command Area
 */
public class CommandArea extends VBox {

    // ####################
    // # STATIC CONSTANTS #
    // ####################
    private static String FXML = "CommandArea.fxml";

    private static final Logger logger = LogsCenter.getLogger(CommandArea.class);

    // #############################
    // # COMMAND EXECUTION HELPERS #
    // #############################
    private Logic logic;

    private String previousCommand;

    private CommandResult mostRecentResult;

    private CommandHistory commandHistoryManager;

    // ########
    // # FXML #
    // ########
    @FXML
    private TextField cmdLine;

    @FXML
    private Label statusLine;

    CommandArea(Logic logic) {
        FXMLLoader loader = UiPartLoader.getLoader(FXML);
        loader.setRoot(this);
        loader.setController(this);
        UiPartLoader.loadNode(loader, FXML);

        this.logic = logic;
        EventsCenter.getInstance().registerHandler(this);
        commandHistoryManager = this.logic.getCommandHistoryManager();
    }

    @FXML
    public void initialize() {
        cmdLine.setPromptText(Messages.COMMAND_LINE_PROMPT);
        statusLine.setText(Messages.STATUS_LINE_WELCOME);
    }

    // @@author
    @FXML
    private void handleCommandInputChanged() {
        previousCommand = cmdLine.getText();
        setStyleToIndicateCorrectCommand();
        mostRecentResult = logic.execute(previousCommand);
        statusLine.setText(mostRecentResult.feedbackToUser);
        logger.info("Result: " + mostRecentResult.feedbackToUser);
    }

    // @@author A0127828W
    @FXML
    private void handleKeyPressedCommandArea(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                fillUpCommandLine(commandHistoryManager.getPreviousCommand());
                logger.info("UP key entered");
                break;
            case DOWN:
                fillUpCommandLine(commandHistoryManager.getNextCommand());
                logger.info("DOWN key entered");
                break;
            default:
                break;
        }
    }

    /**
     * Fill the command line with an input string
     */
    private void fillUpCommandLine(String command) {
        if (command == null) {
            logger.info("Reached command history limit.");
            return;
        }
        cmdLine.setText(command);
    }
    // @@author

    // ##################
    // # EVENT HANDLERS #
    // ##################
    @Subscribe
    private void handleIncorrectCommandAttempted(IncorrectCommandAttemptedEvent event){
        logger.info(LogsCenter.getEventHandlingLogMessage(event,"Invalid command: " + previousCommand));
        setStyleToIndicateIncorrectCommand();
        restoreCommandText();
    }

    // @@author A0116603R
    @Subscribe
    private void handleDidMarkTaskEvent(DidMarkTaskEvent event) {
        statusLine.setText(event.getCommandResult().feedbackToUser);
    }

    @Subscribe
    private void handleFocusCommandLineEvent(FocusCommandLineEvent event) {
        if (!cmdLine.isFocused()) {
            cmdLine.requestFocus();
        }
    }

    /**
     * Sets the command box style to indicate a correct command.
     */
    private void setStyleToIndicateCorrectCommand() {
        cmdLine.getStyleClass().remove(ERROR_STYLE_CLASS);
        cmdLine.setText("");
    }

    /**
     * Sets the command box style to indicate an error
     */
    private void setStyleToIndicateIncorrectCommand() {
        cmdLine.getStyleClass().add(ERROR_STYLE_CLASS);
    }

    /**
     * Restores the command box text to the previously entered command
     */
    private void restoreCommandText() {
        cmdLine.setText(previousCommand);
    }

}
