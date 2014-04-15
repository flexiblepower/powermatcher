package net.powermatcher.der.agent.miele.at.home.msg;


import java.util.Map;

/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleApplianceInfoMessage extends MieleGatewayMessage {

	protected String applianceId;
	protected int applianceType;
	protected int applianceState;
	protected int applianceClass;

	protected Map<String, String> actions;

	/**
	 * @return TODO
	 */
	public Map<String, String> getActions() {
		return this.actions;
	}

	/**
	 * @return TODO
	 */
	public int getApplianceClass() {
		return this.applianceClass;
	}

	/**
	 * @return TODO
	 */
	public String getApplianceId() {
		return this.applianceId;
	}

	/**
	 * @return TODO
	 */
	public int getApplianceState() {
		return this.applianceState;
	}

	/**
	 * @return TODO
	 */
	public int getApplianceType() {
		return this.applianceType;
	}

	/**
	 * @param actions
	 */
	public void setActions(final Map<String, String> actions) {
		this.actions = actions;
	}

	/**
	 * @param applianceClass
	 */
	public void setApplianceClass(final int applianceClass) {
		this.applianceClass = applianceClass;
	}

	/**
	 * @param applianceId
	 */
	public void setApplianceId(final String applianceId) {
		this.applianceId = applianceId;
	}

	/**
	 * @param applianceState
	 */
	public void setApplianceState(final int applianceState) {
		this.applianceState = applianceState;
	}

	/**
	 * @param applianceType
	 */
	public void setApplianceType(final int applianceType) {
		this.applianceType = applianceType;
	}

}
