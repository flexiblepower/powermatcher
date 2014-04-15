package net.powermatcher.der.agent.miele.at.home.msg;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleFridgeFreezerInfoMessage extends MieleApplianceInfoMessage {

	protected int refrigeratorState;
	protected Float refrigeratorTargetTemperature;
	protected Float refrigeratorTemperature;

	protected int freezerState;
	protected Float freezerTargetTemperature;
	protected Float freezerTemperature;

	/**
	 * Default constructor.
	 */
	public MieleFridgeFreezerInfoMessage() {
		super();
	}

	/**
	 * Constructor to support downcasting. It creates a
	 * MieleFridgeFreezerInfoMessage from a MieleApplianceInfoMessage object.
	 * 
	 * @param a
	 *            The MieleApplianceInfoMessage instance to create a new
	 *            MieleFridgeFreezerInfoMessage object.
	 */
	public MieleFridgeFreezerInfoMessage(final MieleApplianceInfoMessage a) {
		super();

		// Set attributes from parameter
		setApplianceId(a.getApplianceId());
		setApplianceType(a.getApplianceType());
		setApplianceState(a.getApplianceState());
		setApplianceClass(a.getApplianceClass());
		setActions(a.getActions());
	}

	/**
	 * @return TODO
	 */
	public int getFreezerState() {
		return this.freezerState;
	}

	/**
	 * @return TODO
	 */
	public Float getFreezerTargetTemperature() {
		return this.freezerTargetTemperature;
	}

	/**
	 * @return TODO
	 */
	public Float getFreezerTemperature() {
		return this.freezerTemperature;
	}

	/**
	 * @return TODO
	 */
	public int getRefrigeratorState() {
		return this.refrigeratorState;
	}

	/**
	 * @return TODO
	 */
	public Float getRefrigeratorTargetTemperature() {
		return this.refrigeratorTargetTemperature;
	}

	/**
	 * @return TODO
	 */
	public Float getRefrigeratorTemperature() {
		return this.refrigeratorTemperature;
	}

	/**
	 * @param freezerState
	 */
	public void setFreezerState(final int freezerState) {
		this.freezerState = freezerState;
	}

	/**
	 * @param freezerTargetTemperature
	 */
	public void setFreezerTargetTemperature(final Float freezerTargetTemperature) {
		this.freezerTargetTemperature = freezerTargetTemperature;
	}

	/**
	 * @param freezerTemperature
	 */
	public void setFreezerTemperature(final Float freezerTemperature) {
		this.freezerTemperature = freezerTemperature;
	}

	/**
	 * @param refrigeratorState
	 */
	public void setRefrigeratorState(final int refrigeratorState) {
		this.refrigeratorState = refrigeratorState;
	}

	/**
	 * @param refrigeratorTargetTemperature
	 */
	public void setRefrigeratorTargetTemperature(final Float refrigeratorTargetTemperature) {
		this.refrigeratorTargetTemperature = refrigeratorTargetTemperature;
	}

	/**
	 * @param refrigeratorTemperature
	 */
	public void setRefrigeratorTemperature(final Float refrigeratorTemperature) {
		this.refrigeratorTemperature = refrigeratorTemperature;
	}

}
