package net.powermatcher.der.agent.miele.at.home.msg;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleGatewayErrorMessage extends MieleGatewayMessage {

	protected String message;
	protected String errorType;

	/**
	 * @return TODO
	 */
	public String getErrorType() {
		return this.errorType;
	}

	/**
	 * @return TODO
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @param errorType
	 */
	public void setErrorType(final String errorType) {
		this.errorType = errorType;
	}

	/**
	 * @param message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}
}
