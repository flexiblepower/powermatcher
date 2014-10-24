package net.powermatcher.core.messaging.adapter.template.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.template.service.ExampleConnectorService;
import net.powermatcher.core.messaging.adapter.template.ExampleAdapterFactory;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 * This class exports an adapter factory as an OSGi managed factory service component.
 * An instance of this component is created and activated by the OSGi runtime for each factory configuration
 * that is created in the OSGi Configuration Admin service for the COMPONENT_NAME pid.
 * The factory creates an ExampleAdapter instance for each registered ExampleConnectorService with a matching factoru id.
 *
 * @author IBM
 * @version 0.9.0
 * 
 * @see ExampleAdapterFactory
 * @see ExampleAdapterFactoryComponentConfiguration
 */
@Component(name = ExampleAdapterFactoryComponent.COMPONENT_NAME, designateFactory = ExampleAdapterFactoryComponentConfiguration.class)
public class ExampleAdapterFactoryComponent extends TargetAdapterFactoryComponent<ExampleConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.messaging.adapter.template.ExampleAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public ExampleAdapterFactoryComponent() {
		super(new ExampleAdapterFactory());
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
	 * Add example connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>ExampleConnectorService</code>)
	 *            parameter.
	 * @see #removeExampleConnector(ExampleConnectorService)
	 */
	@Reference(type = '*')
	protected void addExampleConnector(final ExampleConnectorService targetConnector) {
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
	 * Remove example connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>ExampleConnectorService</code>)
	 *            parameter.
	 * @see #addExampleConnector(ExampleConnectorService)
	 */
	protected void removeExampleConnector(final ExampleConnectorService targetConnector) {
		super.removeTargetConnector(targetConnector);
	}

	/**
	 * Get the Java type of the connector T.
	 * Due to type erasure it is necessary to gave a method return the type explicitly for use
	 * in the call to getTargetConnectorIds.
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(Connectable) 
	 * @return The Java type of the connector T.
	 */
	@Override
	protected Class<ExampleConnectorService> getConnectorType() {
		return ExampleConnectorService.class;
	}

}
