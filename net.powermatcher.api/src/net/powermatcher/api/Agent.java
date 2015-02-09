package net.powermatcher.api;

import org.flexiblepower.context.FlexiblePowerContext;

/**
 * {@link Agent} defines the interface with the basic functionality needed to act as an agent in a Powermatcher cluster.
 *
 * @author FAN
 * @version 2.0
 */
public interface Agent {

    /**
     * @return the mandatory unique id of the {@link Agent}.
     */
    String getAgentId();

    /**
     * @return the id of the cluster, as received from its {@link Agent} parent. <code>null</code> when the cluster is
     *         unknown.
     */
    String getClusterId();

    /**
     * @return the id of the desired parent {@link Agent}. <code>null</code> when no parent is required (Auctioneer).
     */
    String getDesiredParentId();

    /**
     * Give the agent a reference to a {@link FlexiblePowerContext}. This should be done by the runtime bundle after the
     * component is activated.
     *
     * @param context
     *            reference to the {@link FlexiblePowerContext}
     */
    void setContext(FlexiblePowerContext context);
}
