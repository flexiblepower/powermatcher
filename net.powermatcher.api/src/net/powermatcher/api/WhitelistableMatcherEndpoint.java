package net.powermatcher.api;

import java.util.List;

/**
 * {@link WhitelistableMatcherEndpoint} defines the interface with the basic functionality needed to a
 * whitelist in a Concentrator. The Whitelist will be used to decicide if a new {@link AgentEndpoint} is allowed to have
 * a {@link Session} with the {@link MatcherEndpoint} instance.
 * 
 * @author FAN
 * @version 2.0
 */
public interface WhitelistableMatcherEndpoint {

    /**
     * @return the current whitelist of the Concentrator.
     */
    List<String> getWhiteList();

    /**
     * This method is used to inject a new whitelist <code>List</code> into a Concentrator.
     * 
     * @param whiteList
     *            the new whitelist <code>List</code>.
     * @return the Concentrator's updated whiteList.
     */
    List<String> createWhiteList(List<String> whiteList);

    /**
     * This method is used to inject additional whitelist items into a Concentrator.
     * 
     * @param whiteList
     *            the whitelist <code>List</code> that has to be added to the existing whitelist of the Concentrator.
     * @return the Concentrator's updated whiteList.
     */
    List<String> updateWhitelist(List<String> whiteList);

    /**
     * This method is used to remove whitelist <code>List</code> from a Concentrator.
     * 
     * @param whiteList
     *            the whitelist <code>List</code> of items that have to be removed from the Concentrator.
     * @return the Concentrator's updated whiteList.
     */
    List<String> removeWhiteList(List<String> whiteList);
}
