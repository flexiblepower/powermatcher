package net.powermatcher.integration.util;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

/**
 *
 * @author FAN
 * @version 2.1
 */
public class CsvExpectedResultsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvExpectedResultsReader.class);

    private final String inputFile;
    private CsvReader csvReader;
    private MarketBasis marketBasis;

    private Bid aggregatedBid;
    private double equilibriumPrice;

    public CsvExpectedResultsReader(String filename) throws IOException, DataFormatException {
        super();
        inputFile = filename;

        init();
    }

    private void init() throws IOException, DataFormatException {
        csvReader = new CsvReader(inputFile);
        readResultsData();
        closeFile();
    }

    private void readResultsData() throws IOException, DataFormatException {
        // Get market basis parameters bid from first line
        List<String> marketBasisParams = csvReader.nextLine();
        marketBasis = createMarketBasis(marketBasisParams);

        // Get aggregated bid from second line
        List<String> demandList = csvReader.nextLine();
        aggregatedBid = new Bid(marketBasis, createAggregatedBid(demandList));

        // Get equilibrium price from the third line
        List<String> priceList = csvReader.nextLine();
        equilibriumPrice = createEquilibriumPrice(priceList);
    }

    private double[] createAggregatedBid(List<String> demandList) throws DataFormatException {
        if (demandList != null && !demandList.isEmpty()) {
            double[] demand = new double[demandList.size()];
            int i = 0;
            for (String item : demandList) {
                try {
                    demand[i] = new Double(item);
                } catch (NumberFormatException e) {
                    throw new DataFormatException("Exception while parsing demand string : cannot convert" + item
                                                  + " to double");
                }
                i++;
            }
            return demand;
        } else {
            throw new DataFormatException("Aggregated bid format incorrect.");
        }
    }

    private double createEquilibriumPrice(List<String> priceList) throws DataFormatException {
        if (priceList != null && priceList.size() == 1) {
            try {
                return new Double(priceList.get(0));
            } catch (NumberFormatException e) {
                throw new DataFormatException("Parse exception : cannot convert equilibrium" + priceList.get(0)
                                              + " to double");
            }
        } else {
            throw new DataFormatException("Equilibrium price format incorrect.");
        }
    }

    private MarketBasis createMarketBasis(List<String> marketBasisData) throws DataFormatException {
        if (marketBasisData != null && !marketBasisData.isEmpty()) {
            String commodity = "electricity";
            String currency = "EUR";
            try {
                int priceSteps = Integer.valueOf(marketBasisData.get(0));
                double minimumPrice = Double.valueOf(marketBasisData.get(1));
                double maximumPrice = Double.valueOf(marketBasisData.get(2));

                MarketBasis mb = new MarketBasis(commodity, currency, priceSteps, minimumPrice, maximumPrice);
                marketBasis = mb;
            } catch (NumberFormatException e) {
                String msg = "Number format exception parsing market basis parameters. Could not construct market basis.";
                LOGGER.error(msg);
                throw new DataFormatException(msg);
            }
        } else {
            throw new DataFormatException("Could not construct market basis.");
        }
        return marketBasis;
    }

    private void closeFile() throws IOException {
        if (csvReader != null) {
            csvReader.closeReader();
        }
    }

    public Bid getAggregatedBid() {
        return aggregatedBid;
    }

    public double getEquilibriumPrice() {
        return equilibriumPrice;
    }

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }
}
