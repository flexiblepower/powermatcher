package net.powermatcher.core.agent.concentrator.service;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface PeakShavingService {
	/**
	 * Gets the current allocation based on the last propagated price to the
	 * concentrator's children and the latest aggregated bid.
	 * @return The allocation for the concentrator's children; positive flow
	 * means demand.
	 */
	public double getAllocation();

	/**
	 * Gets the amount of flow that was reduced in order to not exceed the flow
	 * constraints; defined as the difference between the allocation with the
	 * price received from the concentrator's parent and the allocation with the
	 * price propagated by the concentrator.
	 * @return The flow reduction; unreduced flow - reduced flow.
	 */
	public double getFlowReduction();

	/**
	 * Gets the uncontrolled flow (double) value.
	 * The uncontrolled flow is the flow that is outside the scope of PowerMatcher.
	 * @return The uncontrolled flow, measuredFlow - allocation; positive flow means demand.
	 */
	public double getUncontrolledFlow();

	/**
	 * Sets new flow constraints to take into account in the concentrator's
	 * transformation of aggregated bid and price. Causes re-evaluation of bid
	 * and price and their propagation.
	 * @param newCeiling
	 *		The new ceiling (<code>double</code>) parameter.
	 * @param newFloor
	 *		The new floor (<code>double</code>) parameter.
	 */
	public void setFlowConstraints(final double newCeiling, final double newFloor);

	/**
	 * Sets the measured flow value.
	 * @param newMeasuredFlow
	 *		The new measured flow (<code>double</code>) parameter.
	 */
	public void setMeasuredFlow(final double newMeasuredFlow);

}
