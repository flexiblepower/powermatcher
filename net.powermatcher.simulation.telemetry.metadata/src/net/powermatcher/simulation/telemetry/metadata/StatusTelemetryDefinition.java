package net.powermatcher.simulation.telemetry.metadata;

import java.util.Arrays;

public class StatusTelemetryDefinition extends TelemetryDefinition {
	private String[] options;

	public StatusTelemetryDefinition() {
	}

	public StatusTelemetryDefinition(String key, String description) {
		super(key, description);
	}

	public StatusTelemetryDefinition(String key, String description, String... options) {
		super(key, description);
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(options);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatusTelemetryDefinition other = (StatusTelemetryDefinition) obj;
		if (!Arrays.equals(options, other.options))
			return false;
		return true;
	}
}
