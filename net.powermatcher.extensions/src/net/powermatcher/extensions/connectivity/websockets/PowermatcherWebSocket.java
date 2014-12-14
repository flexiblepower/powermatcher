package net.powermatcher.extensions.connectivity.websockets;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.extensions.connectivity.websockets.data.BidModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage;
import net.powermatcher.extensions.connectivity.websockets.json.ModelMapper;
import net.powermatcher.extensions.connectivity.websockets.json.PmJsonSerializer;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@WebSocket
@Component(immediate = true)
public class PowermatcherWebSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowermatcherWebSocket.class);

	private static final Map<String, AgentEndpointProxyWebsocket> AGENT_ENDPOINT_PROXIES = 
			Collections.synchronizedMap(new HashMap<String, AgentEndpointProxyWebsocket>());

	private static final Map<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket> REMOTE_LOCAL_LINK = 
			Collections.synchronizedMap(new HashMap<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket>());
	
	private String desiredConnectionId;
	private String remoteMatcherEndpointId;
	
	@Deactivate
	public synchronized void deactivate() {
		// Disconnect every connected agent
		for(Entry<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket> link : REMOTE_LOCAL_LINK.entrySet()) {
			link.getKey().close();
		}
	}
	
	@Reference(dynamic = true, multiple = true, optional = true)
	public synchronized void addProxy(AgentEndpointProxyWebsocket proxy) {
		LOGGER.info("Registered AgentEndpointProxy: [{}]", proxy.getAgentId());
    	AGENT_ENDPOINT_PROXIES.put(proxy.getAgentId(), proxy);
	}
    
	public synchronized void removeProxy(AgentEndpointProxyWebsocket proxy) {
		LOGGER.info("Deregistered AgentEndpointProxy: [{}]", proxy.getAgentId());
		
		// Remote agent proxy from the list, sessions will be closed via normal websocket close.
    	AGENT_ENDPOINT_PROXIES.remove(proxy.getAgentId());
	}
    
	@OnWebSocketConnect
	public synchronized void onOpen(final org.eclipse.jetty.websocket.api.Session remoteSession) {
		Map<String, String> queryString = null;
		try {
			 queryString = splitQuery(remoteSession.getUpgradeRequest().getRequestURI());
		} catch (UnsupportedEncodingException e1) {
			remoteSession.close();
			LOGGER.warn("Rejecting connection from remote agent [{}], URL is not complete (missing querystring)", this.remoteMatcherEndpointId);
			return;
		}
		
		// Read desired connection from the querystring
		this.desiredConnectionId = queryString.get("desiredConnectionId");
		if (this.desiredConnectionId == null || this.desiredConnectionId.length() == 0) {
			remoteSession.close();
			LOGGER.warn("Rejecting connection from remote agent [{}], desiredConnectionId is missing from querystring", this.remoteMatcherEndpointId);
			return;
		}
		
		// Read desired agentId from the querystring
		this.remoteMatcherEndpointId = queryString.get("agentId");
		if (this.remoteMatcherEndpointId == null || this.remoteMatcherEndpointId.length() == 0) {
			remoteSession.close();
			LOGGER.warn("Rejecting connection from remote agent [{}], agentId is missing from querystring", this.remoteMatcherEndpointId);
			return;
		}
		
		// Search for existing agentEndpointProxy, in later stage automatic creation of agents proxies could be implemented 
		if (!AGENT_ENDPOINT_PROXIES.containsKey(desiredConnectionId)) {
			LOGGER.warn("Rejecting connection from remote agent [{}] for non-existing local agent: [{}]", this.remoteMatcherEndpointId, this.desiredConnectionId);
			
			remoteSession.close();
			return;
		}

		// Associate session with agentendpoint proxy
		AgentEndpointProxyWebsocket proxy = AGENT_ENDPOINT_PROXIES.get(desiredConnectionId);
		try {
			proxy.remoteAgentConnected(remoteSession);
		} catch (OperationNotSupportedException e) {
			LOGGER.warn("Rejecting connection from remote agent [{}], reason: [{}]", this.remoteMatcherEndpointId, e);
			remoteSession.close();
		}

		// Store remote session with local proxy
		REMOTE_LOCAL_LINK.put(remoteSession, proxy);
		
		LOGGER.info("Remote agent [{}] connected to local agent [{}]", this.remoteMatcherEndpointId, this.desiredConnectionId);
	}

	@OnWebSocketClose
	public synchronized void onClose(final org.eclipse.jetty.websocket.api.Session session, int statusCode, String reason) {
		// Find existing session
		if (!REMOTE_LOCAL_LINK.containsKey(session)) {
			LOGGER.warn("Received disconnect for non existing session.");
			return;
		}

		// Remove remote session from agent proxy and local session collection
		AgentEndpointProxyWebsocket proxy = REMOTE_LOCAL_LINK.remove(session);
		LOGGER.info("Agent disconnect detected remote agent: [{}], local agent", proxy.getMatcherEndpointProxyId(), proxy.getAgentId());
		proxy.remoteAgentDisconnected();
	}

	@OnWebSocketMessage
 	public void onMessage(org.eclipse.jetty.websocket.api.Session session, String message) {
		// Find existing session
		if (!REMOTE_LOCAL_LINK.containsKey(session)) {
			LOGGER.warn("Received bid update for non existing session.");
			return;
		}
		
		// Find associated local agentproxy
		AgentEndpointProxyWebsocket proxy = REMOTE_LOCAL_LINK.get(session);
		LOGGER.info("Received bid update from remote agent [{}] for local agent [{}]", proxy.getMatcherEndpointProxyId(), proxy.getAgentId());
		
		// Decode the JSON data
		PmJsonSerializer serializer = new PmJsonSerializer();
		PmMessage pmMessage = serializer.deserialize(message);
		Bid newBid = ModelMapper.mapBid((BidModel)pmMessage.getPayload());
		
		// Relay bid update to local agent
		proxy.updateLocalBid(newBid);
	}

	private static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String query = url.getQuery();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
}
