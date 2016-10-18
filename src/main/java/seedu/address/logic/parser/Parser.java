package seedu.address.logic.parser;

import seedu.address.logic.commands.*;
import seedu.address.commons.util.StringUtil;
import seedu.address.commons.exceptions.IllegalValueException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.ListCommand.AFTER_FLAG;
import static seedu.address.logic.commands.ListCommand.BEFORE_FLAG;

/**
 * Parses user input.
 */
public class Parser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    private static final Pattern PERSON_INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    // TODO: Use tokenizer for these
    private static final Pattern LIST_ARGS_FORMAT =
            Pattern.compile("(?<startDate>\\s*" + AFTER_FLAG + "\\d{4}-\\d{1,2}-\\d{1,2})?"
                    + "(?<endDate>\\s*" + BEFORE_FLAG + "\\d{4}-\\d{1,2}-\\d{1,2})?"
                    + "(?<keywords>\\s*\\S*(?:\\s+\\S+)*)"); // zero or more keywords separated by whitespace 

    private static final Pattern FLOATING_TASK_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<title>[^/]+)"
            		+ "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags; 
    
    private static final Pattern DEADLINE_DATA_ARGS_FORMAT = // '/' forward slashes are reserved for delimiter prefixes
            Pattern.compile("(?<title>[^/]+)"
            		+ "(?<deadlineArguments>(?: deadline/\\d{4}-\\d{1,2}-\\d{1,2} \\d{2}:\\d{2}))" // Date time format: DD/MM/YYYY/HH:MM
            		+ "(?<tagArguments>(?: t/[^/]+)*)"); // variable number of tags; 
    
    private static final Pattern EDIT_TASK_ARGS_FORMAT = Pattern
            .compile("(?<targetIndex>\\d+)\\s*(?<title>[\\s\\w\\d]*)" + "(?<tagArguments>(?: t/[^/]+)*)");

    public Parser() {
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput
     *            full user input string
     * @return the command based on the user input
     */
    public Command parseCommand(String userInput) {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
        switch (commandWord) {

        case AddCommand.COMMAND_WORD:
            return prepareAdd(arguments);

        case EditCommand.COMMAND_WORD:
            return prepareEdit(arguments);

        case SelectCommand.COMMAND_WORD:
            return prepareSelect(arguments);

        case DeleteCommand.COMMAND_WORD:
            return prepareDelete(arguments);

        case ClearCommand.COMMAND_WORD:
            return new ClearCommand();

        case MarkCommand.COMMAND_WORD:
            return prepareMark(arguments);
            
        case UnmarkCommand.COMMAND_WORD:
            return prepareUnmark(arguments);

        case ListCommand.COMMAND_WORD:
            return prepareList(arguments);

        case ExitCommand.COMMAND_WORD:
            return new ExitCommand();

        case HelpCommand.COMMAND_WORD:
            return new HelpCommand();

        default:
            return new IncorrectCommand(MESSAGE_UNKNOWN_COMMAND);
        }
    }

    /**
     * Parses arguments in the context of the add task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareAdd(String args) {
    	args = args.trim();
        final Matcher deadlineMatcher = DEADLINE_DATA_ARGS_FORMAT.matcher(args);
        final Matcher floatingTaskMatcher = FLOATING_TASK_DATA_ARGS_FORMAT.matcher(args);
        // Validate arg string format
        if (!floatingTaskMatcher.matches() && !deadlineMatcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        if (deadlineMatcher.matches()) {
        	try {
        		return new AddCommand(deadlineMatcher.group("title"), getDeadlineFromArgument(deadlineMatcher.group("deadlineArguments")), getTagsFromArgs(deadlineMatcher.group("tagArguments")));
        	} catch (IllegalValueException ive) {
                return new IncorrectCommand(ive.getMessage());
            }
        	
        } else {
        	try {
                return new AddCommand(floatingTaskMatcher.group("title"), getTagsFromArgs(floatingTaskMatcher.group("tagArguments")));
            } catch (IllegalValueException ive) {
                return new IncorrectCommand(ive.getMessage());
            }
        }
    }

    /**
     * Extracts the new task's tags from the add command's tag arguments
     * string. Merges duplicate tag strings.
     */
    private static Set<String> getTagsFromArgs(String tagArguments) throws IllegalValueException {
        // no tags
        if (tagArguments.isEmpty()) {
            return Collections.emptySet();
        }
        // replace first delimiter prefix, then split
        final Collection<String> tagStrings = Arrays.asList(tagArguments.replaceFirst(" t/", "").split(" t/"));
        return new HashSet<>(tagStrings);
    }

    /**
     * Parses arguments in the context of the edit task command.
     * 
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareEdit(String args) {
        final Matcher matcher = EDIT_TASK_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        } else {
            Optional<Integer> index = parseIndex(matcher.group("targetIndex"));
            if (!index.isPresent()) {
                return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
            }
            try {
                return new EditCommand(index.get(), matcher.group("title"),
                        getTagsFromArgs(matcher.group("tagArguments")));
            } catch (IllegalValueException ive) {
                return new IncorrectCommand(ive.getMessage());
            }
        }
    }
    
    /**
     * Extracts the new entry's deadline from the add command's tag arguments
     * string. Format: YYYY-MM-DD HH:MM
     */
    private static LocalDateTime getDeadlineFromArgument(String deadlineArguments) throws IllegalValueException {
        if (deadlineArguments.isEmpty()) {
            return LocalDateTime.now();
        }
        // remove the tag.
        final List<String> cleanedStrings = Arrays.asList(deadlineArguments.replaceFirst(" deadline/", "").split(" "));
        return LocalDateTime.parse(cleanedStrings.get(0) + "T" + cleanedStrings.get(1) + ":00");
    }
    
    /**
     * Parse LocalDateTime of start date from an input string
     * string. Format: YYYY-MM-DD
     */
    private static LocalDateTime getStartDateFromArgument(String dateTime) throws IllegalValueException {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        
        // remove the tag.
        final String cleanedString = dateTime.trim().replaceFirst(AFTER_FLAG, "") + "T" + "00:00:00";
        return LocalDateTime.parse(cleanedString);
    }
    
    /**
     * Parse LocalDateTime of end start from an input string
     * string. Format: YYYY-MM-DD
     */
    private static LocalDateTime getEndDateFromArgument(String dateTime) throws IllegalValueException {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        
        // remove the tag.
        final String cleanedString = dateTime.trim().replaceFirst(BEFORE_FLAG, "") + "T" + "23:59:59";
        return LocalDateTime.parse(cleanedString);
    }

    /**
     * Parses arguments in the context of the delete task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareDelete(String args) {

        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        return new DeleteCommand(index.get());
    }

    /**
     * Parses arguments in the context of the mark task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareMark(String args) {
        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }

        return new MarkCommand(index.get());
    }
    
    /**
     * Parses arguments in the context of the unmark task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareUnmark(String args) {
        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkCommand.MESSAGE_USAGE));
        }

        return new UnmarkCommand(index.get());
    }

    /**
     * Parses arguments in the context of the select task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareSelect(String args) {
        Optional<Integer> index = parseIndex(args);
        if (!index.isPresent()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));
        }

        return new SelectCommand(index.get());
    }

    /**
     * Returns the specified index in the {@code command} IF a positive unsigned
     * integer is given as the index. Returns an {@code Optional.empty()}
     * otherwise.
     */
    private Optional<Integer> parseIndex(String command) {
        final Matcher matcher = PERSON_INDEX_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String index = matcher.group("targetIndex");
        if (!StringUtil.isUnsignedInteger(index)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(index));

    }

    /**
     * Parses arguments in the context of the list task command.
     *
     * @param args
     *            full command args string
     * @return the prepared command
     */
    private Command prepareList(String args) {
        // Guard statement
        if (args.isEmpty()) {
            return new ListCommand();
        }
        
        final Matcher matcher = LIST_ARGS_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        try {
            final LocalDateTime startDate = getStartDateFromArgument(matcher.group("startDate"));
            final LocalDateTime endDate = getEndDateFromArgument(matcher.group("endDate"));
            // keywords delimited by whitespace
            String[] keywords = matcher.group("keywords").trim().split("\\s+");

            Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
            keywordSet.removeIf(s -> s.equals(""));
            
            ListCommand listCommand = new ListCommand(keywordSet);
            listCommand.setStartDate(startDate);
            listCommand.setEndDate(endDate);
            
            return listCommand;
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

}