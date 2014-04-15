package net.powermatcher.core.messaging.protocol.adapter.msg;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PriceInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MessageFactory {
	/**
	 * Gets the market basis cache value.
	 * 
	 * @return The market basis cache (<code>MarketBasisCache</code>) value.
	 */
	public MarketBasisCache getMarketBasisCache();

	/**
	 * To bid info with the specified data parameter and return the BidInfo
	 * result.
	 * 
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return Results of the to bid info (<code>BidInfo</code>) value.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 */
	public BidInfo toBidInfo(final byte[] data) throws InvalidObjectException;

	/**
	 * To bid message with the specified bid info parameter and return the
	 * BidMessage result.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @return Results of the to bid message (<code>BidMessage</code>) value.
	 */
	public BidMessage toBidMessage(final BidInfo bidInfo);

	/**
	 * To price info with the specified data parameter and return the PriceInfo
	 * result.
	 * 
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return Results of the to price info (<code>PriceInfo</code>) value.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 */
	public PriceInfo toPriceInfo(final byte[] data) throws InvalidObjectException;

	/**
	 * To price info message with the specified price info parameter and return
	 * the PriceInfoMessage result.
	 * 
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @return Results of the to price info message (
	 *         <code>PriceInfoMessage</code>) value.
	 */
	public PriceInfoMessage toPriceInfoMessage(final PriceInfo priceInfo);

}
