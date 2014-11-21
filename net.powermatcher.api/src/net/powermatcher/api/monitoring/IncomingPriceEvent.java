package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Price;

/**
 * An {@link IncomingPriceEvent} is sent when an {@link AgentEndpoint} receives a new {@link Price}.
 * 
 * @author FAN
 * @version 1.0
 */
public class IncomingPriceEvent extends PriceEvent {

    /**
     * Constructs an instance of this class.
     * 
     * @param clusterId
	 *            The id of the cluster the {@link AgentEndpoint} subclass sending
	 *            the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     * @param sessionId
     *            The id of the {@link Session} of the {@link AgentEndpoint} subclass sending the UpdateEvent
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     * @param price
     *            The received {@link Price}.
     */
    public IncomingPriceEvent(String clusterId, String agentId, String sessionId, Date timestamp, Price price, Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, price, qualifier);
    }

    @Override
    public String toString() {
        return IncomingPriceEvent.class.getSimpleName() + " " + super.toString() + ", price = "
                + getPrice().toString();
    }
}
