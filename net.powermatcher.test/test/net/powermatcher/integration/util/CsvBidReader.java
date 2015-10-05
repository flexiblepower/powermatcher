package net.powermatcher.integration.util;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class CsvBidReader {

    String inputFile;
    CsvReader csvReader;
    MarketBasis marketBasis;

    public CsvBidReader() throws IOException {
        super();
    }

    public CsvBidReader(MarketBasis marketBasis) throws IOException {
        super();
        this.marketBasis = marketBasis;
    }

    public CsvBidReader(String filename, MarketBasis marketBasis) throws IOException {
        super();
        inputFile = filename;
        this.marketBasis = marketBasis;

        init();
    }

    private void init() throws IOException {
        closeFile();
        csvReader = new CsvReader(inputFile);
    }

    public Bid nextBid() throws IOException, DataFormatException {
        List<String> demandList = null;
        while ((demandList = csvReader.nextLine()) != null) {
            try {
                return new Bid(marketBasis, demandFromList(demandList));
            } catch (IllegalArgumentException ex) {
                // Illegal bid, ignore line
            }
        }
        return null;
    }

    public void closeFile() throws IOException {
        if (csvReader != null) {
            csvReader.closeReader();
        }
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) throws IOException {
        this.inputFile = inputFile;
        init();
    }

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

    private double[] demandFromList(List<String> demandList) throws DataFormatException {
        double[] demand = new double[demandList.size()];
        int i = 0;
        for (String item : demandList) {
            try {
                demand[i] = new Double(item);
            } catch (NumberFormatException e) {
                throw new DataFormatException("Parse exception : cannot convert" + item + " to double");
            }

            i++;
        }
        return demand;
    }
}
