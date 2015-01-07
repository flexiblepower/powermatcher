package net.powermatcher.extensions.connectivity.websockets;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.connectivity.AgentEndpointProxy;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.connectivity.BaseAgentEndpointProxy;
import net.powermatcher.extensions.connectivity.websockets.json.PmJsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Component(designateFactory = AgentEndpointProxyWebsocket.Config.class, immediate = true, provide = {
        ObservableAgent.class, AgentEndpoint.class, AgentEndpointProxy.class, AgentEndpointProxyWebsocket.class })
public class AgentEndpointProxyWebsocket extends BaseAgentEndpointProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEndpointProxyWebsocket.class);

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

    /**
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.setDesiredParentId(config.desiredParentId());
        this.setAgentId(config.agentId());
        this.setMatcherEndpointProxyId(config.remoteAgentEndpointId());
    }

    /**
     * OSGi calls this method to delete a managed service.
     */
    @Deactivate
    public void deactivated() {
        if (this.isRemoteConnected()) {
            this.remoteSession.close();
        }
    }

    public void remoteAgentConnected(org.eclipse.jetty.websocket.api.Session session)
            throws OperationNotSupportedException {
        if (this.isRemoteConnected()) {
            throw new OperationNotSupportedException("Remote Agent already connected.");
        }

        this.remoteSession = session;

        // Notify the remote agent about the cluster
        sendCusterInformation();
    }

    public void remoteAgentDisconnected() {
        this.remoteSession = null;
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation checks the if the websocket is open.
     */
    @Override
    public boolean isRemoteConnected() {
        return this.remoteSession != null && this.remoteSession.isOpen();
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation serializes the {@link PriceUpdate} to json and sends it through the websocket.
     */
    @Override
    public void updateRemotePrice(PriceUpdate newPrice) {
        try {
            // Create price update message
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializePriceUpdate(newPrice);
            this.remoteSession.getRemote().sendString(message);
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

        // Local matcher is connected, provide cluster information to remote agent.
        sendCusterInformation();
    }

    private void sendCusterInformation() {
        if (!isRemoteConnected() || this.getLocalMarketBasis() == null) {
            // Skip sending information
            return;
        }

        try {
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializeClusterInfo(this.getClusterId(), this.getLocalMarketBasis());
            this.remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }
}
