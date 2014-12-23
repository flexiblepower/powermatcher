package net.powermatcher.integration.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVLogReader {

    private static final String CSV_LOG_DELIMITERS = ";";
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVLogReader.class);

    private CSVLogReader() {
    }

    public static boolean containsLogLines(String filename) {
        CsvReader reader = null;
        try {
            reader = new CsvReader(filename);
            reader.setDelimiters(CSV_LOG_DELIMITERS);

            List<String> header = reader.nextLine();
            if (header != null) {
                List<String> firstLine = reader.nextLine();
                if (firstLine != null) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Log file " + filename + " does not exist");
        } catch (IOException e) {
            LOGGER.error("IO error occurred while reading log file " + filename, e);
        } finally {
            if (reader != null) {
                try {
                    reader.closeReader();
                } catch (IOException e) {
                    LOGGER.error("IO exception. File " + filename + " could not be closed.", e);
                }
            }
        }
        return false;
    }

    public static boolean containsLogLinesForToken(String filename, String token) {
        CsvReader reader = null;
        try {
            reader = new CsvReader(filename);
            reader.setDelimiters(CSV_LOG_DELIMITERS);

            List<String> header = reader.nextLine();
            if (header != null) {
                List<String> line = null;
                while ((line = reader.nextLine()) != null) {
                    for (String item : line) {
                        if (item.trim().equals(token)) {
                            return true;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Log file " + filename + " does not exist", e);
        } catch (IOException e) {
            LOGGER.error("IO error occurred while reading log file " + filename, e);
        } finally {
            if (reader != null) {
                try {
                    reader.closeReader();
                } catch (IOException e) {
                    LOGGER.error("IO exception. File " + filename + " could not be closed.", e);
                }
            }
        }
        return false;
    }
}
