package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.MarketBasis;

public class ClusterInfoModel {
	private MarketBasis marketBasis;

	private String clusterId;

	public MarketBasis getMarketBasis() {
		return marketBasis;
	}

	public void setMarketBasis(MarketBasis marketBasis) {
		this.marketBasis = marketBasis;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
}
