package net.powermatcher.simulator.prototype.pmcore;

public class SomeObjectiveAgent implements ObjectiveAgent {
	private String id;
	private Matcher matcher;
	private double currentPrice;

	public SomeObjectiveAgent(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public void setPrice(double price) {
		this.currentPrice = price;
	}

	@Override
	public void aggregatedBidUpdated(Bid aggregatedBid) {
		double targetAllocation = aggregatedBid.getAllocation(currentPrice);
		Bid priceSettingBid = Bid.mustRun(targetAllocation);
		this.matcher.processBidUpdate(this.getId(), priceSettingBid);
	}
}
