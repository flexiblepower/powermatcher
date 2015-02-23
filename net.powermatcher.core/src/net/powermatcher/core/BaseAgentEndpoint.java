package net.powermatcher.core;

import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;

/**
 * {@link BaseAgentEndpoint} defines the basic functionality of any Device Agent.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class BaseAgentEndpoint
    extends BaseAgent
    implements AgentEndpoint {

    /**
     * The id of the {@link MatcherEndpoint} this Agent wants to connect to.
     */
    private String desiredParentId;

    /**
     * This method should always be called during activation of the agent. It sets the agentId and desiredParentId.
     *
     * @param agentId
     *            The agentId that should be returned when the {@link #getAgentId()} is called.
     * @param desiredParentId
     *            The identifier that should be returned when the {@link #getDesiredParentId()} is called.
     *
     * @throws IllegalArgumentException
     *             when either the agentId or the desiredParentId is null or is an empty string.
     */
    public void activate(String agentId, String desiredParentId) {
        super.activate(agentId);

        if (desiredParentId == null || desiredParentId.isEmpty()) {
            throw new IllegalArgumentException("The desiredParentId may not be null or empty");
        }

        this.desiredParentId = desiredParentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized() {
        return super.isInitialized() && session != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDesiredParentId() {
        return desiredParentId;
    }

    private final AtomicInteger bidNumberGenerator = new AtomicInteger();

    /**
     * The last {@link Bid} received by this BaseDeviceAgent
     */
    private volatile BidUpdate lastBidUpdate;

    /**
     * The current {@link Session} this BaseDeviceAgent is linked in.
     */
    private volatile Session session;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void connectToMatcher(Session session) {
        if (this.session != null) {
            throw new IllegalStateException("Already connected to agent " + session.getMatcherId());
        }

        configure(session.getMarketBasis(), session.getClusterId());
        bidNumberGenerator.set(0);
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void matcherEndpointDisconnected(Session session) {
        this.session = null;
        unconfigure();
        lastBidUpdate = null;
    }

    public void deactivate() {
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * @return the current value of session.
     */
    public final Session getSession() {
        return session;
    }

    /**
     * @return the current value of lastBid.
     */
    public final BidUpdate getLastBidUpdate() {
        return lastBidUpdate;
    }

    /**
     * Publishes a new bid to its matcher by creating a new {@link BidUpdate} using a generated bidnumber.
     *
     * @param newBid
     *            The new bid that is to be sent to the connected matcher
     * @return The {@link BidUpdate} that has been set.
     */
    protected final BidUpdate publishBid(Bid newBid) {
        Session session = getSession();

        if (isInitialized()) {
            BidUpdate update = new BidUpdate(newBid, bidNumberGenerator.incrementAndGet());
            lastBidUpdate = update;
            publishEvent(new OutgoingBidEvent(getClusterId(),
                                              getAgentId(),
                                              session.getSessionId(),
                                              now(),
                                              update));
            LOGGER.debug("Sending bid [{}] to {}", update, session.getAgentId());
            session.updateBid(update);
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

        LOGGER.debug("Received price update [{}]", priceUpdate);
        publishEvent(new IncomingPriceUpdateEvent(getClusterId(),
                                                  getAgentId(),
                                                  session.getSessionId(),
                                                  context.currentTime(),
                                                  priceUpdate));
    }
}
