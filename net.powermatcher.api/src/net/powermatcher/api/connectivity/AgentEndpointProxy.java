package net.powermatcher.api.connectivity;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;

/**
 * {@link AgentEndpointProxy} defines the interface and contains all basic
 * methods needed to enable remote communication between an
 * {@link AgentEndpoint} and a remote {@link MatcherEndpoint} through a
 * {@link MatcherEndpointProxy}. The means of communication is up to the
 * implementing class.
 * 
 * @author FAN
 * @version 2.0
 */
public interface AgentEndpointProxy extends AgentEndpoint {

	/**
	 * @return <code>true</code> if the local {@link Session} is not
	 *         <code>null</code>.
	 */
	boolean isLocalConnected();

	/**
	 * @return <code>true</code> if {@link Session} and the connection to the
	 *         remote {@link MatcherEndpointProxy} is active.
	 */
	boolean isRemoteConnected();

	/**
	 * Sends the {@link Bid} it receives from the remote {@link MatcherEndpoint}
	 * through the {@link MatcherEndpointProxy} to the local
	 * {@link MatcherEndpoint} through the local {@link Session}.
	 * 
	 * @param priceUpdate
	 *            the new {@link PriceUpdate}
	 */
	void updateLocalBid(Bid newBid);

	/**
	 * Sends the {@link PriceUpdate} it receives through from the local
	 * {@link MatcherEndpoint} to the remote {@link MatcherEndpoint} through he
	 * {@link MatcherEndpointProxy}.
	 * 
	 * @param priceUpdate
	 *            the new {@link PriceUpdate}
	 */
	void updateRemotePrice(PriceUpdate priceUpdate);
}
