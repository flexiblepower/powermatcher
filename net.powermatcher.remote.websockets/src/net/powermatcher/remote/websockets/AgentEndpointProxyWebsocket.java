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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

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
           provide = { ObservableAgent.class, AgentEndpointProxyWebsocket.class })
public class AgentEndpointProxyWebsocket
    extends BaseAgentEndpoint {

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator",
                 description = "The agent identifier of the parent matcher to which this agent should be connected ")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy", description = "The unique identifier of the agent")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy", description = "Remote matcher endpoint proxy")
        String remoteAgentEndpointId();
    }

    private org.eclipse.jetty.websocket.api.Session remoteSession;

    private String remoteAgentEndpointId;

    private BundleContext bundleContext;

    private ServiceRegistration<AgentEndpoint> agentEndpointServiceRegistration;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(BundleContext bundleContext, Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        init(config.agentId(), config.desiredParentId());
        remoteAgentEndpointId = config.remoteAgentEndpointId();

        this.bundleContext = bundleContext;
        agentEndpointServiceRegistration = null;
    }

    /**
     * OSGi calls this method to delete a managed service.
     */
    @Deactivate
    public void deactivated() {
        if (isRemoteConnected()) {
            remoteSession.close();
        }

        unregisterAgentEndpoint();

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

        // Register the AgentEndpoint with the OSGI runtime, to make it available for connections
        registerAgentEndpoint();
        remoteSession = session;

        // Notify the remote agent about the cluster
        sendCusterInformation();
    }

    public void remoteAgentDisconnected() {
        remoteSession = null;

        // Remove the AgentEndpoint with the OSGI runtime, to disable connections locally
        unregisterAgentEndpoint();
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
            LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
        }
    }

    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        super.handlePriceUpdate(priceUpdate);
        if (isRemoteConnected()) {
            updateRemotePrice(priceUpdate);
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

    public void updateLocalBid(BidUpdate bidUpdate) {
        getSession().updateBid(bidUpdate);
    }

    private void registerAgentEndpoint() {
        if (agentEndpointServiceRegistration == null) {
            agentEndpointServiceRegistration = bundleContext.registerService(AgentEndpoint.class, this, null);
        }
    }

    private void unregisterAgentEndpoint() {
        if (agentEndpointServiceRegistration != null) {
            agentEndpointServiceRegistration.unregister();
            agentEndpointServiceRegistration = null;
        }
    }
}
