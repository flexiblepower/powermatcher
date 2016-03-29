package net.powermatcher.remote.websockets.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.core.BaseAgentEndpoint;
import net.powermatcher.remote.websockets.data.BidModel;
import net.powermatcher.remote.websockets.data.PmMessage;
import net.powermatcher.remote.websockets.json.ModelMapper;
import net.powermatcher.remote.websockets.json.PmJsonSerializer;

/**
 * WebSocket implementation of an {@link AgentEndpoint}. Enabled two agents to communicate via WebSockets and JSON over
 * a TCP connection.
 *
 * @author FAN
 * @version 2.1
 */
public class AgentEndpointProxy
    extends BaseAgentEndpoint
    implements WebSocketListener {

    protected final BundleContext bundleContext;
    protected final String desiredParentId;
    protected ServiceRegistration<?> serviceRegistration;
    protected Session remoteSession;

    public AgentEndpointProxy(BundleContext bundleContext, String desiredParentId) {
        this.bundleContext = bundleContext;
        this.desiredParentId = desiredParentId;
    }

    @Override
    public void onWebSocketConnect(Session remoteSession) {
        this.remoteSession = remoteSession;

        Map<String, String> query = splitQuery(remoteSession.getUpgradeRequest().getRequestURI());
        String remoteAgentId = query.get("agentId");
        String connectionId = query.get("connectionId");
        if (remoteAgentId == null || remoteAgentId.isEmpty()) {
            remoteSession.close();
            LOGGER.warn("Rejecting connection from remote agent from [{}], missing the agentId",
                        remoteSession.getRemoteAddress());
            return;
        }

        register(remoteSession, remoteAgentId, connectionId);
        LOGGER.debug("Connected to remote agent {} with connectionId {} on {}",
                     remoteAgentId,
                     connectionId,
                     remoteSession.getRemoteAddress());
    }

    protected void register(Session remoteSession, String remoteAgentId, String connectionId) {
        String agentId = "remote-" + remoteSession.getRemoteAddress().getHostString() + "-" + remoteAgentId;
        init(agentId, desiredParentId);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("agentId", agentId);
        properties.put("desiredParentId", desiredParentId);
        properties.put("connectionId", connectionId);
        serviceRegistration = bundleContext.registerService(new String[] { ObservableAgent.class.getName(),
                                                                           AgentEndpoint.class.getName() },
                                                            this,
                                                            properties);
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
    protected static Map<String, String> splitQuery(URI url) {
        try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            String query = url.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                                URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            return query_pairs;
        } catch (UnsupportedEncodingException ex) {
            return Collections.emptyMap();
        }
    }

    @Override
    public void onWebSocketBinary(byte[] buffer, int offset, int length) {
        // Do nothing, we ignore all binary messages
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        deactivate();
        LOGGER.debug("Disconnected session [{}], code = {}, reason = {}", getAgentId(), statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable ex) {
        LOGGER.warn("Error during communication", ex);
    }

    @Override
    public void onWebSocketText(String message) {
        // Decode the JSON data
        PmJsonSerializer serializer = new PmJsonSerializer();
        PmMessage pmMessage = serializer.deserialize(message);
        BidUpdate newBid = ModelMapper.mapBidUpdate((BidModel) pmMessage.getPayload());

        AgentEndpoint.Status currentStatus = getStatus();
        if (currentStatus.isConnected()) {
            net.powermatcher.api.Session session = currentStatus.getSession();
            publishEvent(new OutgoingBidUpdateEvent(currentStatus.getClusterId(),
                                                    getAgentId(),
                                                    session.getSessionId(),
                                                    now(),
                                                    newBid));
            LOGGER.debug("Sending bid [{}] to {}", newBid, session.getAgentId());
            currentStatus.getSession().updateBid(newBid);
        } else {
            LOGGER.warn("Got a message, while not connected? {}", newBid);
        }
    }

    @Override
    public void deactivate() {
        if (serviceRegistration != null) {
            ServiceRegistration<?> reg = serviceRegistration;
            serviceRegistration = null;
            reg.unregister();
        }
        if (remoteSession != null && remoteSession.isOpen()) {
            remoteSession.close();
            remoteSession = null;
        }
        super.deactivate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        super.handlePriceUpdate(priceUpdate);

        // Create price update message
        PmJsonSerializer serializer = new PmJsonSerializer();
        String message = serializer.serializePriceUpdate(priceUpdate);

        try {
            remoteSession.getRemote().sendString(message);
        } catch (IOException | WebSocketException | NullPointerException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToMatcher(net.powermatcher.api.Session session) {
        super.connectToMatcher(session);

        // Local matcher is connected, provide cluster information to remote // agent.
        PmJsonSerializer serializer = new PmJsonSerializer();
        AgentEndpoint.Status currentStatus = getStatus();
        String message = serializer.serializeClusterInfo(currentStatus.getClusterId(), currentStatus.getMarketBasis());
        try {
            remoteSession.getRemote().sendString(message);
        } catch (IOException | WebSocketException | NullPointerException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    @Override
    public synchronized void matcherEndpointDisconnected(net.powermatcher.api.Session session) {
        super.matcherEndpointDisconnected(session);
        deactivate();
    }
}
