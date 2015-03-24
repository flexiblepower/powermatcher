package net.powermatcher.remote.websockets.server;

import java.io.IOException;
import java.util.Hashtable;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgentEndpoint;
import net.powermatcher.remote.websockets.data.BidModel;
import net.powermatcher.remote.websockets.data.PmMessage;
import net.powermatcher.remote.websockets.json.ModelMapper;
import net.powermatcher.remote.websockets.json.PmJsonSerializer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * WebSocket implementation of an {@link AgentEndpointProxy}. Enabled two agents to communicate via WebSockets and JSON
 * over a TCP connection.
 *
 * @author FAN
 * @version 2.0
 */
public class AgentEndpointProxy
    extends BaseAgentEndpoint
    implements WebSocketListener {

    private final BundleContext bundleContext;
    private final String desiredParentId;
    private ServiceRegistration<?> serviceRegistration;
    private Session remoteSession;

    public AgentEndpointProxy(BundleContext bundleContext, String desiredParentId) {
        this.bundleContext = bundleContext;
        this.desiredParentId = desiredParentId;
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && remoteSession != null && remoteSession.isOpen();
    }

    @Override
    public void onWebSocketConnect(Session remoteSession) {
        this.remoteSession = remoteSession;

        String agentId = "websocket-" + remoteSession.getRemoteAddress() + "-agent";
        this.init(agentId, desiredParentId);

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("agentId", agentId);
        properties.put("desiredParentId", desiredParentId);
        serviceRegistration = bundleContext.registerService(new String[] { ObservableAgent.class.getName(),
                                                                          AgentEndpoint.class.getName() },
                                                            this,
                                                            null);
    }

    @Override
    public void onWebSocketBinary(byte[] buffer, int offset, int length) {
        // Do nothing, we ignore all binary messages
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        deactivate();
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

        // Relay bid update to local agent
        if (isConnected()) {
            getSession().updateBid(newBid);
        }
    }

    @Override
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
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

        try {
            // Create price update message
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializePriceUpdate(priceUpdate);
            remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
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
        try {
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializeClusterInfo(getClusterId(), getMarketBasis());
            remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    @Override
    public synchronized void matcherEndpointDisconnected(net.powermatcher.api.Session session) {
        super.matcherEndpointDisconnected(session);
        deactivate();
    }
}
