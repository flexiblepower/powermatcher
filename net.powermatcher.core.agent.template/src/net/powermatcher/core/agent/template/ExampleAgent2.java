package net.powermatcher.core.agent.template;


import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.template.service.ExampleConnectorService;
import net.powermatcher.core.agent.template.service.ExampleControlService;
import net.powermatcher.core.agent.template.service.ExampleNotificationService;


/**
 * This class is an extension of <code>ExampleAgent1</code> that implements a control and notification interface for integration
 * of this agent with an adapter.<br>
 * The unmodified behavior that this class inherits from <code>ExampleAgent1</code> is that it publishes a static step-shaped bid 
 * for a configurable demand and price value.
 * @author IBM
 * @version 0.9.0
 */
public class ExampleAgent2 extends ExampleAgent1 implements ExampleConnectorService {

	/**
	 * Provides a private implementation of the agent's control service interface.
	 * In this example the implementation does nothing more than log that the 
	 * control method is being invoked by the adapter.
	 * 
	 * @author IBM
	 * @version 0.9.0
	 */
	private class ExampleServiceImpl implements ExampleControlService {

		/* (non-Javadoc)
		 * @see net.powermatcher.core.agent.template.service.ExampleControlService#doSomething()
		 */
		@Override
		public void doSomething() {
			logInfo("Adapter called doSomething");
		}

	}

	/**
	 * Define the example adapter (ExampleNotificationService) field.
	 * This field references the adapter that is currently bound to the agent, or
	 * is null if no adapter is bound.
	 */
	private ExampleNotificationService exampleAdapter;

	/**
	 * Bind an adapter's notification interface to the agent.
	 * This implementation supports only one adapter being bound at the same time.
	 * @see net.powermatcher.core.agent.template.service.ExampleConnectorService#bind(net.powermatcher.core.agent.template.service.ExampleNotificationService)
	 */
	@Override
	public void bind(final ExampleNotificationService exampleAdapter) {
		assert this.exampleAdapter == null;
		this.exampleAdapter = exampleAdapter;
	}

	/**
	 * Gets the agent's control interface (ExampleControlService).
	 * @see net.powermatcher.core.agent.template.service.ExampleConnectorService#getExampleService()
	 */
	@Override
	public ExampleControlService getExampleService() {
		return new ExampleServiceImpl();
	}

	/**
	 * Unbind an adapter's notification interface from the agent.
	 * @see net.powermatcher.core.agent.template.service.ExampleConnectorService#unbind(net.powermatcher.core.agent.template.service.ExampleNotificationService)
	 */
	@Override
	public void unbind(final ExampleNotificationService exampleAdapter) {
		this.exampleAdapter = null;
	}

	/**
	 * Update price info with the specified new price info parameter.
	 * This implementation extends the super behavior by notifying the adapter that something has changed.
	 * @see net.powermatcher.core.agent.template.ExampleAgent1#updatePriceInfo(net.powermatcher.core.agent.framework.data.PriceInfo)
	 */
	@Override
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		/*
		 * Copy the field to a local variable for lock-free thread safety.
		 * This avoids the need for synchronization around references to this.exampleAdapter
		 */
		ExampleNotificationService exampleAdapter = this.exampleAdapter;
		if (exampleAdapter != null) {
			logInfo("Notifying adapter that something has changed");
			exampleAdapter.somethingChanged();
		}
		super.updatePriceInfo(newPriceInfo);
	}

}
