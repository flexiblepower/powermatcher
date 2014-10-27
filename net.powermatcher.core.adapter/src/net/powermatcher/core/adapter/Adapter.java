package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.ActiveObject;
import net.powermatcher.core.object.ConnectableObject;


/**
 * <p>
 * An adapter provides one of possibly many implementations of services used by 
 * other components, including other adapters. The abstract Adapter class is the
 * parent class of all adapter implementations. It provides a default implementation
 * of the AdapterService. Part of this implementation is inherited from the
 * ActiveObject class, so an adapter may schedule one-time or periodic asynchronous tasks.
 * </p>
 * <p>
 * The default implementation of the bind and unbind methods does noting and should
 * be implemented by the child adapter class.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ConnectableObject
 * @see Adaptable
 * @uml.annotations 
 *    uml_dependency="mmi:///#jsrctype^name=ConnectorService[jcu^name=ConnectorService.java[jpack^name=net.powermatcher.core.adapter.service[jsrcroot^srcfolder=src[project^id=net.powermatcher.core.adapter]]]]$uml.Interface"
 */
public abstract class Adapter extends ActiveObject implements Adaptable {
	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Adapter(Configurable)
	 */
	protected Adapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified 
	 * configuration parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #Adapter()
	 */
	protected Adapter(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Bind the adapter to the adaptee.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		/* do nothing */
	}

	/**
	 * Unbind the adapter from the adaptee.
	 */
	@Override
	public void unbind() {
		/* do nothing */
	}

	@Override
	protected void startPeriodicTasks() {
		/*
		 * By default there are no periodic tasks in an adapter.
		 */
	}

	@Override
	protected void stopPeriodicTasks() {
		/*
		 * By default there are no periodic tasks in an adapter.
		 */
	}

}
