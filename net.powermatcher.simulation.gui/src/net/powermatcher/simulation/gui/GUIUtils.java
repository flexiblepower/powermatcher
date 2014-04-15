package net.powermatcher.simulation.gui;

import net.powermatcher.simulation.configuration.NodeDescriptor;

// FIXME this class is very questionable ...
public class GUIUtils {
	private static GUIUtils instance;

	public static GUIUtils getInstance() {
		if (instance == null) {
			instance = new GUIUtils();
		}

		return instance;
	}

	public String getDisplayableName(Object name) {
		if (name instanceof NodeDescriptor) {
			NodeDescriptor descriptor = (NodeDescriptor) name;
			String factoryPid = descriptor.getFactoryPid();
			int lastIndexOf = factoryPid.lastIndexOf(".");
			return factoryPid.substring(lastIndexOf + 1, factoryPid.length());
		} else if (name instanceof String) {
			String sname = (String) name;
			int lastIndexOf = sname.lastIndexOf(".");
			return sname.substring(lastIndexOf + 1, sname.length());
		} else {
			return null;
		}
	}
}
