package net.powermatcher.core.test.util.junit;

import java.io.IOException;
import java.util.List;

import net.powermatcher.core.test.util.CsvReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CsvReaderTest {

	private static final String CSV_INPUT_FILE = "resources/Bids.csv";
	CsvReader reader;
	
	@Before
	public void setUp() throws Exception {
		reader = new CsvReader(CSV_INPUT_FILE);
	}
	
	@Test
	public void readAllLines() throws IOException {
		List<String> list = null;
		while ((list = reader.nextLine()) != null) {
			printList(list);
		}
	}
	
	@After
	public void tearDown() throws IOException {
			reader.closeReader();
	}
	
	private void printList(List<String> list) {
		String line = null;
		for (String item : list) {
			if (line == null) {
				line = item;
			}
			else {
				line = line + ',' + item;
			}
		}
		System.out.println(line);
	}
}
