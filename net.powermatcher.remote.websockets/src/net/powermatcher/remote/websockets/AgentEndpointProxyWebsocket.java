package net.powermatcher.remote.websockets;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
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
           provide = { ObservableAgent.class, AgentEndpoint.class })
public class AgentEndpointProxyWebsocket
    extends BaseAgentEndpoint {

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator", description = "desired parent to connect to")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy", description = "local agent identification")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy", description = "Remote matcher endpoint proxy")
        String remoteAgentEndpointId();
    }

    private org.eclipse.jetty.websocket.api.Session remoteSession;

    private String remoteAgentEndpointId;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        activate(config.agentId(), config.desiredParentId());
        remoteAgentEndpointId = config.remoteAgentEndpointId();
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

    public String getRemoteAgentEndpointId() {
        return remoteAgentEndpointId;
    }

    public void
            remoteAgentConnected(org.eclipse.jetty.websocket.api.Session session)
                                                                                 throws OperationNotSupportedException {
        if (isRemoteConnected()) {
            throw new OperationNotSupportedException("Remote Agent already connected.");
        }

        remoteSession = session;

        // Notify the remote agent about the cluster
        sendCusterInformation();
    }

    public void remoteAgentDisconnected() {
        remoteSession = null;
    }

    /**
     * {@inheritDoc}
     *
     * This specific implementation checks the if the websocket is open.
     */
    public boolean isRemoteConnected() {
        return remoteSession != null && remoteSession.isOpen();
    }

    /**
     * {@inheritDoc}
     *
     * This specific implementation serializes the {@link PriceUpdate} to json and sends it through the websocket.
     */
    public void updateRemotePrice(PriceUpdate newPrice) {
        try {
            // Create price update message
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializePriceUpdate(newPrice);
            remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.warn(
                        "Unable to send price update to remote agent, reason {}", e);
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

    private void sendCusterInformation() {
        if (!isRemoteConnected() || !isInitialized()) {
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

    public void updateLocalBid(BidUpdate bidUpdate) {
        getSession().updateBid(bidUpdate);
    }
}
