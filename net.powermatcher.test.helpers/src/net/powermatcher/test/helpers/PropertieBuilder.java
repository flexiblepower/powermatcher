package net.powermatcher.test.helpers;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.MarketBasis;

public class PropertieBuilder {
    private final Map<String, Object> properties = new HashMap<String, Object>();

    public PropertieBuilder add(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public PropertieBuilder agentId(String agentId) {
        return add("agentId", agentId);
    }

    public PropertieBuilder clusterId(String clusterId) {
        return add("clusterId", clusterId);
    }

    public PropertieBuilder desiredParentId(String desiredParentId) {
        return add("desiredParentId", desiredParentId);
    }

    public PropertieBuilder bidUpdateRate(int bidUpdateRate) {
        return add("bidUpdateRate", bidUpdateRate);
    }

    public PropertieBuilder priceUpdateRate(int priceUpdateRate) {
        return add("priceUpdateRate", priceUpdateRate);
    }

    public PropertieBuilder minTimeBetweenBids(int minTimeBetweenBids) {
        return add("minTimeBetweenBids", minTimeBetweenBids);
    }

    public PropertieBuilder marketBasis(MarketBasis marketBasis) {
        add("commodity", marketBasis.getCommodity());
        add("currency", marketBasis.getCurrency());
        add("maximumPrice", marketBasis.getMaximumPrice());
        add("minimumPrice", marketBasis.getMinimumPrice());
        add("priceIncrement", marketBasis.getPriceIncrement());
        add("priceSteps", marketBasis.getPriceSteps());
        return this;
    }

    public Map<String, Object> build() {
        return new HashMap<String, Object>(properties);
    }
}
