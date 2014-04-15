package net.powermatcher.simulation.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentManager implements BundleListener {
	private static Logger logger = LoggerFactory.getLogger(ComponentManager.class);

	private Map<Bundle, MetaTypeInformation> metaTypeByBundle = new HashMap<Bundle, MetaTypeInformation>();
	private Map<String, MetaTypeInformation> metaTypeByFactoryPid = new HashMap<String, MetaTypeInformation>();

	private List<Configuration> configurations = new ArrayList<Configuration>();

	private BundleContext bundleContext;

	private ServiceTracker metaTypeServiceTracker;
	private ServiceTracker configurationAdminTracker;

	public ComponentManager(BundleContext bundleContext) {
		this.bundleContext = bundleContext;

		metaTypeServiceTracker = new ServiceTracker(bundleContext, MetaTypeService.class.getName(), null);
		metaTypeServiceTracker.open();

		configurationAdminTracker = new ServiceTracker(bundleContext, ConfigurationAdmin.class.getName(), null);
		configurationAdminTracker.open();

		this.removeStaleConfigurations();

		this.bundleContext.addBundleListener(this);
		// read meta type information for already installed bundles
		for (Bundle bundle : this.bundleContext.getBundles()) {
			this.addMetaTypeInformation(bundle);
		}
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	// TODO remove this hack, it's required now to clean up configurations left over from a previous run ...
	private void removeStaleConfigurations() {
		try {
			ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) configurationAdminTracker.getService();
			Configuration[] existingConfigurations = configurationAdmin.listConfigurations(null);
			if (existingConfigurations == null) {
				return;
			}

			for (Configuration configuration : existingConfigurations) {
				configuration.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object createComponent(String factoryPid, Map<String, Object> properties) throws ComponentCreationException {
		// create the configuration -- causing the component to be instantiated
		Configuration config = createConfiguration(factoryPid, properties);

		// retrieve the component instance (from the service registry)
		Object component = getComponentInstance(config);

		if (component == null) {
			logger.info("Configuration created for factoryPid {}, but no component instantiated", factoryPid);

			try {
				// if it's not found, delete the configuration and throw CCE
				config.delete();
			} catch (IOException e) {
				logger.warn("Could not delete configuration", e);
			}

			throw new ComponentCreationException("Configuration created for factoryPid " + factoryPid
					+ ", but no component instantiated");
		}

		logger.info("Created component instance of type: {}", component.getClass());

		// if successful remember the configuration and return the component
		this.configurations.add(config);

		return component;
	}

	private Object getComponentInstance(final Configuration config) throws ComponentCreationException {
		final Object waitLock = new Object();

		// TODO don't use a service tracker, but an immediate lookup
		ServiceTracker serviceTracker = new ServiceTracker(bundleContext, createServicePidFilter(config),
				new ServiceTrackerCustomizer() {
					@Override
					public void removedService(ServiceReference reference, Object service) {
					}

					@Override
					public void modifiedService(ServiceReference reference, Object service) {
					}

					@Override
					public Object addingService(ServiceReference reference) {
						try {
							return bundleContext.getService(reference);
						} finally {
							synchronized (waitLock) {
								waitLock.notifyAll();
							}
						}
					}
				});

		try {
			serviceTracker.open();

			Object component = serviceTracker.getService();
			for (int i = 0; component == null && i < 100; i++) {
				synchronized (waitLock) {
					try {
						waitLock.wait(10);
					} catch (InterruptedException e) {
						throw new ComponentCreationException(e, config.getFactoryPid());
					}

					component = serviceTracker.getService();
				}
			}

			return component;
		} finally {
			serviceTracker.close();
		}

		// final Object[] component = new Object[1];
		//
		// synchronized (component) {
		// // TODO use String filterString = "(" + Constants.SERVICE_PID + "=" + config.getPid() + ")";
		// bundleContext.addServiceListener(new ServiceListener() {
		// @Override
		// public void serviceChanged(ServiceEvent event) {
		// // only interested in registrations
		// if (event.getType() != ServiceEvent.REGISTERED) {
		// return;
		// }
		//
		// // look for pid match
		// ServiceReference serviceReference = event.getServiceReference();
		// if (config.getPid().equals(serviceReference.getProperty(Constants.SERVICE_PID))) {
		// synchronized (component) {
		// component[0] = bundleContext.getService(serviceReference);
		// component.notifyAll();
		// }
		// }
		// }
		// });
		//
		// try {
		// component.wait(1000);
		// } catch (InterruptedException e) {
		// throw new ComponentCreationException("Could not create component because of: " + e.getMessage(), e);
		// }
		// }
		//
		// return component[0];
	}

	private Filter createServicePidFilter(final Configuration config) throws ComponentCreationException {
		Filter filter;

		try {
			// TODO getting the reference to the component depends on the component being registered as a service
			// which may be true in most cases ... but perhaps not in all.
			String filterString = "(" + Constants.SERVICE_PID + "=" + config.getPid() + ")";

			filter = bundleContext.createFilter(filterString);
		} catch (InvalidSyntaxException e) {
			throw new ComponentCreationException(e, config.getFactoryPid());
		}
		return filter;
	}

	private Configuration createConfiguration(String factoryPid, Map<String, Object> properties)
			throws ComponentCreationException {
		try {
			MetaTypeInformation metaTypeInformation = this.metaTypeByFactoryPid.get(factoryPid);
			if (metaTypeInformation == null) {
				throw new ComponentCreationException("No meta type information available for factoryPid: " + factoryPid
						+ ", the associated bundle may not be loaded.");
			}

			// create the factory with the location of the bundle defining it as the config location
			String location = metaTypeInformation.getBundle().getLocation();
			ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) configurationAdminTracker.getService();
			Configuration config = configurationAdmin.createFactoryConfiguration(factoryPid, location);

			// get the OCD of the factory and get the default values from the OCD and overwrite with given properties
			ObjectClassDefinition ocd = metaTypeInformation.getObjectClassDefinition(factoryPid, null);
			Dictionary<String, Object> props = getDefaults(ocd);
			for (Entry<String, Object> property : properties.entrySet()) {
				props.put(property.getKey(), property.getValue());
			}

			// create the configuration -- causing the component to be instantiated
			config.update(props);

			return config;
		} catch (IOException e) {
			throw new ComponentCreationException(e, factoryPid);
		}
	}

	/** sets the default values for the properties as defined in the OCD */
	private Dictionary<String, Object> getDefaults(ObjectClassDefinition ocd) {
		Dictionary<String, Object> props = new Hashtable<String, Object>();

		// TODO check the use of default values with the MetaType specification
		for (AttributeDefinition a : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
			if (props.get(a.getID()) != null) {
				// skip over properties which are already set
				continue;
			}
			String[] defaultValues = a.getDefaultValue();
			Object defaultValue = null;
			if (defaultValues != null && defaultValues.length > 0) {
				defaultValue = defaultValues[0];
			}

			if (defaultValue != null) {
				props.put(a.getID(), defaultValue);
			}
		}

		return props;
	}

	public void close() {
		// TODO delete configurations on closing the component manager
		// for (Configuration config : this.configurations) {
		// try {
		// config.delete();
		// logger.info("Delected configuration with pid: {}", config.getPid());
		// } catch (Exception e) {
		// logger.warn("Could not delete configuration", e);
		// }
		// }

		this.configurations.clear();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();

		switch (event.getType()) {
		case BundleEvent.INSTALLED:
			addMetaTypeInformation(bundle);
			break;
		case BundleEvent.UNINSTALLED:
			removeMetaTypeInformation(bundle);
			break;
		}
	}

	private void addMetaTypeInformation(Bundle bundle) {
		// TODO what if the metatype service isn't available (yet)?
		MetaTypeService metaTypeService = (MetaTypeService) metaTypeServiceTracker.getService();
		MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);

		if (metaTypeInformation == null || metaTypeInformation.getFactoryPids() == null
				|| metaTypeInformation.getFactoryPids().length == 0) {
			// ignore bundles without meta type information
			return;
		}

		this.metaTypeByBundle.put(bundle, metaTypeInformation);

		for (String factoryPid : metaTypeInformation.getFactoryPids()) {
			this.metaTypeByFactoryPid.put(factoryPid, metaTypeInformation);
		}

		logger.info("Added meta type information for bundle {}", bundle);
	}

	private void removeMetaTypeInformation(Bundle bundle) {
		MetaTypeInformation metaTypeInformation = this.metaTypeByBundle.remove(bundle);

		if (metaTypeInformation == null) {
			// ignore bundles without meta type information
			return;
		}

		for (String factoryPid : metaTypeInformation.getFactoryPids()) {
			this.metaTypeByFactoryPid.remove(factoryPid);
		}

		logger.info("Removed meta type information for bundle {}", bundle);
	}

}
