package net.powermatcher.extensions.connectivity.websockets.json;


import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.extensions.connectivity.websockets.data.BidModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage;
import net.powermatcher.extensions.connectivity.websockets.data.PriceModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage.PayloadType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PmJsonSerializer {
	
	public String serializeBid(Bid bid) {
		// Convert to JSON and send
		BidModel bidModel = new BidModel();
		bidModel.setBidNumber(bid.getBidNumber());
		bidModel.setMarketBasis(ModelMapper.convertMarketBasis(bid.getMarketBasis()));
		
		// Caution, include either pricepoints or demand, not both.
		if (bid instanceof ArrayBid) {
			bidModel.setDemand(((ArrayBid)bid).getDemand());
		} else {
			bidModel.setPricePoints(ModelMapper.convertPricePoints(((PointBid)bid).getPricePoints()));
		}

		// Create PM Message with bid information
		PmMessage message = new PmMessage();
		message.setPayloadType(PmMessage.PayloadType.BID);
		message.setPayload(bidModel);
		
		// Create JSON for PM Message
		Gson gson = new Gson();
		return gson.toJson(message, PmMessage.class);
	}
	
	public String serializePriceUpdate(PriceUpdate priceUpdate) {
		PriceModel priceModel = new PriceModel();
		priceModel.setBidNumber(priceModel.getBidNumber());
		priceModel.setPriceValue(priceUpdate.getPrice().getPriceValue());
		priceModel.setMarketBasis(ModelMapper.convertMarketBasis(priceUpdate.getPrice().getMarketBasis()));
		
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
		Gson gson = new GsonBuilder().registerTypeAdapter(PmMessage.class, new PmJsonDeserializer()).create();
		return gson.fromJson(message, PmMessage.class);
	}
}

