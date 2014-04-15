package net.powermatcher.core.agent.marketbasis.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 * OSGi wrapper component for a MarketBasisAdapterFactory.
 * 
 * <p>
 * The TargetAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a MarketBasisAdapter. A ConnectorTracker will bind the adapter component to a component 
 * that implements the AgentConnectorService interface if that component instance has the
 * same id and cluster id.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see MarketBasisAdapterFactory
 * @see MarketBasisAdapterFactoryComponentConfiguration
 */
@Component(name = MarketBasisAdapterFactoryComponent.COMPONENT_NAME, designateFactory = MarketBasisAdapterFactoryComponentConfiguration.class)
public class MarketBasisAdapterFactoryComponent extends TargetAdapterFactoryComponent<AgentConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public MarketBasisAdapterFactoryComponent() {
		super(new MarketBasisAdapterFactory());
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		super.activate(context, properties);
	}

	/**
	 * Add matcher connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @see #removeMatcherConnector(AgentConnectorService)
	 */
	@Reference(type = '*')
	protected void addMatcherConnector(final AgentConnectorService targetConnector) {
		super.addTargetConnector(targetConnector);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * Remove matcher connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @see #addMatcherConnector(AgentConnectorService)
	 */
	protected void removeMatcherConnector(final AgentConnectorService targetConnector) {
		super.removeTargetConnector(targetConnector);
	}

	/**
	 * Get the Java type of the connector T.
	 * Due to type erasure it is necessary to gave a method return the type explicitly for use
	 * in the call to getTargetConnectorIds.
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(ConnectorService) 
	 * @return The Java type of the connector T.
	 */
	@Override
	protected Class<AgentConnectorService> getConnectorType() {
		return AgentConnectorService.class;
	}

}
