package net.powermatcher.api.monitoring;

import net.powermatcher.api.Agent;

/**
 * {@link ObservableAgent} defines the interface with the basic functionality
 * needed to be able to be observed by an {@link AgentObserver}.
 * 
 * @author FAN
 * @version 2.0
 */
public interface ObservableAgent extends Agent {

	/**
	 * Used to add an {@link AgentObserver} to the list of observers of this
	 * instance.
	 * 
	 * @param observer
	 *            the new {@link AgentObserver}.
	 */
	void addObserver(AgentObserver observer);

	/**
	 * Used to remove an {@link AgentObserver} from the list of observers of
	 * this instance.
	 * 
	 * @param observer
	 *            the {@link AgentObserver} that will be removed.
	 */
	void removeObserver(AgentObserver observer);
}
