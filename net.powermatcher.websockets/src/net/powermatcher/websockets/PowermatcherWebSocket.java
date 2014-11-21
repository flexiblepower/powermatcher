package net.powermatcher.websockets;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.websockets.data.BidModel;
import net.powermatcher.websockets.data.MarketBasisModel;
import net.powermatcher.websockets.data.PricePointModel;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.google.gson.Gson;

@WebSocket
@Component(immediate = true)
public class PowermatcherWebSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowermatcherWebSocket.class);

	private static final Map<String, AgentEndpointProxy> AGENT_ENDPOINT_PROXIES = 
			Collections.synchronizedMap(new HashMap<String, AgentEndpointProxy>());

	private static final Map<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxy> REMOTE_LOCAL_LINK = 
			Collections.synchronizedMap(new HashMap<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxy>());
	
	private String agentId = "agentendpointproxy";
	private String matcherEndpointProxyId = "matcherendpointproxy";
	
	@Deactivate
	public synchronized void deactivate() {
		// Disconnect every connected agent
		for(Entry<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxy> link : REMOTE_LOCAL_LINK.entrySet()) {
			link.getKey().close();
		}
	}
	
	@Reference(dynamic = true, multiple = true, optional = true)
	public synchronized void addProxy(AgentEndpointProxy proxy) {
		LOGGER.info("Registered AgentEndpointProxy: [{}]", proxy.getAgentId());
    	AGENT_ENDPOINT_PROXIES.put(proxy.getAgentId(), proxy);
	}
    
	public synchronized void removeProxy(AgentEndpointProxy proxy) {
		LOGGER.info("Deregistered AgentEndpointProxy: [{}]", proxy.getAgentId());

		// TODO break connection with websocket?
    	AGENT_ENDPOINT_PROXIES.remove(proxy.getAgentId());
	}
    
	@OnWebSocketConnect
	public synchronized void onOpen(final org.eclipse.jetty.websocket.api.Session remoteSession) {
		// TODO how to handle desiredParentId?? maybe using querystring param during connect?
		// TODO add identification in querystring
		
		// Search for existing agentEndpointProxy
		if (!AGENT_ENDPOINT_PROXIES.containsKey(agentId)) {
			// TODO (fase 2) Create new agentRoleProxy, supplying websocketSession.

			// TODO throw correct exception to deny connection
			LOGGER.info("Rejecting connection from remote agent [{}] for non-existing local agent: [{}]", this.matcherEndpointProxyId, this.agentId);
			
			remoteSession.close();
			return;
		}

		// Associate session with agentendpoint proxy
		AgentEndpointProxy proxy = AGENT_ENDPOINT_PROXIES.get(agentId);
		try {
			proxy.remoteAgentConnected(remoteSession);
		} catch (OperationNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Store remote session with local proxy
		REMOTE_LOCAL_LINK.put(remoteSession, proxy);
		
		// this.remoteSessions.add(remoteSession);
		
		// TODO send marketbasis and clusterid back to remote agent as a response to connect?

		LOGGER.info("Remote agent [{}] connected to local agent [{}]", this.matcherEndpointProxyId, this.agentId);
	}

	@OnWebSocketClose
	public synchronized void onClose(final org.eclipse.jetty.websocket.api.Session session, int statusCode, String reason) {
		// this.remoteSessions.remove(session);
		
		// Find existing session
		if (!REMOTE_LOCAL_LINK.containsKey(session)) {
			LOGGER.warn("Received disconnect for non existing session.");
			
			// TODO validate this check whether it's correct.
			return;
		}

		// Remove remote session from agent proxy
		AgentEndpointProxy proxy = REMOTE_LOCAL_LINK.get(session);
		LOGGER.info("Agent disconnect detected remote agent: [{}], local agent", proxy.getMatcherEndpointProxyId(), proxy.getAgentId());

		proxy.remoteAgentDisconnected();

		// Remove session and proxy association
		REMOTE_LOCAL_LINK.remove(session);
		
		// TODO disconnect local agent as well 
		// TODO and destroy it?
	}

	@OnWebSocketMessage
 	public void onMessage(org.eclipse.jetty.websocket.api.Session session, String message) {
		// Find existing session
		if (!REMOTE_LOCAL_LINK.containsKey(session)) {
			LOGGER.warn("Received bid update for non existing session.");
			
			// TODO validate this check whether it's correct.
			return;
		}
		
		AgentEndpointProxy proxy = REMOTE_LOCAL_LINK.get(session);
		LOGGER.info("Received bid update from remote agent [{}] for local agent [{}]", proxy.getMatcherEndpointProxyId(), proxy.getAgentId());
		
		Gson gson = new Gson();
		BidModel newBidModel = gson.fromJson(message, BidModel.class);
		
		Bid newBid = null;
		
		// Caution, include either pricepoints or demand and not both.
		PricePointModel[] pricePointsModel  = newBidModel.getPricePoints();
		if (pricePointsModel == null || pricePointsModel.length == 0) {
			 newBid = new Bid(MarketBasisModel.fromMarketBasisModel(
						newBidModel.getMarketBasis()), 
						newBidModel.getBidNumber(), 
						newBidModel.getDemand());
		} else {
			// Convert price points
			PricePoint[] pricePoints = new PricePoint[pricePointsModel.length];
			for (int i = 0; i < pricePoints.length; i++) {
				pricePoints[i] = new PricePoint(); 
				pricePoints[i].setDemand(pricePointsModel[i].getDemand());
				pricePoints[i].setNormalizedPrice(pricePointsModel[i].getNormalizedPrice());
			}
			
			newBid = new Bid(MarketBasisModel.fromMarketBasisModel(
				newBidModel.getMarketBasis()), 
				newBidModel.getBidNumber(), 
				pricePoints);
		}

		// Relay bid update to local agent
		proxy.relayBid(newBid);
	}
}
