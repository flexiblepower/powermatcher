package net.powermatcher.core.xml.standard.parser;


import java.util.Hashtable;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.xml.XMLParserActivator;

/**
 * @author IBM
 * @version 0.9.0
 */
public class Activator extends XMLParserActivator implements BundleActivator, ServiceFactory {
	/**
	 * Define the saxfactorydescription (String) constant.
	 */
	private static final String SAXFACTORYDESCRIPTION = "The Java SE default JAXP Compliant SAX Parser";
	/**
	 * Define the context (BundleContext) field.
	 */
	private volatile BundleContext context;

	/**
	 * Gets the factory (SAXParserFactory) value.
	 * 
	 * @return The factory (<code>SAXParserFactory</code>) value.
	 */
	private SAXParserFactory getFactory() {
		return SAXParserFactory.newInstance();
	}

	/**
	 * Get service with the specified bundle and registration parameters and
	 * return the Object result.
	 * 
	 * @param bundle
	 *            The bundle (<code>Bundle</code>) parameter.
	 * @param registration
	 *            The registration (<code>ServiceRegistration</code>) parameter.
	 * @return Results of the get service (<code>Object</code>) value.
	 */
	@Override
	public Object getService(final Bundle bundle, final ServiceRegistration registration) {
		ServiceReference sref = registration.getReference();
		SAXParserFactory factory = getFactory();
		factory.setValidating(((Boolean) sref.getProperty(PARSER_VALIDATING)).booleanValue());
		factory.setNamespaceAware(((Boolean) sref.getProperty(PARSER_NAMESPACEAWARE)).booleanValue());
		return factory;
	}

	/**
	 * Register saxparser with the specified factory parameter.
	 * 
	 * @param factory
	 *            The factory (<code>SAXParserFactory</code>) parameter.
	 * @throws FactoryConfigurationError
	 *             Factory Configuration Error.
	 */
	private void registerSAXParser(final SAXParserFactory factory) throws FactoryConfigurationError {
		Hashtable<String, Object> properties = new Hashtable<String, Object>(7);
		setDefaultSAXProperties(factory, properties);
		this.context.registerService(SAXFACTORYNAME, this, properties);
	}

	/**
	 * Set default saxproperties with the specified factory and props
	 * parameters.
	 * 
	 * @param factory
	 *            The factory (<code>SAXParserFactory</code>) parameter.
	 * @param props
	 *            The props (<code>Hashtable<String,Object></code>) parameter.
	 */
	private void setDefaultSAXProperties(final SAXParserFactory factory, final Hashtable<String, Object> props) {
		props.put(Constants.SERVICE_DESCRIPTION, SAXFACTORYDESCRIPTION);
		props.put(Constants.SERVICE_PID, SAXFACTORYNAME + "." + this.context.getBundle().getBundleId());
		setSAXProperties(factory, props);
	}

	/**
	 * Start with the specified context parameter.
	 * 
	 * @param context
	 *            The context (<code>BundleContext</code>) parameter.
	 * @throws Exception
	 *             Exception.
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		registerSAXParser(getFactory());
	}

}
