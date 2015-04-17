package net.powermatcher.monitoring.csv;

import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidUpdateEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;

/**
 * This <code>enum</code> contains all existing {@link AgentEvent} implementations. This enum is used by
 * {@link AgentEventLogger} to be able to select an {@link AgentEventType} in the Apache Felix config admin.
 *
 * @author FAN
 * @version 2.0
 */
public enum AgentEventType {

    BID_EVENT("BidEvent", BidUpdateEvent.class),
    PRICE_EVENT("PriceEvent", PriceUpdateEvent.class);

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
        className = description;
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
