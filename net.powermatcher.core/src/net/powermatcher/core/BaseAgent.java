package net.powermatcher.core;

import java.util.Date;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.Agent;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of an {@link Agent}. It provides basic functionality required in each {@link Agent}. Implements
 * the {@link ObservableAgent} interface to make sure the instance van send {@link AgentEvent}s to {@link AgentObserver}
 * s.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class BaseAgent
    implements ObservableAgent {
    /**
     * This lock is used for locking different actions on the agent. For example, when changing the state (calling
     * {@link #configure(MarketBasis, String)} or {@link #unconfigure()}). This lock should only be used in the methods
     * that change the state and nowhere else, to prevent deadlocks.
     */
    protected final Object lock = new Object();

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * The id of this Agent.
     */
    private volatile String agentId;

    /**
     * This method should always be called during activation of the agent. It sets the identifier of this agent.
     *
     * @param agentId
     *            The agentId that should be used by this {@link BaseAgent}. This will be returned weth the
     *            {@link #getAgentId()} is called.
     *
     * @throws IllegalArgumentException
     *             when the agentId is null or is an empty string.
     */
    protected void init(String agentId) {
        synchronized (lock) {
            if (this.agentId != null) {
                throw new IllegalStateException("Agent already initialized with an AgentId");
            } else if (agentId == null || agentId.isEmpty()) {
                throw new IllegalArgumentException("The agentId may not be null or empty");
            }
            this.agentId = agentId;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentId() {
        return agentId;
    }

    protected volatile FlexiblePowerContext context;

    /**
     * @see net.powermatcher.api.Agent#setContext(org.flexiblepower.context.FlexiblePowerContext)
     */
    @Override
    public void setContext(FlexiblePowerContext context) {
        synchronized (lock) {
            if (agentId == null) {
                throw new IllegalStateException("The init method should be called first before the context is set.");
            }
            this.context = context;
        }
    }

    /**
     * Returns the current time in a {@link Date} object.
     *
     * @return A {@link Date} object, representing the current date and time
     */
    protected Date now() {
        synchronized (lock) {
            if (context == null) {
                throw new IllegalStateException("The FlexiblePowerContext has not been set, is the PowerMatcher runtime active?");
            }
            return context.currentTime();
        }
    }

    private volatile String clusterId;
    private volatile MarketBasis marketBasis;

    /**
     * Configures the agent to use a specific {@link MarketBasis} and a cluster identifier.
     *
     * @param marketBasis
     *            The {@link MarketBasis} that this agent should use.
     * @param clusterId
     *            The (locally) unique identifier for the cluster this agent is a part of.
     */
    protected void configure(MarketBasis marketBasis, String clusterId) {
        synchronized (lock) {
            if (agentId == null) {
                throw new IllegalStateException("The init method should be called first before the agent is configured.");
            } else if (marketBasis == null) {
                throw new IllegalArgumentException("The MarketBasis can not be null");
            } else if (clusterId == null) {
                throw new IllegalArgumentException("The clusterId can not be null");
            }

            this.clusterId = clusterId;
            this.marketBasis = marketBasis;
        }
    }

    protected void unconfigure() {
        synchronized (lock) {
            clusterId = null;
            marketBasis = null;
        }
    }

    /**
     * @throws IllegalStateException
     *             When this is called before the agent is connected to the cluster.
     */
    @Override
    public String getClusterId() {
        checkConnected();
        return clusterId;
    }

    /**
     * @return The {@link MarketBasis} that this agent is using.
     * @throws IllegalStateException
     *             When this is called before the agent is connected to the cluster.
     */
    public MarketBasis getMarketBasis() {
        checkConnected();
        return marketBasis;
    }

    private void checkConnected() {
        synchronized (lock) {
            if (!isConnected()) {
                StringBuilder sb = new StringBuilder();
                sb.append("The agent [").append(agentId).append("] is not connected to the cluster. ");
                if (context == null) {
                    sb.append("Missing FlexiblePowerContext. ");
                }
                if (clusterId == null) {
                    sb.append("Not connected to the cluster. ");
                }
                throw new IllegalStateException(sb.toString());
            }
        }
    }

    /**
     * Indicates if the agent is ready. This is only true after {@link #init(String)} and
     * {@link #configure(MarketBasis, String)} are called.
     *
     * @return true when the {@link #init(String)} and {@link #configure(MarketBasis, String)} methods have been called
     */
    @Override
    public boolean isConnected() {
        return context != null && clusterId != null;
    }

    /**
     * Collection of {@link Observer} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(AgentObserver observer) {
        observers.remove(observer);
    }

    /**
     * Publish an {@link AgentEvent} to the attached {@link Observer} services.
     *
     * @param event
     *            The event to publish.
     */
    protected final void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            try {
                observer.handleAgentEvent(event);
            } catch (RuntimeException ex) {
                LOGGER.warn("Could not publish an event to observer [{}]: {}", observer, ex.getMessage());
            }
        }
    }
}
