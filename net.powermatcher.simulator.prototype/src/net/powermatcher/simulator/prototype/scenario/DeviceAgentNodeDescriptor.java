package net.powermatcher.simulator.prototype.scenario;

public class DeviceAgentNodeDescriptor extends NodeDescriptor {
	private String resourceManagerComponentName;
	private String resourceModelComponentName;

	public DeviceAgentNodeDescriptor(String agentComponentName, String resourceManagerComponentName,
			String resourceModelComponentName) {
		super(agentComponentName);
		this.resourceManagerComponentName = resourceManagerComponentName;
		this.resourceModelComponentName = resourceModelComponentName;
	}

	public String getResourceManagerComponentName() {
		return resourceManagerComponentName;
	}

	public String getResourceModelComponentName() {
		return resourceModelComponentName;
	}
}
