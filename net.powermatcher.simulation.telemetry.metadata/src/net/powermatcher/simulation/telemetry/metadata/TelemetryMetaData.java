package net.powermatcher.simulation.telemetry.metadata;

import java.util.Arrays;

public class TelemetryMetaData {
	private ProviderDefinition[] providerDefinitions;

	public TelemetryMetaData() {
	}

	public TelemetryMetaData(ProviderDefinition... providerDefinitions) {
		super();
		this.providerDefinitions = providerDefinitions;
	}

	public ProviderDefinition[] getProviderDefinitions() {
		return providerDefinitions;
	}

	public void setProviderDefinitions(ProviderDefinition[] providerDefinitions) {
		this.providerDefinitions = providerDefinitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(providerDefinitions);
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
		TelemetryMetaData other = (TelemetryMetaData) obj;
		if (!Arrays.equals(providerDefinitions, other.providerDefinitions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TelemetryMetaData [providerDefinitions=" + Arrays.toString(providerDefinitions) + "]";
	}
}
