package net.powermatcher.monitoring.csv;

import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;
import net.powermatcher.api.monitoring.events.PeakShavingEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;
import net.powermatcher.api.monitoring.events.WhitelistEvent;

/**
 * This <code>enum</code> contains all existing {@link AgentEvent} implementations. This enum is used by
 * {@link AgentEventLogger} to be able to select an {@link AgentEventType} in the Apache Felix config admin.
 * 
 * @author FAN
 * @version 2.0
 */
public enum AgentEventType {

    BID_EVENT("BidEvent", BidEvent.class), PRICE_EVENT("PriceEvent", PriceUpdateEvent.class), WHITELIST_EVENT(
            "WhiteListEvent", WhitelistEvent.class), PEAK_SHAVING_EVENT("PeakShavingEvent", PeakShavingEvent.class);

    /**
     * The class name of the {@link AgentEvent} implementations.
     */
    private String className;

    /**
     * The runtime class of the {@link AgentEvent} implementations.
     */
    private Class<? extends AgentEvent> classType;

    /**
     * A private constructor to create an instance of this enum.
     * 
     * @param description
     *            the class name of the {@link AgentEvent} implementations.
     * @param classType
     *            the runtime class of the {@link AgentEvent} implementations.
     */
    private AgentEventType(String description, Class<? extends AgentEvent> classType) {
        this.className = description;
        this.classType = classType;
    }

    /**
     * @return the current value of className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the current value of classType.
     */
    protected Class<? extends AgentEvent> getClassType() {
        return classType;
    }
}
