package net.powermatcher.core.objectiveagent;

import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.core.BaseAgent;

/**
 * Base implementation of an {@link BaseObjectiveAgent}. Each objective agent will require the interface of
 * ObjectiveEndpoint with notifyPriceUpdate(Price) and handleAggregateBid(Bid).
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class BaseObjectiveAgent extends BaseAgent implements ObjectiveEndpoint {

}
