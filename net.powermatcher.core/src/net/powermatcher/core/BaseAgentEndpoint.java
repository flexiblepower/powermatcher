package net.powermatcher.core;

import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;

import org.flexiblepower.context.FlexiblePowerContext;

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
     * This configuration description should be extended by the configuration of the implementing agent and should
     * override the {@link #agentId()} and {@link #desiredParentId()} with their default values and descriptions.
     * Unfortunately the bnd generator does not detect overriden config objects correctly.
     */
    public interface Config
        extends BaseAgent.Config {
        /** @return The agent identifier of the parent matcher to which this agent should be connected */
        String desiredParentId();
    }

    /**
     * The id of the {@link MatcherEndpoint} this Agent wants to connect to.
     */
    private String desiredParentId;

    /**
     * This method should always be called during activation of the agent. It sets the agentId and desiredParentId.
     *
     * @param config
     *            The configuration of this BaseAgent, which provides the agentId and desiredParentId.
     *
     * @throws IllegalArgumentException
     *             when either the agentId or the desiredParentId is null or is an empty string.
     */
    public void activate(Config config) {
        super.activate(config);

        String desiredParentId = config.desiredParentId();
        if (desiredParentId == null || desiredParentId.isEmpty()) {
            throw new IllegalArgumentException("The desiredParentId may not be null or empty");
        }

        this.desiredParentId = desiredParentId;
    }

    @Override
    public void setContext(FlexiblePowerContext context) {
        if (desiredParentId == null) {
            throw new IllegalStateException("The activate method should be called first before the context is set.");
        }
        super.setContext(context);
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
     * Publishes a new bid to its matcher by creating a new {@link BidUpdate} using a generated bidnumber. The call will
     * be ignored if {@link #isConnected()} returns <code>false</code>.
     *
     * @param newBid
     *            The new bid that is to be sent to the connected matcher
     * @return The {@link BidUpdate} that has been set or <code>null</code> if {@link #isConnected()} returns false.
     */
    protected final BidUpdate publishBid(Bid newBid) {
        if (isConnected()) {
            Session session = getSession();

            if (lastBidUpdate != null && newBid.equals(lastBidUpdate.getBid())) {
                // This bid is equal to the previous bid, we should not send an update
                return lastBidUpdate;
            }
            BidUpdate update = new BidUpdate(newBid, bidNumberGenerator.incrementAndGet());
            lastBidUpdate = update;
            publishEvent(new OutgoingBidUpdateEvent(getClusterId(),
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
