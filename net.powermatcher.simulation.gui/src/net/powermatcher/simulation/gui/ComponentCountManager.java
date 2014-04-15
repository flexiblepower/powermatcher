package net.powermatcher.simulation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConfigurationElement;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;

// FIXME this class is very questionable ...
// it's a lot cleaner then it was, but I expect a big performance penalty from using this class
public class ComponentCountManager {
	private static ComponentCountManager instance;

	private Map<Class<?>, List<? extends ConfigurationElement<?>>> instances = new HashMap<Class<?>, List<? extends ConfigurationElement<?>>>();
	private final Map<Class<?>, List<Long>> numbers = new HashMap<Class<?>, List<Long>>();

	public static ComponentCountManager getInstance() {
		if (instance == null) {
			instance = new ComponentCountManager();
		}

		return instance;
	}

	public void updateCurrentData() {
		instances.clear();
		numbers.clear();

		ScenarioContainer scenarioContainer = Application.getInstance().getScenarios();
		instances.put(ScenarioDescriptor.class, scenarioContainer.getChildren());

		for (ScenarioDescriptor scenario : scenarioContainer.getChildren()) {
			for (ClusterDescriptor cluster : scenario.getChildren()) {
				updateConfigurationElement(cluster);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateConfigurationElement(ConfigurationElement<?> element) {
		List instancesOfType = getInstancesOfType(element.getClass());

		instancesOfType.add(element);

		for (ConfigurationElement child : element.getChildren()) {
			updateConfigurationElement(child);
		}
	}

	private List<? extends ConfigurationElement<?>> getInstancesOfType(Class<?> componentType) {
		List<? extends ConfigurationElement<?>> instancesOfType = instances.get(componentType);

		if (instancesOfType == null) {
			instancesOfType = new ArrayList<ConfigurationElement<?>>(1);
			instances.put(componentType, instancesOfType);
		}

		return instancesOfType;
	}

	public long getNextCount(Class<?> componentType) {
		List<Long> numbersOfType = numbers.get(componentType);
		if (numbersOfType != null && numbersOfType.size() > 0) {
			Collections.sort(numbersOfType);
			return numbersOfType.remove(0);
		}

		return getInstancesOfType(componentType).size() + 1;
	}

	public void addReusableNumber(Class<?> componentType, long number) {
		List<Long> numbersOfType = numbers.get(componentType);

		if (numbersOfType == null) {
			numbersOfType = new ArrayList<Long>();
			numbers.put(componentType, numbersOfType);
		}
	}

	public void removeUsableNumber(Class<?> componentType, long number) {
		List<Long> numbersOfType = numbers.get(componentType);

		if (numbersOfType != null && numbersOfType.size() > 0) {
			numbersOfType.remove(number);
		}
	}
}
