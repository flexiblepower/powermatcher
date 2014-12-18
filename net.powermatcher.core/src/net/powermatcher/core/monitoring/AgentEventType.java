package net.powermatcher.core.monitoring;

import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;
import net.powermatcher.api.monitoring.events.PeakShavingEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;
import net.powermatcher.api.monitoring.events.WhitelistEvent;

public enum AgentEventType {

    BID_EVENT("BidEvent", BidEvent.class), PRICE_EVENT("PriceEvent", PriceUpdateEvent.class), WHITELIST_EVENT(
            "WhiteListEvent", WhitelistEvent.class), PEAK_SHAVING_EVENT("PeakShavingEvent", PeakShavingEvent.class);

    private String description;
    private Class<? extends AgentEvent> classType;

    private AgentEventType(String description, Class<? extends AgentEvent> classType) {
        this.description = description;
        this.classType = classType;
    }

    public String getDescription() {
        return description;
    }

    protected Class<? extends AgentEvent> getClassType() {
        return classType;
    }
}
