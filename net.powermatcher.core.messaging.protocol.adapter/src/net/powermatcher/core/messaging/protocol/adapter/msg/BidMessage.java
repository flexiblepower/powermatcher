package net.powermatcher.core.messaging.protocol.adapter.msg;


import net.powermatcher.core.agent.framework.data.BidInfo;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface BidMessage extends AbstractMessage {
	/**
	 * Gets the bid info value.
	 * 
	 * @return The bid info (<code>BidInfo</code>) value.
	 */
	public BidInfo getBidInfo();

}
