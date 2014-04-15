package net.powermatcher.der.agent.miele.at.home.msg;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleGatewayActionOkMessage extends MieleGatewayMessage {

	protected String action;

	/**
	 * @return TODO
	 */
	public String getAction() {
		return this.action;
	}

	/**
	 * @param action
	 */
	public void setAction(final String action) {
		this.action = action;
	}
}
