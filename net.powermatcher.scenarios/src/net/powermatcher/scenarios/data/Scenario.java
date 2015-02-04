package net.powermatcher.scenarios.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Scenario {
    // JSON variables
    public List<ScenarioConfiguration> configurations;

    // Transient variables (non-JSON)
    private static final transient Logger logger = LoggerFactory.getLogger(Scenario.class);

    public Scenario() {
    }

    public Scenario(List<ScenarioConfiguration> configurations) {
        this.configurations = configurations;
    }

    public static Scenario load(URL url) throws IOException {
        return load(IOUtils.toString(url));
    }

    public static Scenario load(String json) {
        return new Gson().fromJson(json, Scenario.class);
    }

    public String save() {
        return new Gson().toJson(this);
    }

    public void save(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(save());
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void start(ConfigurationAdmin configurationAdmin) {
        for (ScenarioConfiguration configuration : configurations) {
            try {
                configuration.start(configurationAdmin);
            } catch (Exception ex) {
                logger.error("Could not start scenario %s: %s", configuration.factoryId, ex.getMessage());
            }
        }
    }

    public void clean() {
        for (ScenarioConfiguration configuration : configurations) {
            try {
                configuration.delete();
            } catch (Exception ex) {
                logger.error("Could not delete scenario %s: %s", configuration.factoryId, ex.getMessage());
            }
        }
    }
}
