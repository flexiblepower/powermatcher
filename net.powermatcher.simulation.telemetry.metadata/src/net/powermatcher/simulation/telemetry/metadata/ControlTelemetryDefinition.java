package net.powermatcher.simulation.telemetry.metadata;

public class ControlTelemetryDefinition extends TelemetryDefinition {
	private String unit;

	public ControlTelemetryDefinition() {
	}

	public ControlTelemetryDefinition(String key, String description, String unit) {
		super(key, description);
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		ControlTelemetryDefinition other = (ControlTelemetryDefinition) obj;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

}
