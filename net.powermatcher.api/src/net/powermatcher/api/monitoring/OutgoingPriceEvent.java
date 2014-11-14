package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Price;

/**
 * An {@link OutgoingPriceEvent} is sent when an {@link AgentRole} sends a new {@link Price}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class OutgoingPriceEvent extends PriceEvent {

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
     *            The new {@link Price} created by the {@link AgentRole} subclass.
     */
    public OutgoingPriceEvent(String clusterId, String agentId, String sessionId, Date timestamp, Price price) {
        super(clusterId, agentId, sessionId, timestamp, price);
    }

    @Override
    public String toString() {
        return OutgoingPriceEvent.class.getSimpleName() + " " + super.toString() + ", price = "
                + getPrice().toString();
    }
}
