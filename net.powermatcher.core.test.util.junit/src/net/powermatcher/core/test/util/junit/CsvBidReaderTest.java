package net.powermatcher.core.test.util.junit;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.test.util.CsvBidReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CsvBidReaderTest {
	
	private static final String CSV_INPUT_FILE = "resources/Bids.csv";
	private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10, 1, 0);
	
	CsvBidReader reader;
	
	@Before
	public void setUp() throws Exception {
		reader = new CsvBidReader(CSV_INPUT_FILE, marketBasis);
	}
	
	@Test
	public void readAllLines() throws IOException, DataFormatException {
		List<String> list = null;
		BidInfo bid = null;
			
		double[] aggregatedDemand = new double[11];
		
		boolean stop = false;
		do {
			try {
				bid = this.reader.nextBidInfo();
				if (bid != null) {
					double[] demand = bid.getDemand();
					System.out.println(bid);
					
					for (int j = 0; j < demand.length; j++) {
						aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
					}
				}
				else {
					stop = true;
				}
			}
			catch (InvalidParameterException e) {
				System.err.println("Skipping incorrect bid specification. Reason: "+ e.getMessage());
				bid = null;
			}									
		} while (!stop);
	}
	
	@After
	public void tearDown() throws IOException {
			reader.closeFile();
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
