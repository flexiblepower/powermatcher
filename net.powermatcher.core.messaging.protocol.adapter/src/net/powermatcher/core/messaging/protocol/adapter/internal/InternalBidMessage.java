package net.powermatcher.core.messaging.protocol.adapter.internal;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public class InternalBidMessage extends AbstractInternalMessage implements BidMessage {
	/**
	 *
	 */
	public enum BidInfoEncoding {
		/**
		 * Ordinal 0
		 */
		NO_BIDINFO,
		/**
		 * Ordinal 1
		 */
		PRICE_POINTS,
		/**
		 * Ordinal 2
		 */
		DEMAND_ARRAY
	}

	/**
	 * Define the bid info (BidInfo) field.
	 */
	private BidInfo bidInfo;

	/**
	 * Constructs an instance of this class from the specified bid info
	 * parameter.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @see #InternalBidMessage(MarketBasisCache,byte[])
	 */
	public InternalBidMessage(final BidInfo bidInfo) {
		super(MessageType.BID);
		this.bidInfo = bidInfo;
	}

	/**
	 * Constructs an instance of this class from the specified market basis
	 * cache and msg parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #InternalBidMessage(BidInfo)
	 */
	public InternalBidMessage(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
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
	 * From data input with the specified market basis cache and data input
	 * parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param dataInput
	 *            The data input (<code>DataInput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void fromDataInput(final MarketBasisCache marketBasisCache, final DataInput dataInput) throws IOException {
		BidInfo bidInfo;
		int marketRef = dataInput.readByte() & 0xFF;
		MarketBasis externalMarketBasis = marketBasisCache.getExternalMarketBasis(marketRef);
		if (externalMarketBasis == null) {
			throw new InvalidObjectException("Message contains unknown market basis reference");
		}
		int bidNumber = dataInput.readInt();
		BidInfoEncoding encodingType = BidInfoEncoding.values()[dataInput.readShort()];
		if (encodingType == BidInfoEncoding.PRICE_POINTS) {
			int numPoints = dataInput.readShort();
			PricePoint pricePoints[] = new PricePoint[numPoints];
			for (int i = 0; i < pricePoints.length; i++) {
				int normalizedPrice = dataInput.readShort();
				pricePoints[i] = new PricePoint(normalizedPrice, dataInput.readFloat());
			}
			bidInfo = new BidInfo(externalMarketBasis, pricePoints);
		} else if (encodingType == BidInfoEncoding.DEMAND_ARRAY) {
			int numSteps = dataInput.readShort();
			double demand[] = new double[numSteps];
			for (int i = 0; i < demand.length; i++) {
				demand[i] = dataInput.readFloat();
			}
			bidInfo = new BidInfo(externalMarketBasis, demand);
		} else {
			bidInfo = new BidInfo(externalMarketBasis);
		}
		this.bidInfo = new BidInfo(bidInfo, bidNumber);
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
	 * To data output with the specified data output parameter.
	 * 
	 * @param dataOutput
	 *            The data output (<code>DataOutput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void toDataOutput(final DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(this.bidInfo.getMarketBasis().getMarketRef());
		dataOutput.writeInt((this.bidInfo.getBidNumber()));
		if (this.bidInfo.getPricePoints() != null) {
			dataOutput.writeShort(BidInfoEncoding.PRICE_POINTS.ordinal());
			PricePoint pricePoints[] = this.bidInfo.getPricePoints();
			dataOutput.writeShort(pricePoints.length);
			for (int i = 0; i < pricePoints.length; i++) {
				dataOutput.writeShort(pricePoints[i].getNormalizedPrice());
				dataOutput.writeFloat((float) pricePoints[i].getDemand());
			}
		} else if (this.bidInfo.getDemand() != null) {
			dataOutput.writeShort(BidInfoEncoding.DEMAND_ARRAY.ordinal());
			double demand[] = this.bidInfo.getDemand();
			dataOutput.writeShort(demand.length);
			for (int i = 0; i < demand.length; i++) {
				dataOutput.writeFloat((float) (demand[i]));
			}
		} else {
			dataOutput.writeShort(BidInfoEncoding.NO_BIDINFO.ordinal());
		}
	}

}
