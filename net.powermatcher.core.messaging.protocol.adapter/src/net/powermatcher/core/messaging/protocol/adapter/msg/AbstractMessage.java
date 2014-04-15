package net.powermatcher.core.messaging.protocol.adapter.msg;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.MarketBasisCache;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface AbstractMessage {
	/**
	 * From bytes with the specified marketBasisCache and msg parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #toBytes()
	 */
	public void fromBytes(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException;

	/**
	 * Returns the bytes (byte[]) value.
	 * 
	 * @return The bytes (<code>byte[]</code>) value.
	 * @see #fromBytes(MarketBasisCache,byte[])
	 */
	public byte[] toBytes();

}
