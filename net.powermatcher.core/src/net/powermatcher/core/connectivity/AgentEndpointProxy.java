/*
package net.powermatcher.core.connectivity;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = AgentEndpointProxy.Config.class, immediate = true, 
	provide = { ObservableAgent.class, AgentRole.class })
public class AgentEndpointProxy extends BaseAgent implements AgentRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEndpointProxy.class);

	private javax.websocket.Session remoteSession;
	
	private Session localSession;

	@Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy")
        String matcherEndpointProxy();
    }

	public void remoteAgentConnected(javax.websocket.Session remoteSession) 
			throws OperationNotSupportedException {
		if (this.remoteSession != null && this.remoteSession.isOpen()) {
			throw new OperationNotSupportedException("Remote Agent already connected.");
		}
		
		this.remoteSession = remoteSession;
	}
	
	public void remoteAgentDisconnected() {
		this.remoteSession = null;
	}
	
	public void relayBid(Bid newBid) {
		if (this.localSession == null) {
			// TODO check local connected session.
			return;
		}

		this.localSession.updateBid(newBid);
	}
	
	@Override
	public void connectToMatcher(Session session) {
		this.localSession = localSession;
	}

	@Override
	public void matcherRoleDisconnected(Session session) {
		this.localSession = null;
		// TODO disconnect remote associated agent.
	    // this.remoteSessions.remove(session);
	}

	@Override
	public void updatePrice(Price newPrice) {
		// TODO relay new price to remote agent
		
		// TODO this.remoteSession.getBasicRemote().sendObject(newPrice);
	}
}
*/