package net.powermatcher.core.messaging.protocol.adapter.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.MatcherConnectorService;
import net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 * OSGi wrapper component for a MatcherProtocolAdapterFactory.
 * 
 * <p>
 * The TargetAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a MatcherProtocolAdapter. A ConnectorTracker will bind the adapter component to a component 
 * that implements the MatcherConnectorService interface if that component instance has the
 * same id and cluster id.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherProtocolAdapterFactory
 * @see MatcherProtocolAdapterFactoryComponentConfiguration
 */
@Component(name = MatcherProtocolAdapterFactoryComponent.COMPONENT_NAME, designateFactory = MatcherProtocolAdapterFactoryComponentConfiguration.class)
public class MatcherProtocolAdapterFactoryComponent extends TargetAdapterFactoryComponent<MatcherConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public MatcherProtocolAdapterFactoryComponent() {
		super(new MatcherProtocolAdapterFactory());
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
	 *            The target connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 * @see #removeMatcherConnector(MatcherConnectorService)
	 */
	@Reference(type = '*')
	protected void addMatcherConnector(final MatcherConnectorService targetConnector) {
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
	 *            The target connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 * @see #addMatcherConnector(MatcherConnectorService)
	 */
	protected void removeMatcherConnector(final MatcherConnectorService targetConnector) {
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
	protected Class<MatcherConnectorService> getConnectorType() {
		return MatcherConnectorService.class;
	}

}
