package net.powermatcher.simulation.gui.views;

import net.powermatcher.simulation.configuration.ConfigurationElement;

import org.eclipse.swt.dnd.TransferData;

public class ConfigurationElementTransferData extends TransferData {
	private ConfigurationElement element;

	public ConfigurationElementTransferData() {
	}

	public ConfigurationElementTransferData(ConfigurationElement element) {
		this.element = element;
	}

	public ConfigurationElement getElement() {
		return element;
	}

	public void setElement(ConfigurationElement element) {
		this.element = element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationElementTransferData other = (ConfigurationElementTransferData) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConfigurationElementTransferData [element=" + element + "]";
	}

}
