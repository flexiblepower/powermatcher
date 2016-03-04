package net.powermatcher.remote.websockets.data;

import net.powermatcher.api.data.MarketBasis;

/**
 * CusterInfo model class to transfer clusterId and {@link MarketBasis} data over the wire.
 * 
 * @author FAN
 * @version 2.1
 */
public class ClusterInfoModel {
    private MarketBasisModel marketBasis;

    private String clusterId;

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasisModel getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasisModel marketBasis) {
        this.marketBasis = marketBasis;
    }

    /**
     * @return the current value of clusterId.
     */
    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
