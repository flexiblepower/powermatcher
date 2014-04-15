package net.powermatcher.core.test.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class CSVLogReader {
	
	private final static String CSV_LOG_DELIMITERS = ";";

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
			System.err.println("Log file " + filename + " does not exist");
			
		} catch (IOException e) {
			System.err.println("IO error occurred while reading log file " + filename);
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.closeReader();
				} catch (IOException e) {
					System.err.println("IO exception. File " + filename + " could not be closed.");
					e.printStackTrace();
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
						if (item.trim().equals(token) ){
							return true;
						}
					}	
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Log file " + filename + " does not exist");
			
		} catch (IOException e) {
			System.err.println("IO error occurred while reading log file " + filename);
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.closeReader();
				} catch (IOException e) {
					System.err.println("IO exception. File " + filename + " could not be closed.");
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
