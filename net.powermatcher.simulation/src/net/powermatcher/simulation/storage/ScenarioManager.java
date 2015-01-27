package net.powermatcher.simulation.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta.AD;

@Component(provide = { ScenarioManager.class },
	designate = ScenarioManager.Config.class)
public class ScenarioManager {
    public interface Config {
        @AD(description = "The scenario files that should be started during activation.",
            required = false)
        String[] filenames();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(ScenarioManager.class);
    private Config config;
    private ConfigurationAdmin configurationAdmin;
    
    private List<Scenario> scenarios;

    @Activate
    public void activate(BundleContext context, Map<String, Object> properties) {
    	config = Configurable.createConfigurable(Config.class, properties);
    	for (String filename : config.filenames()) {
    		try {
    			start(Scenario.load(new File(filename)));
    		} catch (IOException ex) {
    			logger.error("Could not load scenario %s: %s", filename, ex.getMessage());
    		}
    	}
    }
    
    @Deactivate
    public void deactivate() {
    	clean();
    }
    
    public void start(Scenario scenario) {
    	scenario.start(configurationAdmin);
    	scenarios.add(scenario);
    }
    
    public void clean() {
    	for (Scenario scenario : scenarios) {
    		scenario.clean();
    	}
    }
}
