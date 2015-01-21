package net.powermatcher.core.connectivity;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for remote agents. This is the "sending end" of a remote communication pair.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class BaseMatcherEndpointProxy extends BaseAgent implements MatcherEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMatcherEndpointProxy.class);

    /**
     * The {@link Session} to the local {@link AgentEndpoint}.
     */
    private Session localSession;

    /**
     * Scheduler that can schedule commands to run after a given delay, or to execute periodically.
     */
    private ScheduledExecutorService scheduler;

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * This method will be called by the annotated Activate() method of the subclasses.
     * 
     * @param reconnectTimeout
     *            Time in seconds between connections to the {@link AgentEndpointProxy}.
     */
    protected void baseActivate(int reconnectTimeout) {
        // Start connector thread
        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                connectRemote();
            }
        }, 0, reconnectTimeout, TimeUnit.SECONDS);
    }

    /**
     * This method will be called by the annotated Deactivate() method of the subclasses.
     */
    protected void baseDeactivate() {
        // Stop connector thread
        this.scheduledFuture.cancel(false);

        // Disconnect the agent
        this.disconnectRemote();
    }

    /**
     * @param the
     *            new value of scheduler
     */
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Creates a {@link Session} and a connection to the remote {@link AgentEndpointProxy}.
     * 
     * @return <code>true</code> if the connection was created successfully.
     */
    public abstract boolean connectRemote();

    /**
     * @return <code>true</code> if {@link Session} and the connection to the remote {@link AgentEndpointProxy} is
     *         active.
     */
    public abstract boolean isRemoteConnected();

    /**
     * Closes the {@link Session} and the connection to the remote {@link AgentEndpointProxy}.
     * 
     * @return <code>true</code> if the disconnecting was successful.
     */
    public abstract boolean disconnectRemote();

    /**
     * @return <code>true</code> isf the local {@link Session} is not <code>null</code>.
     */
    public boolean isLocalConnected() {
        return this.localSession != null;
    }

    /**
     * This method sets the {@link MarketBasis} of the local {@link Session}. It is called when the
     * {@link AgentEndpointProxy} communicates with this instance.
     * 
     * @param marketBasis
     *            the new {@link MarketBasis}
     */
    public void updateRemoteMarketBasis(MarketBasis marketBasis) {
        // Sync marketbasis with local session, for new connections
        if (this.isLocalConnected() && this.localSession.getMarketBasis() == null) {
            this.localSession.setMarketBasis(marketBasis);
        }
    }

    /**
     * This method sets the clusterId of the local {@link Session}. It is called when the {@link AgentEndpointProxy}
     * communicates with this instance.
     * 
     * @param clusterId
     *            the id of the new cluster.
     */
    public void updateRemoteClusterId(String clusterId) {
        // Sync clusterid with local session, for new connections
        if (this.isLocalConnected() && this.localSession.getMarketBasis() == null) {
            this.localSession.setClusterId(clusterId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connectToAgent(Session session) {
        this.localSession = session;
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());

        // Initiate a remote connection
        connectRemote();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentEndpointDisconnected(Session session) {
        // Disconnect local agent
        this.localSession = null;
        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());

        // Disconnect remote agent
        this.disconnectRemote();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBid(Session session, Bid newBid) {
        if (this.localSession != session) {
            LOGGER.warn("Received bid update for unknown session.");
            return;
        }

        if (!isRemoteConnected()) {
            LOGGER.warn("Received bid update, but remote agent is not connected.");
            return;
        }

        if (this.localSession.getMarketBasis() == null) {
            LOGGER.info("Skip bid update to local agent, no marketbasis available.");
            return;
        }

        // Relay bid to remote agent
        this.updateBidRemote(newBid);
    }

    /**
     * Sends the {@link Bid} it receives through from the local {@link AgentEndpoint} to the remote
     * {@link AgentEndpoint} through he {@link AgentEndpointProxy}.
     * 
     * @param newBid
     *            the new {@link Bid} sent by the {@link AgentEndpointProxy}.
     */
    protected abstract void updateBidRemote(Bid newBid);

    /**
     * Sends the {@link PriceUpdate} it receives from the remote {@link AgentEndpoint} through the {@link AgentEndpoint}
     * to the local {@link AgentEndpoint} through the local {@link Session}.
     * 
     * @param priceUpdate
     *            the new {@link PriceUpdate}
     */
    public void updateLocalPrice(PriceUpdate priceUpdate) {
        if (!this.isLocalConnected()) {
            LOGGER.info("Skip price update to local agent, not connected.");
            return;
        }

        if (this.localSession.getMarketBasis() == null) {
            LOGGER.info("Skip price update to local agent, no marketbasis available.");
            return;
        }

        this.localSession.updatePrice(priceUpdate);
    }

    public boolean canEqual(Object other) {
        return other instanceof BaseMatcherEndpointProxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        BaseMatcherEndpointProxy that = (BaseMatcherEndpointProxy) ((obj instanceof BaseMatcherEndpointProxy) ? obj
                : null);
        if (that == null) {
            return false;
        }

        if (this == that) {
            return true;
        }

        return canEqual(that) && this.localSession.equals(that.localSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 211 * (this.localSession.hashCode());
    }
}
