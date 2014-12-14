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
import net.powermatcher.extensions.connectivity.websockets.data.PriceModel;
import net.powermatcher.extensions.connectivity.websockets.data.PricePointModel;

public class ModelMapper {
	public static Bid mapBid(BidModel bidModel) {
		Bid bid = null;

		MarketBasis marketBasis = convertMarketBasis(bidModel.getMarketBasis());
		
		// Caution, include either pricepoints or demand and not both.
		PricePointModel[] pricePointsModel  = bidModel.getPricePoints();
		if (pricePointsModel == null || pricePointsModel.length == 0) {
			 bid = new ArrayBid(marketBasis, 
						bidModel.getBidNumber(), 
						bidModel.getDemand());
		} else {
			bid = new PointBid(marketBasis, 
					bidModel.getBidNumber(), 
					convertPricePoints(marketBasis, pricePointsModel));
		}

		return bid;
	}
	
	public static PriceUpdate mapPriceUpdate(PriceModel priceModel) {
		Price price = new Price(convertMarketBasis(priceModel.getMarketBasis()), 
				priceModel.getPriceValue());
		PriceUpdate priceUpdate = new PriceUpdate(price, priceModel.getBidNumber());
		return priceUpdate;
	}
	
	public static PricePoint[] convertPricePoints(MarketBasis marketBasis, PricePointModel[] pricePointsModel) {
		// Convert price points
		PricePoint[] pricePoints = new PricePoint[pricePointsModel.length];
		for (int i = 0; i < pricePoints.length; i++) {
			pricePoints[i] = new PricePoint(marketBasis, pricePointsModel[i].getPrice(), 
					pricePointsModel[i].getDemand());
		}

		return pricePoints;
	}

	public static PricePointModel[] convertPricePoints(PricePoint[] pricePoints) {
		PricePointModel[] pricePointsModel = new PricePointModel[pricePoints.length];
		for (int i = 0; i < pricePoints.length; i++) {
			pricePointsModel[i] = new PricePointModel(); 
			pricePointsModel[i].setDemand(pricePoints[i].getDemand());
			pricePointsModel[i].setPrice(pricePoints[i].getPrice().getPriceValue());
		}
		
		return pricePointsModel;
	}
	
	public static MarketBasisModel convertMarketBasis(MarketBasis marketBasis) {
		MarketBasisModel marketBasisModel = new MarketBasisModel();
		marketBasisModel.setCommodity(marketBasis.getCommodity());
		marketBasisModel.setCurrency(marketBasis.getCurrency());
		marketBasisModel.setMaximumPrice(marketBasis.getMaximumPrice());
		marketBasisModel.setMinimumPrice(marketBasis.getMinimumPrice());
		marketBasisModel.setPriceSteps(marketBasis.getPriceSteps());
    	return marketBasisModel;
    }
    
   public static MarketBasis convertMarketBasis(MarketBasisModel marketBasisModel) {
		MarketBasis marketBasis = new MarketBasis(
				marketBasisModel.getCommodity(), 
				marketBasisModel.getCurrency(), 
				marketBasisModel.getPriceSteps(), 
				marketBasisModel.getMinimumPrice(), 
				marketBasisModel.getMaximumPrice()); 
    	return marketBasis;
    }
}
