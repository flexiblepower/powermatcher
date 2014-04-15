package net.powermatcher.simulator.prototype.pmcore;

public class RandomMustRunDeviceAgent extends DeviceAgent {
	private double load = Math.random() * 100;

	public RandomMustRunDeviceAgent(String name) {
		super(name);
	}

	@Override
	protected Bid generateBid() {
		load += Math.random() * 200 - 100;

		if (load > 1000) {
			load = 1000 - Math.random() * 200;
		} else if (load < 0) {
			load = Math.random() * 200;
		}

		return Bid.mustRun(load);
	}
}
