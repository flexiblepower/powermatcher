package net.powermatcher.api;

/**
 * A {@link Agent} defines the interface for classes which implement an Agent. It defines which identifications are
 * required for an Agent to provide to a cluster.
 * 
 * @author FAN
 * @version 2.0
 * 
 */
public interface Agent {

    /**
     * @return the mandatory unique identification of the {@link Agent}.
     */
    String getAgentId();

    /**
     * @return the identification of the cluster, as received from parent {@link Agent}. null when cluster is unknown.
     */
    String getClusterId();

    /**
     * @return the desired identification of parent {@link Agent}. null when no parent is required (Auctioneer).
     */
    String getDesiredParentId();

    String getServicePid();
}
