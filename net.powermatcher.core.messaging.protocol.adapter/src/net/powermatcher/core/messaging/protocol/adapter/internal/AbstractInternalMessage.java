package net.powermatcher.core.messaging.protocol.adapter.internal;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Arrays;

import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.messaging.protocol.adapter.han.AbstractHANMessage.Commodity;
import net.powermatcher.core.messaging.protocol.adapter.msg.AbstractMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractInternalMessage implements AbstractMessage {
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
	 * Define the protocol ID (int) constant.
	 */
	private static final int PROTOCOL_ID = 0x504D494E; /* PMIN */
	/**
	 * Define the default currency (String) constant.
	 */
	public static final String DEFAULT_CURRENCY = "EUR";
	/**
	 * Define the default commodity (String) constant.
	 */
	public static final String DEFAULT_COMMODITY = Commodity.ELECTRICITY.name();
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
	protected AbstractInternalMessage(final MessageType msgType) {
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
			AbstractInternalMessage msg = (AbstractInternalMessage) obj;
			return Arrays.equals(toBytes(), msg.toBytes());
		}
		return false;
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
		try {
			DataInput dataInput = new DataInputStream(new ByteArrayInputStream(msg));
			int protocolId = dataInput.readInt();
			if (protocolId != PROTOCOL_ID) {
				throw new InvalidObjectException("Unexpected protocol id");
			}
			byte messageVersion = dataInput.readByte();
			if (messageVersion != (byte) getVersion().ordinal()) {
				throw new InvalidObjectException("Unsupported message version");
			}
			byte messageType = dataInput.readByte();
			if (messageType != (byte) getMsgType().ordinal()) {
				throw new InvalidObjectException("Unexpected message type");
			}
			fromDataInput(marketBasisCache, dataInput);
		} catch (final InvalidObjectException e) {
			throw e;
		} catch (final IOException e) {
			throw new InvalidObjectException("Invalid message payload");
		}
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
	protected abstract void fromDataInput(final MarketBasisCache marketBasisCache, final DataInput dataInput)
			throws IOException;

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
	 * @see #fromBytes(MarketBasisCache,byte[])
	 */
	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		DataOutput dataOutput = new DataOutputStream(out);
		try {
			dataOutput.writeInt(PROTOCOL_ID);
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
	 */
	protected abstract void toDataOutput(final DataOutput dataOutput) throws IOException;

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
