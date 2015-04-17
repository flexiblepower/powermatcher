package net.powermatcher.core;

import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;

/**
 * {@link BaseAgentEndpoint} defines the basic functionality of any Device Agent.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class BaseAgentEndpoint
    extends BaseAgent
    implements AgentEndpoint {

    public static final AgentEndpoint.Status NOT_CONNECTED = new AgentEndpoint.Status() {
        @Override
        public String getClusterId() {
            throw new IllegalStateException("Agent not connected to a cluster");
        }

        @Override
        public MarketBasis getMarketBasis() {
            throw new IllegalStateException("Agent not connected to a cluster");
        }

        @Override
        public Session getSession() {
            throw new IllegalStateException("Agent not connected to a cluster");
        }

        @Override
        public boolean isConnected() {
            return false;
        }
    };

    /**
     * The {@link Connected} object describes the current status and configuration of an {@link AgentEndpoint}. This
     * status can be queried through the {@link Agent#getStatus()} method and will give a snapshot of the state at that
     * time.
     */
    public static class Connected
        implements AgentEndpoint.Status {

        private final Session session;

        /**
         * Creates a new {@link Connected} object.
         *
         * @param session
         *            the current {@link Session} of the {@link AgentEndpoint}.
         */
        public Connected(Session session) {
            if (session == null) {
                throw new NullPointerException();
            }
            this.session = session;
        }

        @Override
        public String getClusterId() {
            return session.getClusterId();
        }

        @Override
        public MarketBasis getMarketBasis() {
            return session.getMarketBasis();
        }

        @Override
        public Session getSession() {
            return session;
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }

    private final AtomicInteger bidNumberGenerator;

    private volatile AgentEndpoint.Status status;

    private volatile BidUpdate lastBidUpdate;

    private String agentId, desiredParentId;

    public BaseAgentEndpoint() {
        bidNumberGenerator = new AtomicInteger();
        status = NOT_CONNECTED;
        lastBidUpdate = null;
        agentId = null;
        desiredParentId = null;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getDesiredParentId() {
        return desiredParentId;
    }

    @Override
    public AgentEndpoint.Status getStatus() {
        return status;
    }

    /**
     * This method should always be called during activation of the agent. It sets the agentId and desiredParentId.
     *
     * @param agentId
     *            The agentId that should be used by this {@link BaseAgent}. This will be returned when the
     *            {@link #getAgentId()} is called.
     * @param desiredParentId
     *            The agentId that should be used by this {@link BaseAgentEndpoint} when determining the desired parent.
     *            This will be returned when the {@link #getDesiredParentId()} is called.
     *
     * @throws IllegalArgumentException
     *             when either the agentId or the desiredParentId is null or is an empty string.
     */
    protected void init(String agentId, String desiredParentId) {
        if (agentId == null || agentId.isEmpty()) {
            throw new IllegalArgumentException("The agentId may not be null or empty");
        }
        if (desiredParentId == null || desiredParentId.isEmpty()) {
            throw new IllegalArgumentException("The desiredParentId may not be null or empty");
        }
        this.agentId = agentId;
        this.desiredParentId = desiredParentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToMatcher(Session session) {
        if (status.isConnected()) {
            throw new IllegalStateException("Already connected to agent " + session.getMatcherId());
        }

        bidNumberGenerator.set(0);
        lastBidUpdate = null;
        status = new Connected(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void matcherEndpointDisconnected(Session session) {
        status = NOT_CONNECTED;
    }

    public void deactivate() {
        AgentEndpoint.Status currentStatus = getStatus();
        if (currentStatus.isConnected()) {
            currentStatus.getSession().disconnect();
        }
    }

    /**
     * @return the current value of lastBid.
     */
    public final BidUpdate getLastBidUpdate() {
        return lastBidUpdate;
    }

    /**
     * Publishes a new bid to its matcher by creating a new {@link BidUpdate} using a generated bidnumber. The call will
     * be ignored if the Agent is not connected.
     *
     * @param newBid
     *            The new bid that is to be sent to the connected matcher
     * @return The {@link BidUpdate} that has been set or <code>null</code> if not connected.
     */
    protected final BidUpdate publishBid(Bid newBid) {
        AgentEndpoint.Status currentStatus = getStatus();
        if (currentStatus.isConnected()) {
            if (lastBidUpdate != null && newBid.equals(lastBidUpdate.getBid())) {
                // This bid is equal to the previous bid, we should not send an update
                return lastBidUpdate;
            }
            BidUpdate update = new BidUpdate(newBid, bidNumberGenerator.incrementAndGet());
            lastBidUpdate = update;
            publishEvent(new OutgoingBidUpdateEvent(status.getClusterId(),
                                                    getAgentId(),
                                                    status.getSession().getSessionId(),
                                                    now(),
                                                    update));
            LOGGER.debug("Sending bid [{}] to {}", update, status.getSession().getMatcherId());
            status.getSession().updateBid(update);
            return update;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * This base implementation checks if the {@link PriceUpdate} is valid and publishes the
     * {@link IncomingPriceUpdateEvent}.
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {

        if (priceUpdate == null) {
            String message = "Price cannot be null";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }

        AgentEndpoint.Status currentStatus = getStatus();
        if (currentStatus.isConnected()) {
            LOGGER.debug("Received price update [{}]", priceUpdate);
            publishEvent(new IncomingPriceUpdateEvent(status.getClusterId(),
                                                      getAgentId(),
                                                      status.getSession().getSessionId(),
                                                      context.currentTime(),
                                                      priceUpdate));
        }
    }
}
