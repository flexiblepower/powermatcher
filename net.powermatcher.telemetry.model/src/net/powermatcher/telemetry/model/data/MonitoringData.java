package net.powermatcher.telemetry.model.data;

import java.util.Date;

public class MonitoringData {

	private String clusterId;
	private String configurationItem;
	private String configurationItemName;
	private String componentName;
	private String serverName;
	private String status;
	private Date statusDate;
	private Character severity;
	
	
	/**
	 * @param clusterId
	 * @param configurationItem
	 * @param configurationItemName
	 * @param componentName
	 * @param serverName
	 * @param status
	 * @param statusDate
	 * @param severity
	 */
	public MonitoringData(String clusterId, String configurationItem,
			String configurationItemName, String componentName,
			String serverName, String status, Date statusDate,
			Character severity) {
		super();
		this.clusterId = clusterId;
		this.configurationItem = configurationItem;
		this.configurationItemName = configurationItemName;
		this.componentName = componentName;
		this.serverName = serverName;
		this.status = status;
		this.statusDate = statusDate;
		this.severity = severity;
	}
	
	/**
	 * @return the cluster ID
	 */
	public String getClusterId() {
		return clusterId;
	}
	/**
	 * @param clusterId the cluster ID to set
	 */
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	/**
	 * @return the configurationItem
	 */
	public String getConfigurationItem() {
		return configurationItem;
	}
	/**
	 * @param configurationItem the configurationItem to set
	 */
	public void setConfigurationItem(String configurationItem) {
		this.configurationItem = configurationItem;
	}
	/**
	 * @return the configurationItemName
	 */
	public String getConfigurationItemName() {
		return configurationItemName;
	}
	/**
	 * @param configurationItemName the configurationItemName to set
	 */
	public void setConfigurationItemName(String configurationItemName) {
		this.configurationItemName = configurationItemName;
	}
	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return componentName;
	}
	/**
	 * @param componentName the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}
	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the statusDate
	 */
	public Date getStatusDate() {
		return statusDate;
	}
	/**
	 * @param statusDate the statusDate to set
	 */
	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}
	/**
	 * @return the severity
	 */
	public Character getSeverity() {
		return severity;
	}
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(Character severity) {
		this.severity = severity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.status);
		sb.append(',');
		sb.append(this.severity);
		sb.append(',');
		sb.append(this.configurationItem);
		sb.append(',');
		sb.append(this.configurationItemName);
		sb.append(',');
		sb.append(this.componentName);
		sb.append(',');
		sb.append(this.serverName);
		sb.append(',');
		sb.append(this.statusDate);
		
		return sb.toString();
	}
}
