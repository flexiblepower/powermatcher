package net.powermatcher.core.messaging.protocol.adapter.log;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.BidLogInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public class BidLogMessage extends AbstractLogMessage {
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
	 * Define the bid log info (BidLogInfo) field.
	 */
	private BidLogInfo bidLogInfo;

	/**
	 * Constructs an instance of this class from the specified bid log info
	 * parameter.
	 * 
	 * @param bidLogInfo
	 *            The bid log info (<code>BidLogInfo</code>) parameter.
	 * @see #BidLogMessage(byte[])
	 */
	public BidLogMessage(final BidLogInfo bidLogInfo) {
		super(MessageType.BID);
		this.bidLogInfo = bidLogInfo;
	}

	/**
	 * Constructs an instance of this class from the specified msg parameter.
	 * 
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #BidLogMessage(BidLogInfo)
	 */
	public BidLogMessage(final byte[] msg) throws InvalidObjectException {
		super(MessageType.BID);
		fromBytes(msg);
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		strb.append(getBidLogInfo());
	}

	/**
	 * From data input with the specified data input, cluster ID, agent ID,
	 * qualifier, time stamp and market basis parameters.
	 * 
	 * @param dataInput
	 *            The data input (<code>DataInput</code>) parameter.
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>Date</code>) parameter.
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void fromDataInput(final DataInput dataInput, final String clusterId, final String agentId,
			final String qualifier, final Date timestamp, final MarketBasis marketBasis) throws IOException {
		double effectivePrice = dataInput.readFloat();
		double effectiveDemand = dataInput.readFloat();
		double minimumDemand = dataInput.readFloat();
		double maximumDemand = dataInput.readFloat();
		BidInfoEncoding encodingType = BidInfoEncoding.values()[dataInput.readShort()];
		BidInfo bidInfo = null;
		if (encodingType == BidInfoEncoding.PRICE_POINTS) {
			double scaleFactor = dataInput.readFloat();
			int numPoints = dataInput.readShort();
			PricePoint pricePoints[] = new PricePoint[numPoints];
			for (int i = 0; i < pricePoints.length; i++) {
				int normalizedPrice = dataInput.readShort();
				int scaledDemand = dataInput.readShort();
				pricePoints[i] = new PricePoint(normalizedPrice, scaledDemand * scaleFactor);
			}
			bidInfo = new BidInfo(marketBasis, pricePoints);
		} else if (encodingType == BidInfoEncoding.DEMAND_ARRAY) {
			double scaleFactor = dataInput.readFloat();
			double demand[] = new double[marketBasis.getPriceSteps()];
			for (int i = 0; i < demand.length; i++) {
				demand[i] = dataInput.readShort() * scaleFactor;
			}
			bidInfo = new BidInfo(marketBasis, demand);
		}
		this.bidLogInfo = new BidLogInfo(clusterId, agentId, qualifier, timestamp, marketBasis, effectivePrice,
				effectiveDemand, minimumDemand, maximumDemand, bidInfo);
	}

	/**
	 * Gets the bid log info value.
	 * 
	 * @return The bid log info (<code>BidLogInfo</code>) value.
	 */
	public BidLogInfo getBidLogInfo() {
		return this.bidLogInfo;
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
		super.toDataOutput(dataOutput, this.bidLogInfo);
		dataOutput.writeFloat((float) this.bidLogInfo.getEffectivePrice());
		dataOutput.writeFloat((float) this.bidLogInfo.getEffectiveDemand());
		dataOutput.writeFloat((float) this.bidLogInfo.getMinimumDemand());
		dataOutput.writeFloat((float) this.bidLogInfo.getMaximumDemand());
		BidInfo bidInfo = this.bidLogInfo.getBidInfo();
		if (bidInfo == null) {
			dataOutput.writeShort(BidInfoEncoding.NO_BIDINFO.ordinal());
		} else if (bidInfo.getPricePoints() != null) {
			dataOutput.writeShort(BidInfoEncoding.PRICE_POINTS.ordinal());
			double scaleFactor = bidInfo.getScaleFactor(Short.MAX_VALUE);
			dataOutput.writeFloat((float) scaleFactor);
			PricePoint pricePoints[] = bidInfo.getPricePoints();
			dataOutput.writeShort(pricePoints.length);
			for (int i = 0; i < pricePoints.length; i++) {
				dataOutput.writeShort(pricePoints[i].getNormalizedPrice());
				dataOutput.writeShort((short) (pricePoints[i].getDemand() / scaleFactor));
			}
		} else if (bidInfo.getDemand() != null) {
			dataOutput.writeShort(BidInfoEncoding.DEMAND_ARRAY.ordinal());
			double scaleFactor = bidInfo.getScaleFactor(Short.MAX_VALUE);
			dataOutput.writeFloat((float) scaleFactor);
			double demand[] = bidInfo.getDemand();
			for (int i = 0; i < demand.length; i++) {
				dataOutput.writeShort((short) (demand[i] / scaleFactor));
			}
		} else {
			dataOutput.writeShort(BidInfoEncoding.NO_BIDINFO.ordinal());
		}
	}

}
