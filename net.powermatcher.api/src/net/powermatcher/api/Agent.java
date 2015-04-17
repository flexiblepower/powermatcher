package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

import org.flexiblepower.context.FlexiblePowerContext;

/**
 * {@link Agent} defines the interface with the basic functionality needed to act as an agent in a Powermatcher cluster.
 *
 * @author FAN
 * @version 2.0
 */
public interface Agent {
    /**
     * The {@link Status} object describes the current connection status of an {@link Agent}. This status can be queried
     * through the {@link Agent#getStatus()} method and will give a snapshot of the state at that time.
     */
    interface Status {
        /**
         * @return the id of the cluster, as received from its {@link Agent} parent.
         * @throws IllegalStateException
         *             when the agent is not connected (see {@link #isConnected()})
         */
        String getClusterId();

        /**
         * @return the current {@link MarketBasis} on which this {@link Agent} is running and basing its {@link Bid}s
         *         and {@link Price}s.
         * @throws IllegalStateException
         *             when the agent is not connected (see {@link #isConnected()})
         */
        MarketBasis getMarketBasis();

        /**
         * For a agent to be connect, the {@link #setContext(FlexiblePowerContext)} has been called and the agent has
         * been configured with a clusterId (and usually a market basis). Only if this is <code>true</code> should the
         * agent be used normally.
         *
         * For an {@link AgentEndpoint} this means that it has been connected through a {@link Session}. For a
         * {@link MatcherEndpoint} is can be connected simply by having a {@link MarketBasis} configured (e.g. for an
         * Auctioneer).
         *
         * @return <code>true</code> when the agent is connected to the cluster.
         */
        boolean isConnected();
    }

    /**
     * @return the mandatory unique id of the {@link Agent}.
     */
    String getAgentId();

    /**
     * @return The current {@link Status} of the {@link Agent}. This returns a snapshot of the state that won't change.
     *         If used at a later time, the snapshot should be refreshed.
     */
    Status getStatus();

    /**
     * Give the agent a reference to a {@link FlexiblePowerContext}. This should be done by the runtime bundle after the
     * component is activated.
     *
     * @param context
     *            reference to the {@link FlexiblePowerContext}
     */
    void setContext(FlexiblePowerContext context);
}
