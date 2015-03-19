package net.powermatcher.test.helpers;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.MarketBasis;

public class PropertiesBuilder {
    private final Map<String, Object> properties = new HashMap<String, Object>();

    public PropertiesBuilder add(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public PropertiesBuilder agentId(String agentId) {
        return add("agentId", agentId);
    }

    public PropertiesBuilder clusterId(String clusterId) {
        return add("clusterId", clusterId);
    }

    public PropertiesBuilder desiredParentId(String desiredParentId) {
        return add("desiredParentId", desiredParentId);
    }

    public PropertiesBuilder minTimeBetweenBidUpdates(int minTimeBetweenBidUpdates) {
        return add("minTimeBetweenBidUpdates", minTimeBetweenBidUpdates);
    }

    public PropertiesBuilder minTimeBetweenPriceUpdates(int minTimeBetweenPriceUpdates) {
        return add("minTimeBetweenPriceUpdates", minTimeBetweenPriceUpdates);
    }

    public PropertiesBuilder marketBasis(MarketBasis marketBasis) {
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
