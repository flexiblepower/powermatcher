package net.powermatcher.stubs.miele.appliance;


/**
 * @author IBM
 * @version 1.0.0
 */
public interface IMieleFridgeFreezerStub extends IMieleAppliance {

	/**
	 * @return TODO
	 */
	public int getFreezerStatus();

	/**
	 * @return TODO
	 */
	public float getFreezerTemperatureVariance();

	/**
	 * @return TODO
	 */
	public int getFridgeStatus();

	/**
	 * @return TODO
	 */
	public float getFridgeTemperatureVariance();

	/**
	 * @return TODO
	 */
	public float getTargetTemperatureFreezer();

	/**
	 * @return TODO
	 */
	public float getTargetTemperatureFridge();

	// Freezer interface
	/**
	 * @return TODO
	 */
	public float getTemperatureFreezer();

	// Refrigerator interface
	/**
	 * @return TODO
	 */
	public float getTemperatureFridge();

	/**
	 * @return TODO
	 */
	public boolean isSuperCool();

	/**
	 * @return TODO
	 */
	public boolean isSuperFrost();

	/**
	 * @param variance
	 */
	public void setFreezerTemperatureVariance(float variance);

	/**
	 * @param variance
	 */
	public void setFridgeTemperatureVariance(float variance);

	/**
	 * @param value
	 */
	public void setSuperCool(boolean value);

	/**
	 * @param value
	 */
	public void setSuperFrost(boolean value);

	/**
	 * @param temperature
	 */
	public void setTargetTemperatureFreezer(float temperature);

	/**
	 * @param temperature
	 */
	public void setTargetTemperatureFridge(float temperature);

}
