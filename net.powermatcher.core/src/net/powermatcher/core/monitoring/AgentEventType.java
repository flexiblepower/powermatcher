package net.powermatcher.core.monitoring;

import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;

public enum AgentEventType {

    BIDEVENT("BidEvent", BidEvent.class), PRICEEVENT("PriceEvent", PriceUpdateEvent.class);

    private String description;
    private Class<?extends AgentEvent> classType;

    private AgentEventType(String description, Class<?extends AgentEvent> classType) {
        this.description = description;
        this.classType = classType;
    }

    public String getDescription() {
        return description;
    }

    protected  Class<?extends AgentEvent> getClassType() {
        return classType;
    }
}
