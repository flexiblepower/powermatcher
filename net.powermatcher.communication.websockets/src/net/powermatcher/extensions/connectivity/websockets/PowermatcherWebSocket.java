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

/**
 * Receiving end of the WebSocket implementation for PowerMatcher.
 *
 * @author FAN
 * @version 2.0
 */
@WebSocket
@Component(immediate = true)
public class PowermatcherWebSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowermatcherWebSocket.class);

    private static final Map<String, AgentEndpointProxyWebsocket> AGENT_ENDPOINT_PROXIES = Collections.synchronizedMap(new HashMap<String, AgentEndpointProxyWebsocket>());

    private static final Map<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket> REMOTE_LOCAL_LINK = Collections.synchronizedMap(new HashMap<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket>());

    private String desiredConnectionId;
    private String remoteMatcherEndpointId;

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public synchronized void deactivate() {
        // Disconnect every connected agent
        for (Entry<org.eclipse.jetty.websocket.api.Session, AgentEndpointProxyWebsocket> link : REMOTE_LOCAL_LINK.entrySet()) {
            link.getKey().close();
        }
    }

    /**
     * Register {@link AgentEndpointProxyWebsocket} within the OSGI runtime.
     *
     * @param proxy
     */
    @Reference(dynamic = true, multiple = true, optional = true)
    public synchronized void addProxy(AgentEndpointProxyWebsocket proxy) {
        LOGGER.info("Registered AgentEndpointProxy: [{}]", proxy.getAgentId());
        AGENT_ENDPOINT_PROXIES.put(proxy.getAgentId(), proxy);
    }

    /**
     * Deregister {@link AgentEndpointProxyWebsocket} within the OSGI runtime.
     *
     * @param proxy
     */
    public synchronized void removeProxy(AgentEndpointProxyWebsocket proxy) {
        LOGGER.info("Deregistered AgentEndpointProxy: [{}]", proxy.getAgentId());

        // Remote agent proxy from the list, sessions will be closed via normal
        // websocket close.
        AGENT_ENDPOINT_PROXIES.remove(proxy.getAgentId());
    }

    /**
     * Handle a new incoming WebSocket connection.
     *
     * @param remoteSession
     *            the remote WebSocket session which wants to connect.
     */
    @OnWebSocketConnect
    public synchronized void onOpen(final org.eclipse.jetty.websocket.api.Session remoteSession) {
        Map<String, String> queryString = null;
        try {
            queryString = splitQuery(remoteSession.getUpgradeRequest().getRequestURI());
        } catch (UnsupportedEncodingException e1) {
            remoteSession.close();
            LOGGER.warn("Rejecting connection from remote agent [{}], URL is not complete (missing querystring)",
                        remoteMatcherEndpointId);
            return;
        }

        // Read desired connection from the querystring
        desiredConnectionId = queryString.get("desiredConnectionId");
        if (desiredConnectionId == null || desiredConnectionId.length() == 0) {
            remoteSession.close();
            LOGGER.warn("Rejecting connection from remote agent [{}], desiredConnectionId is missing from querystring",
                        remoteMatcherEndpointId);
            return;
        }

        // Read desired agentId from the querystring
        remoteMatcherEndpointId = queryString.get("agentId");
        if (remoteMatcherEndpointId == null || remoteMatcherEndpointId.length() == 0) {
            remoteSession.close();
            LOGGER.warn("Rejecting connection from remote agent [{}], agentId is missing from querystring",
                        remoteMatcherEndpointId);
            return;
        }

        // Search for existing agentEndpointProxy, in later stage automatic
        // creation of agents proxies could be
        // implemented
        if (!AGENT_ENDPOINT_PROXIES.containsKey(desiredConnectionId)) {
            LOGGER.warn("Rejecting connection from remote agent [{}] for non-existing local agent: [{}]",
                        remoteMatcherEndpointId, desiredConnectionId);

            remoteSession.close();
            return;
        }

        // Associate session with agentendpoint proxy
        AgentEndpointProxyWebsocket proxy = AGENT_ENDPOINT_PROXIES.get(desiredConnectionId);
        try {
            proxy.remoteAgentConnected(remoteSession);
        } catch (OperationNotSupportedException e) {
            LOGGER.warn("Rejecting connection from remote agent [{}], reason: [{}]", remoteMatcherEndpointId, e);
            remoteSession.close();
        }

        // Store remote session with local proxy
        REMOTE_LOCAL_LINK.put(remoteSession, proxy);

        LOGGER.info("Remote agent [{}] connected to local agent [{}]", remoteMatcherEndpointId,
                    desiredConnectionId);
    }

    /**
     * Handle WebSocket connection which disconnected.
     *
     * @param remoteSession
     *            the remote WebSocket session which disconnected.
     * @param statusCode
     *            the statusCode indicating the reason.
     * @param reason
     *            the reason providing more information about disconnect.
     */
    @OnWebSocketClose
    public synchronized void onClose(final org.eclipse.jetty.websocket.api.Session remoteSession, int statusCode,
                                     String reason) {
        // Find existing session
        if (!REMOTE_LOCAL_LINK.containsKey(remoteSession)) {
            LOGGER.warn("Received disconnect for non existing session.");
            return;
        }

        // Remove remote session from agent proxy and local session collection
        AgentEndpointProxyWebsocket proxy = REMOTE_LOCAL_LINK.remove(remoteSession);
        LOGGER.info("Agent disconnect detected remote agent: [{}], local agent", proxy.getMatcherEndpointProxyId(),
                    proxy.getAgentId());
        proxy.remoteAgentDisconnected();
    }

    /**
     * Handle an incoming message from a remote WebSocket (agent).
     *
     * @param remoteSession
     *            the session which sent the message.
     * @param message
     *            the message containing a JSON string with PmMessage.
     */
    @OnWebSocketMessage
    public void onMessage(org.eclipse.jetty.websocket.api.Session remoteSession, String message) {
        // Find existing session
        if (!REMOTE_LOCAL_LINK.containsKey(remoteSession)) {
            LOGGER.warn("Received bid update for non existing session.");
            return;
        }

        // Find associated local agentproxy
        AgentEndpointProxyWebsocket proxy = REMOTE_LOCAL_LINK.get(remoteSession);
        LOGGER.info("Received bid update from remote agent [{}] for local agent [{}]",
                    proxy.getMatcherEndpointProxyId(), proxy.getAgentId());

        // Decode the JSON data
        PmJsonSerializer serializer = new PmJsonSerializer();
        PmMessage pmMessage = serializer.deserialize(message);
        Bid newBid = ModelMapper.mapBid((BidModel) pmMessage.getPayload());

        // Relay bid update to local agent
        proxy.updateLocalBid(newBid);
    }

    /**
     * Get the queryparams from the URL used to connect.
     *
     * @param url
     *            the URL
     * @return the key value pairs of the queryparams.
     * @throws UnsupportedEncodingException
     *             when URL is incorrect.
     */
    private static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
