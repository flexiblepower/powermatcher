package net.powermatcher.simulation.configuration;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

public final class HashMapAdapter extends XmlAdapter<ConfigMarshallerType, HashMap<String, Object>> {
	@Override
	public ConfigMarshallerType marshal(HashMap<String, Object> v) throws Exception {
		ConfigMarshallerType map = new ConfigMarshallerType();

		for (Entry<String, Object> entry : v.entrySet()) {
			String key = entry.getKey();
			if (key.equalsIgnoreCase(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY)
					|| key.equalsIgnoreCase(AgentConfiguration.PARENT_MATCHER_ID_PROPERTY)) {
				continue;
			}

			map.getEntry().add(new ConfigMarshallerEntryType(key, String.valueOf(entry.getValue())));
		}
		return map;
	}

	@Override
	public HashMap<String, Object> unmarshal(ConfigMarshallerType v) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (ConfigMarshallerEntryType entry : v.getEntry()) {
			map.put(entry.key, entry.value);
		}

		return map;
	}
}