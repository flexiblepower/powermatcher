package net.powermatcher.core.concentrator;

import javax.measure.Measurable;
import javax.measure.quantity.Power;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;

/**
 * This is the API for a TransformingConcentrator that should be linked with the current measurements of a transformer.
 *
 * @see TransformingConcentrator#setMeasuredFlow(Measurable)
 */
public interface TransformingConcentrator
    extends AgentEndpoint, MatcherEndpoint {
    /**
     * Sets the current measured flow for a transformer.
     * 
     * @param measuredFlow
     *            The current power flow.
     */
    void setMeasuredFlow(Measurable<Power> measuredFlow);
}
