package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "cluster")
public class ClusterDescriptor extends ConfigurationElementImpl<AuctioneerNodeDescriptor> {
	private String clusterId;

	public void setParent(ScenarioDescriptor scenario) {
		this.setParent(scenario);
	}

	@Override
	@XmlTransient
	public ScenarioDescriptor getParent() {
		return (ScenarioDescriptor) super.getParent();
	}

	@XmlAttribute(name = "id")
	public String getClusterId() {
		return clusterId;
	}

	@XmlElement
	public AuctioneerNodeDescriptor getRoot() {
		return (hasChildren() ? getChildren().get(0) : null);
	}

	public boolean removeRoot(AuctioneerNodeDescriptor root) {
		return super.removeChild(this.getRoot());
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	@Override
	public boolean addChild(AuctioneerNodeDescriptor child) {
		if (this.hasChildren()) {
			return false;
		}

		return super.addChild(child);
	}

	public void setRoot(AuctioneerNodeDescriptor root) {
		this.addChild(root);
	}
}
