package app.services;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class LogService {

    // Custom formatter class to ensure logs are on a single line
    private static class OneLineFormatter extends Formatter {
        private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

        @Override
        public String format(LogRecord record) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
            String timestamp = simpleDateFormat.format(new Date(record.getMillis()));
            return String.format("[%s] %s: %s%n", timestamp, record.getLevel().equals(Level.FINE) ? "DEBUG" : record.getLevel(), record.getMessage());
        }
    }

    private static final Logger logger = Logger.getLogger(LogService.class.getName());
    private static final String LOG_DIRECTORY = "logs/";
    private static final String LOG_FILE_PATTERN = "chatbot-%s.log";
    private static final int MAX_LOG_FILES = 10;

    // Static block to initialize logging (runs once when the app starts running)
    static {
        try {
            // Clean up default handlers
            LogManager.getLogManager().reset();

            // Ensure the log directory exists, create it if not
            Files.createDirectories(Paths.get(LOG_DIRECTORY));
            cleanOldLogs();

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String logFileName = String.format(LOG_FILE_PATTERN, date);

            FileHandler fileHandler = new FileHandler(LOG_DIRECTORY + logFileName, true);
            fileHandler.setFormatter(new OneLineFormatter());

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

            // Console handler for SEVERE logs only
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.SEVERE);
            consoleHandler.setFormatter(new OneLineFormatter());

            logger.addHandler(consoleHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize log handler: ", e);
        }
    }

    private static void cleanOldLogs() throws IOException {
        Path logDir = Paths.get(LOG_DIRECTORY);

        List<Path> logFiles = Files.list(logDir)
                .filter(path -> path.toString().endsWith(".log"))
                .sorted(Comparator.comparingLong(path -> path.toFile().lastModified()))
                .toList();

        for (int i = 0; i < logFiles.size() - MAX_LOG_FILES; i++) {
            try {
                Files.delete(logFiles.get(i));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to delete old log file: " + logFiles.get(i), e);
            }
        }
    }

    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    public static void logDebug(String message) {
        logger.log(Level.FINE, message);
    }

    public static void logWarning(String message) {
        logger.log(Level.WARNING, message);
    }

    public static void logError(String message) {
        logger.log(Level.SEVERE, message);
    }
}