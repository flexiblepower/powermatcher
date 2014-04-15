package net.powermatcher.der.agent.miele.at.home;


/**
 * @author IBM
 * @version 0.9.0
 */
public class HttpUtilException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4737650105850804444L;

	/**
	 * @param msg
	 */
	public HttpUtilException(final String msg) {
		super(msg);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HttpUtilException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
