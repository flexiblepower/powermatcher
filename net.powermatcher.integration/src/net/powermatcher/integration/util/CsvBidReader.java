package net.powermatcher.integration.util;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;

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
        this.inputFile = filename;
        this.marketBasis = marketBasis;

        this.init();
    }

    private void init() throws IOException {
        this.closeFile();
        csvReader = new CsvReader(inputFile);
    }

    public ArrayBid nextBid() throws IOException, DataFormatException {
        ArrayBid bid = null;

        List<String> demandList = csvReader.nextLine();
        if (demandList != null) {
            bid = new ArrayBid(marketBasis, 0, demandFromList(demandList));
        }
        return bid;
    }

    public void closeFile() throws IOException {
        if (this.csvReader != null) {
            this.csvReader.closeReader();
        }
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) throws IOException {
        this.inputFile = inputFile;
        this.init();
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
