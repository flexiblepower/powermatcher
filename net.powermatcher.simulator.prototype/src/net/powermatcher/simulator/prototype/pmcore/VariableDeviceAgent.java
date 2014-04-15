package net.powermatcher.simulator.prototype.pmcore;

public class VariableDeviceAgent extends DeviceAgent {

	public VariableDeviceAgent(String name) {
		super(name);
	}

	@Override
	protected Bid generateBid() {
		Bid bid = new Bid();

		for (int i = 0; i < bid.demand.length; i++) {
			bid.demand[i] = i * -20;
		}

		return bid;
	}
}
