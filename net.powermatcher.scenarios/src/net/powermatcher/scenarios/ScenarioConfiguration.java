package net.powermatcher.scenarios;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioConfiguration {
	private static final transient Logger logger = LoggerFactory.getLogger(ScenarioConfiguration.class);
	private transient Configuration configuration;
	
	public String bundleId;
	public String factoryId;
	public Dictionary<String, String> properties;
	
	public void start(ConfigurationAdmin configurationAdmin) throws Exception {
		if (configuration != null) {
			throw new Exception("Configuration has already been created");
		}
		Bundle bundle = getBundle();
		if (bundle != null) {
			configuration = configurationAdmin.createFactoryConfiguration(factoryId, bundle.getLocation());
			configuration.update(properties);
		} else {
			logger.warn("Ignoring configuration, bundle %s could not be found", bundleId);
		}
	}
	
	public void delete() throws Exception {
		if (configuration == null) {
			throw new Exception("Configuration has not yet been created");
		}
		configuration.delete();
	}
	
	private Bundle getBundle() {
        for (Bundle bundle : FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles()) {
            if (bundleId.equals(bundle.getSymbolicName())) {
                return bundle;
            }
        }
        return null;
    }
}