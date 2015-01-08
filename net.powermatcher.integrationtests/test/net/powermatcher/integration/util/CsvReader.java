package net.powermatcher.integration.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class CsvReader {

	private final static String DEFAULT_DELIMITER = ",";

	private File file;
	private String filename;

	private BufferedReader bufRdr;

	private String delimiters = DEFAULT_DELIMITER;

	public CsvReader(String filename) throws FileNotFoundException {
		super();
		this.filename = filename;
		init();
	}

	private void init() throws FileNotFoundException {
		file = new File(filename);
		bufRdr = new BufferedReader(new FileReader(file));
	}

	public List<String> nextLine() throws IOException {
		String line = null;
		List<String> items = null;

		if ((line = bufRdr.readLine()) != null) {
			items = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(line, this.delimiters);
			String token = null;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				items.add(token);
			}

			// Print the array
			printList(items);
		}
		return items;
	}

	private void printList(List<String> list) {
		String line = null;
		for (String item : list) {
			if (line == null) {
				line = item;
			} else {
				line = +',' + item;
			}
		}
	}

	public void closeReader() throws IOException {
		if (bufRdr != null) {
			bufRdr.close();
		}
	}

	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}
}
