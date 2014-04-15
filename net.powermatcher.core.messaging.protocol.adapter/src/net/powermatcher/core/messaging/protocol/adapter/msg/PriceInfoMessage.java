package net.powermatcher.core.messaging.protocol.adapter.msg;


import net.powermatcher.core.agent.framework.data.PriceInfo;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface PriceInfoMessage extends AbstractMessage {
	/**
	 * Gets the price info value.
	 * 
	 * @return The price info (<code>PriceInfo</code>) value.
	 */
	public PriceInfo getPriceInfo();

}
