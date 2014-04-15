package net.powermatcher.expeditor.messaging.mqttv5.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.messaging.service.MessagingConnectorService;
import net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 *  OSGi wrapper component for a MessagingAdapterFactory.
 * 
 * <p>
 * The Mqttv5ConnectionFactoryComponent is a wrapper class that creates an OSGi component of
 * a MessagingAdapter. A ConnectorTracker will bind the adapter component to a log listener
 * component that implements the MessagingConnectorService interface if that component 
 * instance has the same id and cluster id.
 * </p>
 * Configuration properties and default values are defined in Mqttv5ConnectionFactoryComponentConfiguration.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see Mqttv5ConnectionFactory
 * @see MessagingConnectorService
 * @see Mqttv5ConnectionFactoryComponentConfiguration
 */
@Component(name = Mqttv5ConnectionFactoryComponent.COMPONENT_NAME, designateFactory = Mqttv5ConnectionFactoryComponentConfiguration.class)
public class Mqttv5ConnectionFactoryComponent extends TargetAdapterFactoryComponent<MessagingConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public Mqttv5ConnectionFactoryComponent() {
		super(new Mqttv5ConnectionFactory());
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
	 * Add log listener connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>MessagingConnectorService</code>)
	 *            parameter.
	 * @see #removeMessagingConnector(MessagingConnectorService)
	 */
	@Reference(type = '*')
	protected void addMessagingConnector(final MessagingConnectorService targetConnector) {
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
	 * Remove log listener connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>MessagingConnectorService</code>)
	 *            parameter.
	 * @see #addMessagingConnector(MessagingConnectorService)
	 */
	protected void removeMessagingConnector(final MessagingConnectorService targetConnector) {
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
	protected Class<MessagingConnectorService> getConnectorType() {
		return MessagingConnectorService.class;
	}

}
