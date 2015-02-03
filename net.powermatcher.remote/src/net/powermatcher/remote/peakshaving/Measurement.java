package net.powermatcher.remote.peakshaving;

public class Measurement {
    private String agentId;

    private double measurement;

    public Measurement() {
    }

    public Measurement(String agentId, double measurement) {
        this.agentId = agentId;
        this.measurement = measurement;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setMeasurement(double measurement) {
        this.measurement = measurement;
    }

    public String getAgentId() {
        return agentId;
    }

    public double getMeasurement() {
        return measurement;
    }

    @Override
    public String toString() {
        return "Measurement [agentId=" + agentId + ", measurement=" + measurement + "]";
    }
}
