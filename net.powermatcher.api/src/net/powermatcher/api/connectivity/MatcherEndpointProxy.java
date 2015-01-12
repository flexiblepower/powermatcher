package net.powermatcher.api.connectivity;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

/**
 * {@link MatcherEndpointProxy} defines the interface and contains all basic methods needed to enable remote
 * communication between a {@link MatcherEndpointProxy} and a remote {@link AgentEndpoint} through a
 * {@link AgentEndpointProxy}. The means of communication is up to the implementing class.
 * 
 * @author FAN
 * @version 2.0
 */
public interface MatcherEndpointProxy extends MatcherEndpoint {

    /**
     * Creates a {@link Session} and a connection to the remote {@link AgentEndpointProxy}.
     * 
     * @return <code>true</code> if the connection was created successfully.
     */
    boolean connectRemote();

    /**
     * Closes the {@link Session} and the connection to the remote {@link AgentEndpointProxy}.
     * 
     * @return <code>true</code> if the disconnecting was successful.
     */
    boolean disconnectRemote();

    /**
     * @return <code>true</code> isf the local {@link Session} is not <code>null</code>.
     */
    boolean isLocalConnected();

    /**
     * @return <code>true</code> if {@link Session} and the connection to the remote {@link AgentEndpointProxy} is
     *         active.
     */
    boolean isRemoteConnected();

    /**
     * Sends the {@link Bid} it receives through from the local {@link AgentEndpoint} to the remote
     * {@link AgentEndpoint} through he {@link AgentEndpointProxy}.
     * 
     * @param newBid
     *            the new {@link Bid} sent by the {@link AgentEndpointProxy}.
     */
    void updateBidRemote(Bid newBid);

    /**
     * Sends the {@link PriceUpdate} it receives from the remote {@link AgentEndpoint} through the {@link AgentEndpoint}
     * to the local {@link AgentEndpoint} through the local {@link Session}.
     * 
     * @param priceUpdate
     *            the new {@link PriceUpdate}
     */
    void updateLocalPrice(PriceUpdate priceUpdate);

    /**
     * This method sets the {@link MarketBasis} of the local {@link Session}. It is called when the
     * {@link AgentEndpointProxy} communicates with this instance.
     * 
     * @param marketBasis
     *            the new {@link MarketBasis}
     */
    void updateRemoteMarketBasis(MarketBasis marketBasis);

    /**
     * This method sets the clusterId of the local {@link Session}. It is called when the {@link AgentEndpointProxy}
     * communicates with this instance.
     * 
     * @param clusterId
     *            the id of the new cluster.
     */
    void updateRemoteClusterId(String clusterId);
}
