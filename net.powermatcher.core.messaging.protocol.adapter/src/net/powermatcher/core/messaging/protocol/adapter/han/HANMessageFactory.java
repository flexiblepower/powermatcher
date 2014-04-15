package net.powermatcher.core.messaging.protocol.adapter.han;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;
import net.powermatcher.core.messaging.protocol.adapter.msg.MessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.msg.PriceInfoMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public class HANMessageFactory implements MessageFactory {
	/**
	 * Gets the instance (MessageFactory) value.
	 * 
	 * @return The instance (<code>MessageFactory</code>) value.
	 */
	public static MessageFactory getInstance() {
		return new HANMessageFactory();
	}

	/**
	 * Define the market basis cache (MarketBasisCache) field.
	 */
	private MarketBasisCache marketBasisCache;

	/**
	 * Constructs an instance of this class.
	 */
	private HANMessageFactory() {
		this.marketBasisCache = new MarketBasisCache(new HANMarketBasisMapper());
	}

	/**
	 * Gets the market basis cache value.
	 * 
	 * @return The market basis cache (<code>MarketBasisCache</code>) value.
	 */
	@Override
	public MarketBasisCache getMarketBasisCache() {
		return this.marketBasisCache;
	}

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
	@Override
	public BidInfo toBidInfo(final byte[] data) throws InvalidObjectException {
		BidMessage bidMessage = new HANBidMessage(this.marketBasisCache, data);
		BidInfo bidInfo = bidMessage.getBidInfo();
		MarketBasis internalMarketBasis = this.marketBasisCache.getInternalMarketBasis(bidInfo.getMarketBasis());
		return bidInfo.toMarketBasis(internalMarketBasis);
	}

	/**
	 * To bid message with the specified bid info parameter and return the
	 * BidMessage result.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @return Results of the to bid message (<code>BidMessage</code>) value.
	 */
	@Override
	public BidMessage toBidMessage(final BidInfo bidInfo) {
		MarketBasis externalMarketBasis = this.marketBasisCache.getExternalMarketBasis(bidInfo.getMarketBasis());
		BidInfo externalBidInfo = bidInfo.toMarketBasis(externalMarketBasis);
		return new HANBidMessage(externalBidInfo);
	}

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
	@Override
	public PriceInfo toPriceInfo(final byte[] data) throws InvalidObjectException {
		PriceInfoMessage priceInfoMessage = new HANPriceInfoMessage(this.marketBasisCache, data);
		PriceInfo priceInfo = priceInfoMessage.getPriceInfo();
		MarketBasis internalMarketBasis = this.marketBasisCache.getInternalMarketBasis(priceInfo.getMarketBasis());
		return priceInfo.toMarketBasis(internalMarketBasis);
	}

	/**
	 * To price info message with the specified price info parameter and return
	 * the PriceInfoMessage result.
	 * 
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @return Results of the to price info message (
	 *         <code>PriceInfoMessage</code>) value.
	 */
	@Override
	public PriceInfoMessage toPriceInfoMessage(final PriceInfo priceInfo) {
		MarketBasis externalMarketBasis = this.marketBasisCache.getExternalMarketBasis(priceInfo.getMarketBasis());
		PriceInfo externalPriceInfo = priceInfo.toMarketBasis(externalMarketBasis);
		HANPriceInfoMessage priceInfoMessage = new HANPriceInfoMessage(externalPriceInfo);
		return priceInfoMessage;
	}

}
