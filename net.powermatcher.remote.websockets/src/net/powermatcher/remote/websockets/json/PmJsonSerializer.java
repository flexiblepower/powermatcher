package net.powermatcher.remote.websockets.json;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.remote.websockets.data.BidModel;
import net.powermatcher.remote.websockets.data.ClusterInfoModel;
import net.powermatcher.remote.websockets.data.PmMessage;
import net.powermatcher.remote.websockets.data.PriceUpdateModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implements a serializer for the net.powermatcher.api.data data types to JSON. Items are wrapped in a
 * {@link PmMessage} and correct type is set.
 *
 * @author FAN
 * @version 2.0
 */
public class PmJsonSerializer {

    /**
     * Serialize a {@link Bid} to JSON.
     *
     * @param bid
     *            the bid to serializer
     * @return a JSON string with a {@link Bid} wrapped in {@link PmMessage}.
     */
    public String serializeBidUpdate(final BidUpdate bidUpdate) {
        Bid bid = bidUpdate.getBid();

        // Convert to JSON and send
        BidModel bidModel = new BidModel();
        bidModel.setBidNumber(bidUpdate.getBidNumber());
        bidModel.setMarketBasis(ModelMapper.convertMarketBasis(bid.getMarketBasis()));

        // Include either pricepoints or demand, not both.
        if (bid instanceof ArrayBid) {
            bidModel.setDemand(((ArrayBid) bid).getDemand());
        } else {
            bidModel.setPricePoints(ModelMapper.convertPricePoints(((PointBid) bid).getPricePoints()));
        }

        // Create PM Message with bid information
        PmMessage message = new PmMessage();
        message.setPayloadType(PmMessage.PayloadType.BID);
        message.setPayload(bidModel);

        // Create JSON for PM Message
        Gson gson = new Gson();
        return gson.toJson(message, PmMessage.class);
    }

    /**
     * Serialize a {@link PriceUpdate} to JSON.
     *
     * @param bid
     *            the bid to serializer
     * @return a JSON string with a {@link PriceUpdate} wrapped in {@link PmMessage}.
     */
    public String serializePriceUpdate(final PriceUpdate priceUpdate) {
        PriceUpdateModel priceModel = new PriceUpdateModel();
        priceModel.setBidNumber(priceModel.getBidNumber());
        priceModel.setPriceValue(priceUpdate.getPrice().getPriceValue());
        priceModel.setMarketBasis(ModelMapper.convertMarketBasis(priceUpdate.getPrice().getMarketBasis()));

        PmMessage message = new PmMessage();
        message.setPayloadType(PmMessage.PayloadType.PRICE_UPDATE);
        message.setPayload(priceModel);

        Gson gson = new Gson();
        return gson.toJson(message, PmMessage.class);
    }

    /**
     * Serialize a clusterId and {@link MarketBasis} to JSON.
     *
     * @param clusterId
     *            the id of the cluster
     * @param marketBasis
     *            the market basis of the cluster
     * @return a JSON string with clusterId and {@link MarketBasis} wrapped in {@link PmMessage}.
     */
    public String serializeClusterInfo(final String clusterId, final MarketBasis marketBasis) {
        ClusterInfoModel customerModel = new ClusterInfoModel();
        customerModel.setClusterId(clusterId);
        customerModel.setMarketBasis(ModelMapper.convertMarketBasis(marketBasis));

        PmMessage message = new PmMessage();
        message.setPayloadType(PmMessage.PayloadType.CLUSTERINFO);
        message.setPayload(customerModel);

        Gson gson = new Gson();
        return gson.toJson(message, PmMessage.class);
    }

    /**
     * Deserialize a JSON string to {@link PmMessage}.
     *
     * @param message
     *            the JSON string
     * @return a {@link PmMessage} containing payload.
     */
    public PmMessage deserialize(String message) {
        Gson gson = new GsonBuilder().registerTypeAdapter(PmMessage.class, new PmJsonDeserializer()).create();
        return gson.fromJson(message, PmMessage.class);
    }
}
