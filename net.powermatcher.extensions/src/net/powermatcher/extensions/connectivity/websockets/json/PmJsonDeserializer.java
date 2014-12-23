package net.powermatcher.extensions.connectivity.websockets.json;

import java.lang.reflect.Type;

import net.powermatcher.extensions.connectivity.websockets.data.BidModel;
import net.powermatcher.extensions.connectivity.websockets.data.ClusterInfoModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage;
import net.powermatcher.extensions.connectivity.websockets.data.PriceUpdateModel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Implementation of JsonDeserializer for a PmMessage. Handles the correct deserialization according to
 * {@link PmMessage.PayloadType}
 */
public class PmJsonDeserializer implements JsonDeserializer<PmMessage> {

    /**
     * Deserialize a JSON element according to the type.
     */
    @Override
    public PmMessage deserialize(JsonElement json, Type desiredType, JsonDeserializationContext context)
            throws JsonParseException {

        // Deserialize payload type
        JsonObject obj = json.getAsJsonObject();
        PmMessage message = new PmMessage();
        message.setPayloadType((PmMessage.PayloadType) context.deserialize(obj.get("payloadType"),
                PmMessage.PayloadType.class));

        // Deserialize payload
        JsonElement payload = obj.get("payload");
        if (message.getPayloadType() == PmMessage.PayloadType.PRICE_UPDATE) {
            message.setPayload((PriceUpdateModel) context.deserialize(payload, PriceUpdateModel.class));
        } else if (message.getPayloadType() == PmMessage.PayloadType.BID) {
            message.setPayload((BidModel) context.deserialize(payload, BidModel.class));
        } else if (message.getPayloadType() == PmMessage.PayloadType.CLUSTERINFO) {
            message.setPayload((ClusterInfoModel) context.deserialize(payload, ClusterInfoModel.class));
        }

        return message;
    }
}
