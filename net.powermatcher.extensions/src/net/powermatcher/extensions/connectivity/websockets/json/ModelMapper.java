package net.powermatcher.extensions.connectivity.websockets.json;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.extensions.connectivity.websockets.data.BidModel;
import net.powermatcher.extensions.connectivity.websockets.data.MarketBasisModel;
import net.powermatcher.extensions.connectivity.websockets.data.PriceUpdateModel;
import net.powermatcher.extensions.connectivity.websockets.data.PricePointModel;

/**
 * Helper class to mapp between net.powermatcher.api.data classes and model classed for wire transfer.
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
    public static Bid mapBid(BidModel bidModel) {
        Bid bid = null;

        MarketBasis marketBasis = convertMarketBasis(bidModel.getMarketBasis());

        // Include either pricepoints or demand and not both.
        PricePointModel[] pricePointsModel = bidModel.getPricePoints();
        if (pricePointsModel == null || pricePointsModel.length == 0) {
            bid = new ArrayBid(marketBasis, bidModel.getBidNumber(), bidModel.getDemand());
        } else {
            bid = new PointBid(marketBasis, bidModel.getBidNumber(), convertPricePoints(marketBasis, pricePointsModel));
        }

        return bid;
    }

    /**
     * Map from {@link PriceUpdateModel} to {@link PriceUpdate}
     * 
     * @param priceUpdateModel
     *            the priceupdate to map
     * @return a mapped {@link PriceUpdate}
     */
    public static PriceUpdate mapPriceUpdate(PriceUpdateModel priceUpdateModel) {
        Price price = new Price(convertMarketBasis(priceUpdateModel.getMarketBasis()), priceUpdateModel.getPriceValue());
        PriceUpdate priceUpdate = new PriceUpdate(price, priceUpdateModel.getBidNumber());
        return priceUpdate;
    }

    /**
     * Convert a list of {@link PricePointModel} to a list of {@link PricePoint}
     * 
     * @param marketBasis
     *            the marketbasis to use
     * @param pricePointsModel
     *            the list of pricepointmodels
     * @return a list of {@link PricePoint}
     */
    public static PricePoint[] convertPricePoints(MarketBasis marketBasis, PricePointModel[] pricePointsModel) {
        // Convert price points
        PricePoint[] pricePoints = new PricePoint[pricePointsModel.length];
        for (int i = 0; i < pricePoints.length; i++) {
            pricePoints[i] = new PricePoint(marketBasis, pricePointsModel[i].getPrice(),
                    pricePointsModel[i].getDemand());
        }

        return pricePoints;
    }

    /**
     * Convert a list of {@link PricePoint} to a list of {@link PricePointModel}
     * 
     * @param pricePointsModel
     *            the list of pricepoint
     * @return a list of {@link PricePointModel}
     */
    public static PricePointModel[] convertPricePoints(PricePoint[] pricePoints) {
        PricePointModel[] pricePointsModel = new PricePointModel[pricePoints.length];
        for (int i = 0; i < pricePoints.length; i++) {
            pricePointsModel[i] = new PricePointModel();
            pricePointsModel[i].setDemand(pricePoints[i].getDemand());
            pricePointsModel[i].setPrice(pricePoints[i].getPrice().getPriceValue());
        }

        return pricePointsModel;
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
        MarketBasis marketBasis = new MarketBasis(marketBasisModel.getCommodity(), marketBasisModel.getCurrency(),
                marketBasisModel.getPriceSteps(), marketBasisModel.getMinimumPrice(),
                marketBasisModel.getMaximumPrice());
        return marketBasis;
    }
}
