package net.powermatcher.scenarios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Scenario {
    private static final transient Logger logger = LoggerFactory.getLogger(Scenario.class);

    public List<ScenarioConfiguration> configurations;

    public Scenario(List<ScenarioConfiguration> configurations) {
        this.configurations = configurations;
    }

    public static Scenario load(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        try {
            StringBuffer fileContents = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                fileContents.append(line);
                line = bufferedReader.readLine();
            }
            return load(fileContents.toString());
        } finally {
            bufferedReader.close();
        }
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
