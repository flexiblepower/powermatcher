package net.powermatcher.simulation.telemetry.metadata;

import java.util.Arrays;

public class ProviderDefinition {
	private String providerClass;
	private TelemetryDefinition[] telemetryDefinitions;

	public ProviderDefinition() {
	}

	public ProviderDefinition(String providerClass) {
		this.telemetryDefinitions = new TelemetryDefinition[0];
	}

	public ProviderDefinition(String providerClass, TelemetryDefinition... telemetryDefinitions) {
		this.providerClass = providerClass;
		this.telemetryDefinitions = telemetryDefinitions;
	}

	public String getProviderClass() {
		return providerClass;
	}

	public void setProviderClass(String providerClass) {
		this.providerClass = providerClass;
	}

	public TelemetryDefinition[] getTelemetryDefinitions() {
		return telemetryDefinitions;
	}

	public void setTelemetryDefinitions(TelemetryDefinition[] telemetryDefinitions) {
		this.telemetryDefinitions = telemetryDefinitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((providerClass == null) ? 0 : providerClass.hashCode());
		result = prime * result + Arrays.hashCode(telemetryDefinitions);
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
		ProviderDefinition other = (ProviderDefinition) obj;
		if (providerClass == null) {
			if (other.providerClass != null)
				return false;
		} else if (!providerClass.equals(other.providerClass))
			return false;
		if (!Arrays.equals(telemetryDefinitions, other.telemetryDefinitions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProviderDefinition [providerClass=" + providerClass + "]";
	}
}
