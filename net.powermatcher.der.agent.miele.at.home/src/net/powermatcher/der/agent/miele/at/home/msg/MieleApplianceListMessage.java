package net.powermatcher.der.agent.miele.at.home.msg;


import java.util.ArrayList;
import java.util.List;

/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleApplianceListMessage extends MieleGatewayMessage {

	List<MieleApplianceInfoMessage> appliances;

	/**
	 * @param appliance
	 */
	public void addAppliance(final MieleApplianceInfoMessage appliance) {
		if (this.appliances == null) {
			this.appliances = new ArrayList<MieleApplianceInfoMessage>();
		}
		this.appliances.add(appliance);
	}

	/**
	 * @return TODO
	 */
	public List<MieleApplianceInfoMessage> getAppliances() {
		return this.appliances;
	}

	/**
	 * @param appliances
	 */
	public void setAppliances(final List<MieleApplianceInfoMessage> appliances) {
		this.appliances = appliances;
	}
}
