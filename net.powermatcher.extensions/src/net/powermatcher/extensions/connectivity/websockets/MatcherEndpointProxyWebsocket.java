package net.powermatcher.extensions.connectivity.websockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.connectivity.MatcherEndpointProxy;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.connectivity.BaseMatcherEndpointProxy;
import net.powermatcher.extensions.connectivity.websockets.data.ClusterInfoModel;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage;
import net.powermatcher.extensions.connectivity.websockets.data.PmMessage.PayloadType;
import net.powermatcher.extensions.connectivity.websockets.data.PriceUpdateModel;
import net.powermatcher.extensions.connectivity.websockets.json.ModelMapper;
import net.powermatcher.extensions.connectivity.websockets.json.PmJsonSerializer;

import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.google.gson.JsonSyntaxException;

/**
 * WebSocket implementation of an {@link MatcherEndpointProxy}. Enabled two agents to communicate via WebSockets and
 * JSON over a TCP connection.
 * 
 * @author FAN
 * @version 2.0
 */
@WebSocket()
@Component(designateFactory = MatcherEndpointProxyWebsocket.Config.class, immediate = true, provide = {
        ObservableAgent.class, MatcherEndpoint.class, MatcherEndpointProxy.class })
public class MatcherEndpointProxyWebsocket extends BaseMatcherEndpointProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherEndpointProxyWebsocket.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "", description = "remote agent endpoint proxy to connect to.")
        String desiredConnectionId();

        @Meta.AD(deflt = "matcherendpointproxy", description = "local agent identification")
        String agentId();

        @Meta.AD(
                deflt = "ws://localhost:8080/powermatcher/websockets/agentendpoint",
                description = "URL of powermatcher websocket endpoint.")
        String powermatcherUrl();

        @Meta.AD(deflt = "30", description = "reconnect timeout keeping the connection alive.")
        int reconnectTimeout();

        @Meta.AD(deflt = "60", description = "connect timeout to wait for remote server to respond.")
        int connectTimeout();
    }

    private URI powermatcherUrl;

    private org.eclipse.jetty.websocket.api.Session remoteSession;

    private WebSocketClient client;

    private int connectTimeout;

    /**
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public synchronized void activate(Map<String, Object> properties) {
        // Read configuration properties
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.setAgentId(config.agentId());

        try {
            this.powermatcherUrl = new URI(config.powermatcherUrl() + "?agentId=" + this.getAgentId()
                    + "&desiredConnectionId=" + config.desiredConnectionId());
        } catch (URISyntaxException e) {
            LOGGER.error("Malformed URL for powermatcher websocket endpoint. Reason {}", e);
            return;
        }

        this.baseActivate(config.reconnectTimeout());
        this.connectTimeout = config.connectTimeout();
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public synchronized void deactivate() {
        this.baseDeactivate();
    }

    /**
     * {@inheritDoc}
     */
    @Reference
    @Override
    public void setExecutorService(ScheduledExecutorService scheduler) {
        super.setExecutorService(scheduler);
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation opens a websocket.
     */
    @Override
    public synchronized boolean connectRemote() {
        if (!this.isLocalConnected()) {
            // Don't connect when no agentRole is connected
            return true;
        }

        if (this.isRemoteConnected()) {
            // Already connected, skip connection
            return true;
        }

        return connectWebsocket();
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation closes the open websocket.
     */
    @Override
    public synchronized boolean disconnectRemote() {
        // Terminate remote session (if any)
        if (this.isRemoteConnected()) {
            this.remoteSession.close(new CloseStatus(0, "Normal disconnect"));
        }

        this.remoteSession = null;

        // Stop the client
        if (this.client != null && !this.client.isStopped()) {
            try {
                this.client.stop();
            } catch (Exception e) {
                LOGGER.warn("Unable to disconnect, reason: [{}]", e);
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation checks to see if the websocket is open.
     */
    @Override
    public boolean isRemoteConnected() {
        return this.remoteSession != null && this.remoteSession.isOpen();
    }

    /**
     * {@inheritDoc}
     * 
     * This specific implementation serializes the {@link Bid} to json and sends it through the websocket.
     */
    @Override
    public synchronized void updateBidRemote(Bid newBid) {
        // Relay bid to remote agent
        try {
            PmJsonSerializer serializer = new PmJsonSerializer();
            String message = serializer.serializeBid(newBid);
            this.remoteSession.getRemote().sendString(message);
        } catch (IOException e) {
            LOGGER.error("Unable to send new bid to remote agent. Reason {}", e);
        }
    }

    @OnWebSocketConnect
    public void onConnect(org.eclipse.jetty.websocket.api.Session session) {
        LOGGER.info("Connected: {}", session);
    }

    @OnWebSocketClose
    public void onDisconnect(int statusCode, String reason) {
        LOGGER.info("Connection closed: {} - {}", statusCode, reason);

        this.remoteSession = null;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        LOGGER.debug("Received message from remote agent {}", message);

        try {
            // Decode the JSON data
            PmJsonSerializer serializer = new PmJsonSerializer();
            PmMessage pmMessage = serializer.deserialize(message);

            // Handle specific message
            if (pmMessage.getPayloadType() == PayloadType.PRICE_UPDATE) {
                // Relay price update to local agent
                PriceUpdate newPriceUpdate = ModelMapper.mapPriceUpdate((PriceUpdateModel) pmMessage.getPayload());
                this.updateLocalPrice(newPriceUpdate);
            }

            if (pmMessage.getPayloadType() == PayloadType.CLUSTERINFO) {
                // Sync marketbasis and clusterid with local session, for new
                // connections
                ClusterInfoModel clusterInfo = (ClusterInfoModel) pmMessage.getPayload();
                this.updateRemoteClusterId(clusterInfo.getClusterId());
                this.updateRemoteMarketBasis(ModelMapper.convertMarketBasis(clusterInfo.getMarketBasis()));
            }
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Unable to understand message from remote agent: {}", message);
        }
    }

    private boolean connectWebsocket() {
        // Try to setup a new websocket connection.
        client = new WebSocketClient();
        ClientUpgradeRequest request = new ClientUpgradeRequest();

        try {
            client.start();
            Future<org.eclipse.jetty.websocket.api.Session> connectFuture = client.connect(this, this.powermatcherUrl,
                    request);
            LOGGER.info("Connecting to : {}", request);

            // Wait configurable time for remote to respond
            org.eclipse.jetty.websocket.api.Session newRemoteSession = connectFuture.get(this.connectTimeout,
                    TimeUnit.SECONDS);

            this.remoteSession = newRemoteSession;
        } catch (Exception e) {
            LOGGER.error("Unable to connect to remote agent. Reason {}", e);

            this.remoteSession = null;
            return false;
        }

        return true;
    }
}
