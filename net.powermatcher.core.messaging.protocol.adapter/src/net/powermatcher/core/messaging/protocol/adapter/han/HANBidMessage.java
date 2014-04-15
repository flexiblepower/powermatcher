package net.powermatcher.core.messaging.protocol.adapter.han;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;


/**
 * HAN bid message
 * Element		# bytes 		Description
 * version		1, unsigned int	denotes the version of PowerMatcher to which this message adheres. For now 1
 * msgtype		1, enum			the message type; 2 for a bid message
 * marketref	1, unsigned int	reference to a market basis (to a seqnum, see below). This field is retrieved from the UpdatePriceInfo message and should reflect the latest market ref retrieved.
 * demandunit	2, unsigned int	denotes the step size of demand in Watt. For pilot 1, i.e. our UoM is 1 W. Big endian.
 * numpoints	1, unsigned int	the number of points of the bid curve, 2 for Refrigerator
 * price[0]		1, signed int	the price in normalized price units at which point the Refrigerator will turn off / on
 * demand[0]	2, signed int	denotes the demand in demand units that the Refrigerator uses when on.  Big endian.
 * price[1]		1, signed int	denotes the price in normalized price units at which point the Refrigerator will turn off / on (=price[0])
 * demand[1]	2, signed int	denotes the demand in demand units when the Refrigerator is off, i.e. 0.  Big endian.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class HANBidMessage extends AbstractHANMessage implements BidMessage {
	/**
	 * Define the message size (int) constant.
	 */
	protected static final int MESSAGE_SIZE = HEADER_SIZE + 4; // Base size for
																// numpoints = 0
	/**
	 * Define the marketref offset (int) constant.
	 */
	protected static final int MARKETREF_OFFSET = 2;
	/**
	 * Define the demandunit offset (int) constant.
	 */
	protected static final int DEMANDUNIT_OFFSET = 3;
	/**
	 * Define the numpoints offset (int) constant.
	 */
	protected static final int NUMPOINTS_OFFSET = 5;
	/**
	 * Define the points offset (int) constant.
	 */
	protected static final int POINTS_OFFSET = 6;
	/**
	 * Define the bid info (BidInfo) field.
	 */
	private BidInfo bidInfo;
	/**
	 * Define the calculated price points (PricePoint[]) field.
	 */
	private PricePoint[] calculatedPricePoints;

	/**
	 * Constructs an instance of this class from the specified bid info
	 * parameter.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @see #HANBidMessage(MarketBasisCache,byte[])
	 */
	public HANBidMessage(final BidInfo bidInfo) {
		super(MessageType.BID);
		this.bidInfo = bidInfo;
		this.calculatedPricePoints = bidInfo.getCalculatedPricePoints();
	}

	/**
	 * Constructs an instance of this class.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 * @see #HANBidMessage(BidInfo)
	 */
	public HANBidMessage(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
		super(MessageType.BID);
		fromBytes(marketBasisCache, msg);
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		strb.append("bidInfo=");
		strb.append(this.bidInfo);
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
		int marketRef = msg[MARKETREF_OFFSET] & 0xFF;
		int demandUnit = (msg[DEMANDUNIT_OFFSET] << 8) | (msg[DEMANDUNIT_OFFSET + 1] & 0xFF);
		int numPoints = msg[NUMPOINTS_OFFSET] & 0xFF;
		MarketBasis externalMarketBasis = marketBasisCache.getExternalMarketBasis(marketRef);
		if (externalMarketBasis == null) {
			throw new InvalidObjectException("Message contains unknown market basis reference");
		}
		this.calculatedPricePoints = new PricePoint[numPoints];
		for (int i = 0; i < numPoints; i++) {
			int offset = POINTS_OFFSET + i * 3;
			double demand = demandUnit * (msg[offset + 1] << 8) | (msg[offset + 2] & 0xFF);
			calculatedPricePoints[i] = new PricePoint(msg[offset], demand);
		}
		this.bidInfo = new BidInfo(externalMarketBasis, calculatedPricePoints);
	}

	/**
	 * Gets the bid info value.
	 * 
	 * @return The bid info (<code>BidInfo</code>) value.
	 */
	@Override
	public BidInfo getBidInfo() {
		return this.bidInfo;
	}

	/**
	 * Get the number of price points in the bid.
	 * @return The number of price points in the bid.
	 */
	private int getNumberOfPricePoints() {
		return calculatedPricePoints == null ? 0 : calculatedPricePoints.length;
	}

	/**
	 * Gets the size (int) value.
	 * 
	 * @return The size (<code>int</code>) value.
	 */
	@Override
	protected int getSize() {
		if (this.bidInfo == null) {
			return MESSAGE_SIZE;
		} else {
			int numPoints = getNumberOfPricePoints();
			return MESSAGE_SIZE + numPoints * 3;
		}
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
		msg[MARKETREF_OFFSET] = (byte) this.bidInfo.getMarketBasis().getMarketRef();
		double scaleFactor = this.bidInfo.getScaleFactor(Short.MAX_VALUE);
		int demandUnit = (int) Math.ceil(scaleFactor);
		msg[DEMANDUNIT_OFFSET] = (byte) (demandUnit >> 8);
		msg[DEMANDUNIT_OFFSET + 1] = (byte) demandUnit;
		int numPoints = getNumberOfPricePoints();
		msg[NUMPOINTS_OFFSET] = (byte) numPoints;
		for (int i = 0; i < numPoints; i++) {
			int offset = POINTS_OFFSET + i * 3;
			int normalizedPrice = this.calculatedPricePoints[i].getNormalizedPrice();
			msg[offset] = (byte) normalizedPrice;
			int demand = Math.round((float) this.calculatedPricePoints[i].getDemand() / demandUnit);
			msg[offset + 1] = (byte) (demand >> 8);
			msg[offset + 2] = (byte) demand;
		}
		return msg;
	}

}
