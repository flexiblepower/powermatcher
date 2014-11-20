package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Price;

/**
 * An {@link PriceEvent} is sent when an {@link AgentRole} receives a new {@link Price}.
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class PriceEvent extends AgentEvent {

    /**
     * The received {@link Price}
     */
    private final Price price;

    /**
     * Constructs an instance of this class.
     * 
     * @param clusterId
	 *            The id of the cluster the {@link AgentRole} subclass sending
	 *            the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentRole} subclass sending the UpdateEvent.
     * @param sessionId
     *            The id of the {@link Session} of the {@link AgentRole} subclass sending the UpdateEvent
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     * @param price
     *            The received {@link Price}.
     */
    public PriceEvent(String clusterId, String agentId, String sessionId, Date timestamp, Price price, Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, qualifier);
        this.price = price;
    }

    public Price getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return PriceEvent.class.getSimpleName() + " " + super.toString() + ", price = "
                + price.toString();
    }
}
