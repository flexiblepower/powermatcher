package net.powermatcher.extensions.connectivity.websockets.data;

public class PmMessage {
	public enum PayloadType {
		BID,
		PRICE,
		CLUSTERINFO
	}
	
	private PayloadType payloadType;

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
