package net.powermatcher.remote.websockets;

import java.io.IOException;
import java.util.Map;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgentEndpoint;
import net.powermatcher.remote.websockets.json.PmJsonSerializer;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * WebSocket implementation of an {@link AgentEndpointProxy}. Enabled two agents to communicate via WebSockets and JSON
 * over a TCP connection.
 * 
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = AgentEndpointProxyWebsocket.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, AgentEndpointProxyWebsocket.class, AgentEndpoint.class })
public class AgentEndpointProxyWebsocket
    extends BaseAgentEndpoint {

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator",
                 description = "The agent identifier of the parent matcher to which this agent should be connected ")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy", description = "The unique identifier of the agent")
        String agentId();
    }

    private org.eclipse.jetty.websocket.api.Session remoteSession;

    /**
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        init(config.agentId(), config.desiredParentId());
    }

    /**
     * OSGi calls this method to delete a managed service.
     */
    @Deactivate
    public void deactivated() {
        if (isRemoteConnected()) {
            remoteSession.close();
        }

        super.deactivate();
    }

    /**
     * Remote matcherProxy has connected, send cluster information (if available).
     * 
     * @param session
     *            remote session created by MatcherProxy
     */
    public void
            remoteMatcherProxyConnected(org.eclipse.jetty.websocket.api.Session session) {
        remoteSession = session;

        // Notify the remote agent about the cluster
        sendCusterInformation();
    }

    /**
     * Remote matcherProxy has disconnected.
     */
    public void remoteMatcherProxyDisconnected() {
        remoteSession = null;
    }

    /**
     * Checks whether the websocket is open.
     */
    public boolean isRemoteConnected() {
        return remoteSession != null && remoteSession.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        super.handlePriceUpdate(priceUpdate);
        if (isRemoteConnected()) {
            updateRemotePrice(priceUpdate);
        }
    }

    /**
     * Serializes the {@link PriceUpdate} to json and sends it through the websocket to remote agent
     */
    private void updateRemotePrice(PriceUpdate newPrice) {
        try {
            // Create price update message
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializePriceUpdate(newPrice);
            remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToMatcher(Session session) {
        super.connectToMatcher(session);

        // Local matcher is connected, provide cluster information to remote // agent.
        sendCusterInformation();
    }

    @Override
    public synchronized void matcherEndpointDisconnected(Session session) {
        super.matcherEndpointDisconnected(session);

        try {
            if (remoteSession != null) {
                remoteSession.disconnect();
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to disconnect remote session, reason {}", e);
        }
    }

    /**
     * Send cluster information containing Cluster Id and {@link MarketBasis}
     */
    private void sendCusterInformation() {
        if (!isRemoteConnected() || !isConnected()) {
            // Skip sending information
            return;
        }

        try {
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializeClusterInfo(getClusterId(), getMarketBasis());
            remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    /**
     * Update the received BidUpdate from MatcherProxy to local MatcherEndpoint.
     * 
     * @param bidUpdate
     */
    public void updateLocalBid(BidUpdate bidUpdate) {
        if (isConnected()) {
            getSession().updateBid(bidUpdate);
        }
    }
}
