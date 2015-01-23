package net.powermatcher.simulation.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.Gson;

public class Scenario {
	public List<Configuration> configurations;
	public List<Connection> connections;

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

	public void save(File file) {
		String json = new Gson().toJson(this);
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(json);
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void init() {
		
	}
	
	
	private Bundle getBundle(String bundleId) {
        for (Bundle bundle : FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles()) {
            if (bundleId.equals(bundle.getSymbolicName())) {
                return bundle;
            }
        }
        return null;
    }
}
