package net.powermatcher.core.messaging.protocol.adapter.han;


import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.messaging.protocol.adapter.msg.PriceInfoMessage;


/**
 * HAN price info message
 * Element		# bytes 		Description
 * version		1, unsigned int	denotes the version of PowerMatcher to which this message adheres. For now 1
 * msgtype		1, enum			the message type; 1 for a price message
 * currentprice	1, signed int	denotes the equilibrium price in normalized price units
 * marketref	1, unsigned int	wrapping sequence number for this market basis
 * currency		3, chars		ISO-4217 standard representation  , for now "EUR" (simplification: HAN rev. 6 actually packs 3 chars in 2 bytes)
 * exchangerate	2, unsigned int	npu * exchangerate / 1000 is the price in currency
 * commodity	1, enum			enumeration maintained by PowerMatcher consortium (1 = Electricity)
 * 
 * @author IBM
 * @version 0.9.0
 */
public class HANPriceInfoMessage extends AbstractHANMessage implements PriceInfoMessage {
	/**
	 * Define the message size (int) constant.
	 */
	protected static final int MESSAGE_SIZE = HEADER_SIZE + 8;
	/**
	 * Define the currentprice offset (int) constant.
	 */
	protected static final int CURRENTPRICE_OFFSET = 2;
	/**
	 * Define the marketref offset (int) constant.
	 */
	protected static final int MARKETREF_OFFSET = 3;
	/**
	 * Define the currency offset (int) constant.
	 */
	protected static final int CURRENCY_OFFSET = 4;
	/**
	 * Define the exchangerate offset (int) constant.
	 */
	protected static final int EXCHANGERATE_OFFSET = 7;
	/**
	 * Define the commodity offset (int) constant.
	 */
	protected static final int COMMODITY_OFFSET = 9;
	/**
	 * Define the price info (PriceInfo) field.
	 */
	private PriceInfo priceInfo;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #HANPriceInfoMessage(PriceInfo)
	 */
	public HANPriceInfoMessage(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
		super(MessageType.PRICE);
		fromBytes(marketBasisCache, msg);
	}

	/**
	 * Constructs an instance of this class from the specified price info
	 * parameter.
	 * 
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @see #HANPriceInfoMessage(MarketBasisCache,byte[])
	 */
	public HANPriceInfoMessage(final PriceInfo priceInfo) {
		super(MessageType.PRICE);
		this.priceInfo = priceInfo;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		strb.append("priceInfo=");
		strb.append(this.priceInfo);
	}

	/**
	 * From bytes with the specified msg parameter.
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
	@Override
	public void fromBytes(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
		super.fromBytes(msg);
		int normalizedPrice = msg[CURRENTPRICE_OFFSET];
		int marketRef = msg[MARKETREF_OFFSET] & 0xFF;
		String currency = null;
		try {
			currency = new String(msg, CURRENCY_OFFSET, 3, "ISO-8859-1");
		} catch (final UnsupportedEncodingException e) {
			/* ignore exception */
		}
		int exchangeRate = ((msg[EXCHANGERATE_OFFSET] & 0xFF) << 8) | (msg[EXCHANGERATE_OFFSET + 1] & 0xFF);
		String commodity = Commodity.values()[msg[COMMODITY_OFFSET]].toString();
		double maximumPrice = MAXIMUM_NORMALIZED_PRICE * exchangeRate / 1000.0d;
		double currentPrice = (normalizedPrice * exchangeRate) / 1000.0d;
		MarketBasis marketBasis = new MarketBasis(commodity, currency, PRICE_STEPS, -maximumPrice, maximumPrice, 0, marketRef);
		this.priceInfo = new PriceInfo(marketBasis, currentPrice);
	}

	/**
	 * Gets the price info value.
	 * 
	 * @return The price info (<code>PriceInfo</code>) value.
	 */
	@Override
	public PriceInfo getPriceInfo() {
		return this.priceInfo;
	}

	/**
	 * Gets the size (int) value.
	 * 
	 * @return The size (<code>int</code>) value.
	 */
	@Override
	protected int getSize() {
		return MESSAGE_SIZE;
	}

	/**
	 * Returns the bytes (byte[]) value.
	 * 
	 * @return The bytes (<code>byte[]</code>) value.
	 * @see #fromBytes(MarketBasisCache,byte[])
	 */
	@Override
	public byte[] toBytes() {
		byte[] msg = super.toBytes();
		MarketBasis marketBasis = this.priceInfo.getMarketBasis();
		msg[CURRENTPRICE_OFFSET] = (byte) this.priceInfo.getNormalizedPrice();
		msg[MARKETREF_OFFSET] = (byte) marketBasis.getMarketRef();
		try {
			System.arraycopy(marketBasis.getCurrency().getBytes("ISO-8859-1"), 0, msg, CURRENCY_OFFSET, 3);
		} catch (final UnsupportedEncodingException e) {
			/* ignore exception */
		}
		int exchangeRate = Math.round((float) (marketBasis.getPriceIncrement() * 1000.0d));
		msg[EXCHANGERATE_OFFSET] = (byte) (exchangeRate >> 8);
		msg[EXCHANGERATE_OFFSET + 1] = (byte) exchangeRate;
		msg[COMMODITY_OFFSET] = (byte) Commodity.normalizedValueOf(marketBasis.getCommodity()).ordinal();
		return msg;
	}

}
