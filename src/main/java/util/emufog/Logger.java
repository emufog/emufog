package util.emufog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The logger class offers to write to the console and the log to save it at a later time.
 * Different levels of log entries can be used. Uses singleton pattern.
 */
public class Logger {

    /* unique instance of the logger class */
    private static Logger INSTANCE;

    /* lines of log entries to write to a file */
    private final List<String> lines;

    /* separator object to have a consistent look */
    private final String separator;

    /**
     * Creates a new logger instance.
     */
    private Logger() {
        lines = new ArrayList<>();
        separator = "##############################################################";
    }

    /**
     * Returns the unique logger instance. In case there is none yet the
     * method creates a new object and returns it.
     *
     * @return the unique logger instance
     */
    public static Logger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Logger();
        }

        return INSTANCE;
    }

    /**
     * Write the given message to the console and stores it in the log to write it to an output file.
     * Uses the INFO level as per default.
     *
     * @param msg message to append to the log
     */
    public void log(String msg) {
        log(msg, LoggerLevel.INFO);
    }

    /**
     * Write the given message to the console and stores it in the log to write it to an output file.
     * Uses the specified logger level.
     *
     * @param msg   message to append to the log
     * @param level priority level of the logger
     */
    public void log(String msg, LoggerLevel level) {
        if (msg != null) {

            switch (level) {
                case WARNING:
                    msg = "[WARNING] " + msg;
                    break;
                case ERROR:
                    msg = "[ERROR]" + msg;
                    break;
                case ADVANCED:
                    msg = "[ADVANCED]" + msg;
                    break;
            }

            System.out.println(msg);
            lines.add(msg);
        }
    }

    /**
     * Adds a separator object with the given logger level to the log to increase readability.
     *
     * @param level level to apply to the separator
     */
    public void logSeparator(LoggerLevel level) {
        log(separator, level);
    }

    /**
     * Adds a separator object to the log to increase readability.
     */
    public void logSeparator() {
        log(separator, LoggerLevel.INFO);
    }

    /**
     * Converts a start and end point in nano seconds to a ms string to log.
     *
     * @param start start point in ns
     * @param end   end point in ns
     * @return string in ms format
     */
    public static String convertToMs(long start, long end) {
        return (double) (end - start) / 1000000 + "ms";
    }

    /**
     * Writes the log to the given file and resets it.
     *
     * @param path path to write the output to
     */
    public void saveLogFile(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("The given path object is not initialized.");
        }

        try {
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            lines.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
