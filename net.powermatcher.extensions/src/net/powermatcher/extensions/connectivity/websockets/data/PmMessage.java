package net.powermatcher.extensions.connectivity.websockets.data;

/**
 * PmMessage class to encapsulate {@link BidModel}, {@link PriceUpdateModel} or {@link CusterinfoModel}.
 * WebSocket communication does not contain a native RPC-style, so the same message object is used for data exchange.
 * Therefore this message contains a type indicator to allow the different objects to be transferred reliably.
 */
public class PmMessage {
	/**
	 * Type indicator for the payload type.
	 */
	public enum PayloadType {
		BID,
		PRICE_UPDATE,
		CLUSTERINFO
	}

	/**
	 * Indicator to specify the contents of the payload.
	 */
	private PayloadType payloadType;

	/**
	 * The payload which could contain {@link BidModel}, {@link PriceUpdateModel} or {@link CusterinfoModel}.
	 */
	private Object payload;
	
	public PayloadType getPayloadType() {
		return payloadType;
	}
	
	public void setPayloadType(PayloadType payloadType) {
		this.payloadType = payloadType;
	}
	
	public Object getPayload() {
		return payload;
	}
	
	public void setPayload(Object payload) {
		this.payload = payload;
	}
}
