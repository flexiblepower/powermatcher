package net.powermatcher.api;

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
     * @return the service Pid of the managed service of this instance in the OSGi container.
     */
    String getServicePid();
}
