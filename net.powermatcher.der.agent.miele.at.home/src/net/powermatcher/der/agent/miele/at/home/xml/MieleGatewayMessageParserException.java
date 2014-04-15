package net.powermatcher.der.agent.miele.at.home.xml;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleGatewayMessageParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6272001335553874524L;

	/**
	 * @param msg
	 */
	public MieleGatewayMessageParserException(final String msg) {
		super(msg);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MieleGatewayMessageParserException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
