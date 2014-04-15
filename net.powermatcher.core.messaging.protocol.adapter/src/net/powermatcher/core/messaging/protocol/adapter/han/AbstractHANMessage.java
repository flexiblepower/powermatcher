package net.powermatcher.core.messaging.protocol.adapter.han;


import java.io.InvalidObjectException;
import java.util.Arrays;

import net.powermatcher.core.messaging.protocol.adapter.msg.AbstractMessage;


/**
 * HAN message header
 * Element		# bytes 		Description
 * version		1, unsigned int	denotes the version of PowerMatcher to which this message adheres. For now 1
 * msgtype		1, enum			the message type
 * 
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractHANMessage implements AbstractMessage {
	/**
	 *
	 */
	public enum Commodity {
		/**
		 * Ordinal 0
		 */
		UNDEFINED,
		/**
		 * Ordinal 1
		 */
		ELECTRICITY;

		/**
		 * Return enum value for name converted to uppercase.
		 * 
		 * @param name
		 * @return The commodity enum value for name
		 */
		public static Commodity normalizedValueOf(final String name) {
			return valueOf(name.toUpperCase());
		}

		/**
		 * Return lowercase string for enum value.
		 * 
		 * @return The commodity enum value lowercase string
		 */
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

	}

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
	 * Define the default currency (String) constant.
	 */
	public static final String DEFAULT_CURRENCY = "EUR";
	/**
	 * Define the default commodity (String) constant.
	 */
	public static final String DEFAULT_COMMODITY = Commodity.ELECTRICITY.name();
	/**
	 * Define the minimum normalized price (int) constant.
	 */
	public static final int MINIMUM_NORMALIZED_PRICE = -127;
	/**
	 * Define the maximum normalized price (int) constant.
	 */
	public static final int MAXIMUM_NORMALIZED_PRICE = 127;
	/**
	 * Define the price steps (int) constant.
	 */
	public static final int PRICE_STEPS = MAXIMUM_NORMALIZED_PRICE - MINIMUM_NORMALIZED_PRICE + 1;
	/**
	 * Define the header size (int) constant.
	 */
	protected static final int HEADER_SIZE = 2;
	/**
	 * Define the version offset (int) constant.
	 */
	protected static final int VERSION_OFFSET = 0;
	/**
	 * Define the msgtype offset (int) constant.
	 */
	protected static final int MSGTYPE_OFFSET = 1;
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
	protected AbstractHANMessage(final MessageType msgType) {
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
			AbstractHANMessage msg = (AbstractHANMessage) obj;
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
	 * @see #toBytes()
	 */
	protected void fromBytes(final byte[] msg) throws InvalidObjectException {
		if (msg.length < getSize()) {
			throw new InvalidObjectException("Invalid message payload, too few bytes");
		} else if (msg[VERSION_OFFSET] != (byte) getVersion().ordinal()) {
			throw new InvalidObjectException("Unsupported message version");
		} else if (msg[MSGTYPE_OFFSET] != (byte) getMsgType().ordinal()) {
			throw new InvalidObjectException("Unexpected message type");
		}
	}

	/**
	 * Gets the msg type (MessageType) value.
	 * 
	 * @return The msg type (<code>MessageType</code>) value.
	 */
	public MessageType getMsgType() {
		return this.msgType;
	}

	/**
	 * Gets the size (int) value.
	 * 
	 * @return The size (<code>int</code>) value.
	 */
	protected abstract int getSize();

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
	 */
	@Override
	public byte[] toBytes() {
		byte[] msg = new byte[getSize()];
		msg[0] = (byte) getVersion().ordinal();
		msg[1] = (byte) getMsgType().ordinal();
		return msg;
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
