package net.powermatcher.simulator.prototype.pmcore;

public class ModulatedDeviceAgent extends DeviceAgent {
	public ModulatedDeviceAgent(String name) {
		super(name);
	}

	@Override
	protected Bid generateBid() {
		return Bid.mustRun(Math.random() * 1000 - 500);
	}
}
