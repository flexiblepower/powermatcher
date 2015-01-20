package net.powermatcher.api;

import java.util.List;

/**
 * {@link WhitelistableMatcherEndpoint} defines the interface with the basic
 * functionality needed for a whitelist in a Concentrator. The Whitelist will be
 * used to decide if a new {@link AgentEndpoint} is allowed to have a
 * {@link Session} with the {@link MatcherEndpoint} instance.
 * 
 * @author FAN
 * @version 2.0
 */
public interface WhitelistableMatcherEndpoint {

	/**
	 * @return the current whitelist with agents of the Concentrator.
	 */
	List<String> getWhiteList();

	/**
	 * This method is used to Set a whiteList <code>List</code> for a
	 * Concentrator. If setting a null whiteList, the whiteList will be cleared
	 * and all agents are accepted for the Concentrator.
	 * 
	 * @param whiteList
	 *            the new whitelist <code>List</code>.
	 * @return the Concentrator's updated whiteList.
	 */
	void setWhiteList(List<String> whiteList);
}
