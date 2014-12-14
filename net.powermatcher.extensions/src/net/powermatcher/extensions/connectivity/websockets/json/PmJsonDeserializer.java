package net.powermatcher.extensions.connectivity.websockets.json;

import java.lang.reflect.Type;

import net.powermatcher.extensions.connectivity.websockets.data.BidModel;
import net.powermatcher.extensions.connectivity.websockets.data.ClusterInfoModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage;
import net.powermatcher.extensions.connectivity.websockets.data.PriceModel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class PmJsonDeserializer implements JsonDeserializer<PmMessage> {

	@Override
	public PmMessage deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext context) throws JsonParseException {

		// Deserialize payload type
		JsonObject obj = json.getAsJsonObject();
		PmMessage message = new PmMessage();
		message.setPayloadType((PmMessage.PayloadType) context.deserialize(
				obj.get("payloadType"), PmMessage.PayloadType.class));

		// Deserialize payload
		JsonElement payload = obj.get("payload");
		if (message.getPayloadType() == PmMessage.PayloadType.PRICE) {
			message.setPayload((PriceModel)context.deserialize(payload, PriceModel.class));
		} else if (message.getPayloadType() == PmMessage.PayloadType.PRICE) {
			message.setPayload((BidModel)context.deserialize(payload, BidModel.class));
		} else if (message.getPayloadType() == PmMessage.PayloadType.CLUSTERINFO) {
			message.setPayload((ClusterInfoModel)context.deserialize(payload, ClusterInfoModel.class));
		}

		return message;
	}
}
