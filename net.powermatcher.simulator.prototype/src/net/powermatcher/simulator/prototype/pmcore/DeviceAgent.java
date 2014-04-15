package net.powermatcher.simulator.prototype.pmcore;


public abstract class DeviceAgent extends ActiveObject implements Agent {
	private Matcher matcher;

	public DeviceAgent(String name) {
		super(name);
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public String getId() {
		return this.name;
	}

	@Override
	public void doUpdate() {
		super.doUpdate();

		for (int i = 0; i < 10; i++) {
			Math.pow(this.getCurrentTimeMillis(), i);
		}

		this.matcher.processBidUpdate(this.getId(), generateBid());

		// System.out.out.println("doUpdate " + this.name + " at " + new
		// Date(this.getCurrentTimeMillis()) + "(" + new Date()+ ")");
	}

	protected abstract Bid generateBid();

	@Override
	public void setPrice(double price) {
		// System.out.out.println("setPrice " + this.name + " at " + new
		// Date(this.getCurrentTimeMillis()) + "(" + new Date()+ ")");
	}
}
