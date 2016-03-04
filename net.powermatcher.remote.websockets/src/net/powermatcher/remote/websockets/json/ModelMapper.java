package net.powermatcher.remote.websockets.json;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.remote.websockets.data.BidModel;
import net.powermatcher.remote.websockets.data.MarketBasisModel;
import net.powermatcher.remote.websockets.data.PriceUpdateModel;

/**
 * Helper class to mapp between net.powermatcher.api.data classes and model classed for wire transfer.
 *
 * @author FAN
 * @version 2.1
 */
public class ModelMapper {

    private ModelMapper() {
    }

    /**
     * Map from {@link BidModel} to {@link Bid}
     *
     * @param bidModel
     *            the bidmodel to map
     * @return a mapped {@link Bid}
     */
    public static BidUpdate mapBidUpdate(BidModel bidModel) {
        BidUpdate bidUpdate = null;

        MarketBasis marketBasis = convertMarketBasis(bidModel.getMarketBasis());

        // Include either pricepoints or demand and not both.
        bidUpdate = new BidUpdate(new Bid(marketBasis, bidModel.getDemand()), bidModel.getBidNumber());

        return bidUpdate;
    }

    /**
     * Map from {@link PriceUpdateModel} to {@link PriceUpdate}
     *
     * @param priceUpdateModel
     *            the priceupdate to map
     * @return a mapped {@link PriceUpdate}
     */
    public static PriceUpdate mapPriceUpdate(PriceUpdateModel priceUpdateModel) {
        Price price = new Price(convertMarketBasis(priceUpdateModel.getMarketBasis()),
                                priceUpdateModel.getPriceValue());
        PriceUpdate priceUpdate = new PriceUpdate(price, priceUpdateModel.getBidNumber());
        return priceUpdate;
    }

    /**
     * Convert a {@link MarketBasis} to a {@link MarketBasisModel}
     *
     * @param marketBasis
     *            the market basis
     * @return a {@link MarketBasisModel}
     */
    public static MarketBasisModel convertMarketBasis(MarketBasis marketBasis) {
        MarketBasisModel marketBasisModel = new MarketBasisModel();
        marketBasisModel.setCommodity(marketBasis.getCommodity());
        marketBasisModel.setCurrency(marketBasis.getCurrency());
        marketBasisModel.setMaximumPrice(marketBasis.getMaximumPrice());
        marketBasisModel.setMinimumPrice(marketBasis.getMinimumPrice());
        marketBasisModel.setPriceSteps(marketBasis.getPriceSteps());
        return marketBasisModel;
    }

    /**
     * Convert a {@link MarketBasisModel} to a {@link MarketBasis}
     *
     * @param marketBasisModel
     *            the market basis model
     * @return a {@link MarketBasis}
     */
    public static MarketBasis convertMarketBasis(MarketBasisModel marketBasisModel) {
        MarketBasis marketBasis = new MarketBasis(marketBasisModel.getCommodity(),
                                                  marketBasisModel.getCurrency(),
                                                  marketBasisModel.getPriceSteps(),
                                                  marketBasisModel.getMinimumPrice(),
                                                  marketBasisModel.getMaximumPrice());
        return marketBasis;
    }
}
