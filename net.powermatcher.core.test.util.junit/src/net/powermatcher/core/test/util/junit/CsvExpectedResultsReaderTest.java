package net.powermatcher.core.test.util.junit;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.test.util.CsvExpectedResultsReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class CsvExpectedResultsReaderTest {

	private static final String CSV_INPUT_FILE = "resources/AggBidPrice.csv";
	private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10, 1, 0);
	
	CsvExpectedResultsReader reader;
	
	@Before
	public void setUp() throws Exception {
		//reader = new CsvExpectedResultsReader(CSV_INPUT_FILE, marketBasis);
	}
	
	@Test
	public void getAggregatedBid() throws IOException, DataFormatException {
		BidInfo bid = reader.getAggregatedBid();
		System.out.println("Aggregated bid: " + bid);
		
		double expected[] = {20944,18733,18733,18733,18733,18733,16234,13471,9055,6334,-1282};
		assertArrayEquals(expected, bid.getDemand(), 0.0);
	}

	@Test
	public void getEquilibriumPrice() throws IOException, DataFormatException {
		double price = reader.getEquilibriumPrice();
		System.out.println("Equilibrium price: " + price);
	
		double expected = 49.158;
		assertEquals(expected, price, 0.0);
	}

	@After
	public void tearDown() throws IOException {
	//		reader.closeFile();
	}
}
