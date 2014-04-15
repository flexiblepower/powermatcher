package net.powermatche.simulation.cli;

import java.io.File;

import net.powermatcher.simulation.configuration.Scenario;
import net.powermatcher.simulation.configuration.XmlSerializer;
import net.powermatcher.simulation.engine.ScenarioInitializer;
import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.engine.dependencyengine.DependencyEngineScenarioInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the temporary user interface for the simulation tool
 */
public class Main {

	public static void main(String[] args) throws InterruptedException {
		Logger logger = LoggerFactory.getLogger(Main.class);

		System.out.println("PowerMatcher Simulation tool");
		if (args.length != 1) {
			System.err.println("Please provide the scenario XML file as argument");
			System.exit(1);
		}

		Scenario scenario = null;
		try {
			scenario = XmlSerializer.loadScenario(new File(args[0]));
		} catch (Exception e) {
			System.err.println("Could not read scenario file");
			e.printStackTrace();
			System.exit(1);
		}

		ScenarioInitializer init = new DependencyEngineScenarioInitializer();
		SimulationControl control = init.initializeScenario(scenario);
		control.start();
		Thread.sleep(100);
		control.stop();
		Thread.sleep(100);
		init.getBroker().closeAllDataSinks();
	}
}
