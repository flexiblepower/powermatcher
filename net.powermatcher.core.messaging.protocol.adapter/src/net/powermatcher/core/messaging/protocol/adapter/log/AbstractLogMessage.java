package net.powermatcher.core.messaging.protocol.adapter.log;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Arrays;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.log.AbstractLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.msg.AbstractMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractLogMessage implements AbstractMessage {
	/**
	 *
	 */
	public enum MessageType {
		/**
		 * Ordinal 0
		 */
		UNDEFINED,
		/**
		 * Ordinal 1
		 */
		PRICE,
		/**
		 * Ordinal 2
		 */
		BID
	}

	/**
	 *
	 */
	public enum Version {
		/**
		 * Ordinal 0
		 */
		UNDEFINED,
		/**
		 * Ordinal 1
		 */
		VERSION_1
	}

	/**
	 * Define the buffer size (int) constant.
	 */
	private static final int BUFFER_SIZE = 2560;
	/**
	 * Define the version (Version) field.
	 */
	private Version version = Version.VERSION_1;
	/**
	 * Define the msg type (MessageType) field.
	 */
	private MessageType msgType;

	/**
	 * Constructs an instance of this class from the specified msg type
	 * parameter.
	 * 
	 * @param msgType
	 *            The msg type (<code>MessageType</code>) parameter.
	 */
	protected AbstractLogMessage(final MessageType msgType) {
		this.msgType = msgType;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	protected abstract void appendData(final StringBuilder strb);

	/**
	 * Equals with the specified obj parameter and return the boolean result.
	 * 
	 * @param obj
	 *            The obj (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj != null && obj.getClass().equals(getClass())) {
			AbstractLogMessage msg = (AbstractLogMessage) obj;
			return Arrays.equals(toBytes(), msg.toBytes());
		}
		return false;
	}

	/**
	 * From bytes with the specified msg parameter.
	 * 
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #fromBytes(MarketBasisCache,byte[])
	 * @see #toBytes()
	 */
	public void fromBytes(final byte[] msg) throws InvalidObjectException {
		try {
			DataInput dataInput = new DataInputStream(new ByteArrayInputStream(msg));
			byte messageVersion = dataInput.readByte();
			if (messageVersion != (byte) getVersion().ordinal()) {
				throw new InvalidObjectException("Unsupported message version");
			}
			byte messageType = dataInput.readByte();
			if (messageType != (byte) getMsgType().ordinal()) {
				throw new InvalidObjectException("Unexpected message type");
			}
			fromDataInput(dataInput);
		} catch (final IOException e) {
			throw new InvalidObjectException("Invalid message payload");
		}
	}

	/**
	 * From bytes with the specified market basis cache and msg parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #fromBytes(byte[])
	 * @see #toBytes()
	 */
	@Override
	public void fromBytes(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
		fromBytes(msg);
	}

	/**
	 * From data input with the specified data input parameter.
	 * 
	 * @param dataInput
	 *            The data input (<code>DataInput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 * @see #fromDataInput(DataInput,String,String,String,Date,MarketBasis)
	 */
	protected void fromDataInput(final DataInput dataInput) throws IOException {
		String clusterId = dataInput.readUTF();
		String agentId = dataInput.readUTF();
		String qualifier = dataInput.readUTF();
		Date timestamp = new Date(dataInput.readLong());
		String commodity = dataInput.readUTF();
		String currency = dataInput.readUTF();
		int priceSteps = dataInput.readShort();
		double minPrice = dataInput.readFloat();
		double maxPrice = dataInput.readFloat();
		int marketRef = dataInput.readByte();
		MarketBasis marketBasis = new MarketBasis(commodity, currency, priceSteps, minPrice, maxPrice, 0, marketRef);
		fromDataInput(dataInput, clusterId, agentId, qualifier, timestamp, marketBasis);
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
	 * @see #fromDataInput(DataInput)
	 */
	protected abstract void fromDataInput(final DataInput dataInput, final String clusterId, final String agentId,
			final String qualifier, final Date timestamp, final MarketBasis marketBasis) throws IOException;

	/**
	 * Gets the msg type (MessageType) value.
	 * 
	 * @return The msg type (<code>MessageType</code>) value.
	 */
	public MessageType getMsgType() {
		return this.msgType;
	}

	/**
	 * Gets the version value.
	 * 
	 * @return The version (<code>Version</code>) value.
	 */
	public Version getVersion() {
		return this.version;
	}

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(toBytes());
	}

	/**
	 * Returns the bytes (byte[]) value.
	 * 
	 * @return The bytes (<code>byte[]</code>) value.
	 * @see #fromBytes(byte[])
	 * @see #fromBytes(MarketBasisCache,byte[])
	 */
	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		DataOutput dataOutput = new DataOutputStream(out);
		try {
			dataOutput.writeByte(getVersion().ordinal());
			dataOutput.writeByte(getMsgType().ordinal());
			toDataOutput(dataOutput);
		} catch (final IOException e) {
			/* ignore exception */
		}
		return out.toByteArray();
	}

	/**
	 * To data output with the specified data output parameter.
	 * 
	 * @param dataOutput
	 *            The data output (<code>DataOutput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 * @see #toDataOutput(DataOutput,AbstractLogInfo)
	 */
	protected abstract void toDataOutput(final DataOutput dataOutput) throws IOException;

	/**
	 * To data output with the specified data output and log info parameters.
	 * 
	 * @param dataOutput
	 *            The data output (<code>DataOutput</code>) parameter.
	 * @param logInfo
	 *            The log info (<code>AbstractLogInfo</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 * @see #toDataOutput(DataOutput)
	 */
	protected void toDataOutput(final DataOutput dataOutput, final AbstractLogInfo logInfo) throws IOException {
		MarketBasis marketBasis = logInfo.getMarketBasis();
		dataOutput.writeUTF(logInfo.getClusterId());
		dataOutput.writeUTF(logInfo.getAgentId());
		dataOutput.writeUTF(logInfo.getQualifier());
		dataOutput.writeLong(logInfo.getTimestamp().getTime());
		dataOutput.writeUTF(marketBasis.getCommodity());
		dataOutput.writeUTF(marketBasis.getCurrency());
		dataOutput.writeShort(marketBasis.getPriceSteps());
		dataOutput.writeFloat((float) marketBasis.getMinimumPrice());
		dataOutput.writeFloat((float) marketBasis.getMaximumPrice());
		dataOutput.writeByte(marketBasis.getMarketRef());
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append(getClass().getSimpleName());
		strb.append('(');
		appendData(strb);
		strb.append(')');
		return strb.toString();
	}

}
