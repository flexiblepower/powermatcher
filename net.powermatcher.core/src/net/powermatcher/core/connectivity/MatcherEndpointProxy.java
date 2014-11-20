/*package net.powermatcher.core.connectivity;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.WebSocketContainer;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Meta;

@ClientEndpoint()
@Component(designateFactory = AgentEndpointProxy.Config.class, immediate = true, provide = { ObservableAgent.class,
    MatcherRole.class })
public class MatcherEndpointProxy extends BaseAgent implements MatcherRole {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherEndpointProxy.class);

	@Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy")
        String matcherEndpointProxy();
    }
    
    // TODO make this configurable
    private final String uri = "ws://localhost:8080/powermatcher/agentroleservice"; 
	
	private WebSocketContainer container;

    private Session localSession;
	private javax.websocket.Session remoteSession;
	
	@Activate
	public void activate() {
        this.container = ContainerProvider.getWebSocketContainer();
        connect();
	}

	private void connect() {

		// Try to connect to the remote agent and retrieve required data
		try {
			this.remoteSession = container.connectToServer(MatcherEndpointProxy.class, URI.create(uri));
		} catch (DeploymentException | IOException e) {
			LOGGER.error("Unable to connect to remote agent. Reason {}", e);
			
			// TODO add error behavior for connection failures
//			return false;
		}
		
		// TODO Wait for marketbasis response?
		// TODO Synchronization
	}
	
	@Deactivate
	public void deactivate() {
		// TODO ( ( ClientContainer )container ).stop();?
		this.container = null;		
	}
	
	@Override
	public boolean connectToAgent(Session session) {
        // TODO how to receive remote marketBasis? session.setMarketBasis(marketBasis); -> response of server after connect
		// TODO how to receive remote clusterId? session.setClusterId(this.getClusterId()); -> response of server after connect
		// TODO how to handle local / remote agentId?
		// TODO how to handle local / remote desiredParentId?
		// TODO maybe via querystring during connection?
		// TODO how to handle security (HTTPS and identity)?

		
		// Check status of remote agent connection
		if (this.remoteSession == null || !this.remoteSession.isOpen()) {
	        LOGGER.warn("Remote agent is not connected, unable to connect local agent with session [{}]", session.getSessionId());
			return false;
		}
		
        this.localSession = session;
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
		
		return true;
	}

	@Override
	public void agentRoleDisconnected(Session session) {
		// TODO is this correct approach?

		// Disconnect remote and local agent, keep remote connection open.
		try {
			if (this.remoteSession != null && this.remoteSession.isOpen()) {
				this.remoteSession.close();
				this.remoteSession = null;
			}
		} catch (IOException e) {
	        LOGGER.warn("Unable to disconnect remote agent is not connected, unable to connect local agent with session [{}]", session.getSessionId());
		}

		this.localSession = null;
        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
	}

	@Override
	public void updateBid(Session session, Bid newBid) {
		if (this.localSession != session) {
			LOGGER.warn("Received bid update for unknown session.");

			// TODO throw correct exception
			return;
		}
		
		// Relay bid to remote agent
		try {
			this.remoteSession.getBasicRemote().sendObject(newBid);
		} catch (IOException | EncodeException e) {
			LOGGER.error("Unable to send new bid to remote agent. Reason {}", e);
			
			// TODO throw correct exception
		}		
	}
	
	@OnMessage
	public void onMessage(Price newPrice) {
		LOGGER.info("Received price update from remote agent {}", newPrice);
		
		// Relay price update to local agent
		this.localSession.updatePrice(newPrice);
	}
}
*/