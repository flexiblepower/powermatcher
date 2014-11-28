package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PmMessageSerializer {
	
	public String serializeBid(Bid bid) {
		// Convert to JSON and send
		BidModel bidModel = new BidModel();
		bidModel.setBidNumber(bid.getBidNumber());
		
		// Caution, include either pricepoints or demand, not both!
		PricePoint[] pricePoints = bid.getPricePoints();
		if (pricePoints == null || pricePoints.length == 0) {
			bidModel.setDemand(bid.getDemand());
		} else  {
			bidModel.setPricePoints(convertPricePoints(pricePoints));
		}
		
		bidModel.setMarketBasis(convertMarketBasis(bid.getMarketBasis()));
		
		PmMessage message = new PmMessage();
		message.setPayloadType(PmMessage.PayloadType.BID);
		message.setPayload(bidModel);
		
		Gson gson = new Gson();
		return gson.toJson(message, PmMessage.class);
	}
	
	public String serializePrice(Price price) {
		PriceModel priceModel = new PriceModel();
		priceModel.setCurrentPrice(price.getCurrentPrice());
		priceModel.setMarketBasis(convertMarketBasis(price.getMarketBasis()));
		
		PmMessage message = new PmMessage();
		message.setPayloadType(PmMessage.PayloadType.PRICE);
		message.setPayload(priceModel);
		
		Gson gson = new Gson();
		return gson.toJson(message, PmMessage.class);
	}

	/* TODO
	public String serializeClusterInfo(String clusterId, MarketBasis marketBasis) {
		PmMessage message = new PmMessage();
		message.setPayloadType(PmMessage.PayloadType.CLUSTERINFO);
		message.setPayload(priceModel);
		
		Gson gson = new Gson();
		return gson.toJson(message, PmMessage.class);
	}
	 */
	
	public PmMessage deserialize(String message) {
		Gson gson = new GsonBuilder().registerTypeAdapter(PmMessage.class, new PmMessageDeserializer()).create();
		return gson.fromJson(message, PmMessage.class);
	}
	
	public Bid mapBid(BidModel bidModel) {
		Bid bid = null;

		// Caution, include either pricepoints or demand and not both.
		PricePointModel[] pricePointsModel  = bidModel.getPricePoints();
		if (pricePointsModel == null || pricePointsModel.length == 0) {
			 bid = new Bid(convertMarketBasis(
						bidModel.getMarketBasis()), 
						bidModel.getBidNumber(), 
						bidModel.getDemand());
		} else {
			bid = new Bid(convertMarketBasis(
					bidModel.getMarketBasis()), 
					bidModel.getBidNumber(), 
					convertPricePoints(pricePointsModel));
		}

		return bid;
	}
	
	public Price mapPrice(PriceModel priceModel) {
		Price price = new Price(convertMarketBasis(priceModel.getMarketBasis()), 
				priceModel.getCurrentPrice());
		return price;
	}
	
	private static PricePoint[] convertPricePoints(PricePointModel[] pricePointsModel) {
		// Convert price points
		PricePoint[] pricePoints = new PricePoint[pricePointsModel.length];
		for (int i = 0; i < pricePoints.length; i++) {
			pricePoints[i] = new PricePoint(); 
			pricePoints[i].setDemand(pricePointsModel[i].getDemand());
			pricePoints[i].setNormalizedPrice(pricePointsModel[i].getNormalizedPrice());
		}

		return pricePoints;
	}

	private static PricePointModel[] convertPricePoints(PricePoint[] pricePoints) {
		PricePointModel[] pricePointsModel = new PricePointModel[pricePoints.length];
		for (int i = 0; i < pricePoints.length; i++) {
			pricePointsModel[i] = new PricePointModel(); 
			pricePointsModel[i].setDemand(pricePoints[i].getDemand());
			pricePointsModel[i].setNormalizedPrice(pricePoints[i].getNormalizedPrice());
		}
		
		return pricePointsModel;
	}
	
   private static MarketBasisModel convertMarketBasis(MarketBasis marketBasis) {
		MarketBasisModel marketBasisModel = new MarketBasisModel();
		marketBasisModel.setCommodity(marketBasis.getCommodity());
		marketBasisModel.setCurrency(marketBasis.getCurrency());
		marketBasisModel.setMaximumPrice(marketBasis.getMaximumPrice());
		marketBasisModel.setMinimumPrice(marketBasis.getMinimumPrice());
		marketBasisModel.setPriceSteps(marketBasis.getPriceSteps());
    	return marketBasisModel;
    }
    
   private static MarketBasis convertMarketBasis(MarketBasisModel marketBasisModel) {
		MarketBasis marketBasis = new MarketBasis(
				marketBasisModel.getCommodity(), 
				marketBasisModel.getCurrency(), 
				marketBasisModel.getPriceSteps(), 
				marketBasisModel.getMinimumPrice(), 
				marketBasisModel.getMaximumPrice()); 
    	return marketBasis;
    }
}

