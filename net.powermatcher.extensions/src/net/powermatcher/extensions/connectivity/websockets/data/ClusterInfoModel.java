package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.MarketBasis;

/**
 * CusterInfo model class to transfer clusterId and {@link MarketBasis} data over the wire.
 */
public class ClusterInfoModel {
    private MarketBasisModel marketBasis;

    private String clusterId;

    public MarketBasisModel getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasisModel marketBasis) {
        this.marketBasis = marketBasis;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
