package net.powermatcher.remote.websockets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.remote.websockets.data.BidModel;
import net.powermatcher.remote.websockets.data.PmMessage;
import net.powermatcher.remote.websockets.json.ModelMapper;
import net.powermatcher.remote.websockets.json.PmJsonSerializer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Receiving end of the WebSocket implementation for PowerMatcher.
 * 
 * @author FAN
 * @version 2.0
 */
@WebSocket
@Component(designate = PowermatcherWebSocket.Config.class, immediate = true)
public class PowermatcherWebSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowermatcherWebSocket.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator",
                 description = "The agent identifier of the parent matcher to which agent proxies should be connected ")
                String
                desiredParentId();
    }

    private static String desiredParentId;

    private static ConfigurationAdmin configurationAdmin;

    /**
     * Internal info class which holds the remote and local connection information.
     */
    private class ConnectionInfo {
        private String remoteAgentId;
        private Session remoteSession;

        private String localAgentId;
        private Configuration localAgentConfig;
        private AgentEndpointProxyWebsocket localAgent;

        public String getRemoteAgentId() {
            return remoteAgentId;
        }

        public void setRemoteAgentId(String remoteAgentId) {
            this.remoteAgentId = remoteAgentId;
        }

        public Session getRemoteSession() {
            return remoteSession;
        }

        public void setRemoteSession(Session remoteSession) {
            this.remoteSession = remoteSession;
        }

        public String getLocalAgentId() {
            return localAgentId;
        }

        public void setLocalAgentId(String localAgentId) {
            this.localAgentId = localAgentId;
        }

        public Configuration getLocalAgentConfig() {
            return localAgentConfig;
        }

        public void setLocalAgentConfig(Configuration localAgentConfig) {
            this.localAgentConfig = localAgentConfig;
        }

        public AgentEndpointProxyWebsocket getLocalAgent() {
            return localAgent;
        }

        public void setLocalAgent(AgentEndpointProxyWebsocket localAgent) {
            this.localAgent = localAgent;
        }
    }

    private static final List<ConnectionInfo> connectionInfo = new ArrayList<ConnectionInfo>();

    /**
     * Add connectionInfo to list.
     * 
     * @param info
     *            the info to add
     */
    private void addInfo(ConnectionInfo info) {
        synchronized (connectionInfo) {
            connectionInfo.add(info);
        }
    }

    /**
     * Find connectionInfo based on local agentId
     * 
     * @param localAgentId
     *            id of agentProxy
     * @return connectionInfo or null when not found
     */
    private ConnectionInfo findInfoByLocalId(String localAgentId) {
        synchronized (connectionInfo) {
            for (ConnectionInfo info : connectionInfo) {
                if (info.getLocalAgentId().equals(localAgentId)) {
                    return info;
                }
            }
        }

        return null;
    }

    /**
     * Find connectionInfo based on remote agentId
     * 
     * @param remoteAgentId
     *            id of matcherProxy
     * @return connectionInfo or null when not found
     */
    private ConnectionInfo findInfoByRemoteId(String remoteAgentId) {
        synchronized (connectionInfo) {
            for (ConnectionInfo info : connectionInfo) {
                if (info.getRemoteAgentId().equals(remoteAgentId)) {
                    return info;
                }
            }
        }

        return null;
    }

    /**
     * Find connectionInfo based on remote session
     * 
     * @param remoteSession
     *            remote session created by matcherProxy
     * @return connectionInfo or null when not found
     */
    private ConnectionInfo findInfoByRemoteSession(Session remoteSession) {
        synchronized (connectionInfo) {
            for (ConnectionInfo info : connectionInfo) {
                if (info.getRemoteSession().equals(remoteSession)) {
                    return info;
                }
            }
        }

        return null;
    }

    /**
     * Remove connectionInfo based on remote agentId
     * 
     * @param remoteAgentId
     *            id of matcherProxy
     * @return connectionInfo or null when not found
     */
    private void removeInfoByRemoteId(String remoteAgentId) {
        synchronized (connectionInfo) {
            ConnectionInfo remove = null;
            remove = findInfoByRemoteId(remoteAgentId);
            if (remove != null) {
                connectionInfo.remove(remove);
            }
        }
    }

    /**
     * Disconnect and remove every connected remote agent
     */
    private void disconnectAll() {
        synchronized (connectionInfo) {
            // Disconnect every connected remote agent
            for (ConnectionInfo info : connectionInfo) {
                if (info.getRemoteSession() != null) {
                    info.getRemoteSession().close();
                }
            }

            connectionInfo.clear();
        }
    }

    @Reference
    public void setConfigurationAdmin(ConfigurationAdmin cadmin) {
        configurationAdmin = cadmin;
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        desiredParentId = config.desiredParentId();
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public synchronized void deactivate() {
        disconnectAll();
    }

    /**
     * Register {@link AgentEndpointProxyWebsocket} within the OSGI runtime.
     * 
     * @param proxy
     */
    @Reference(dynamic = true, multiple = true, optional = true)
    public synchronized void
            addProxy(AgentEndpointProxyWebsocket proxy) {
        LOGGER.info("Registered AgentEndpointProxy: [{}]",
                    proxy.getAgentId());

        // Associate session with agentendpoint proxy
        ConnectionInfo info = findInfoByLocalId(proxy.getAgentId());
        if (info == null) {
            LOGGER.warn("Received AgentEndpointProxy: [{}] for unknown remote session", proxy.getAgentId());
            return;
        }

        info.setLocalAgent(proxy);
        proxy.remoteMatcherProxyConnected(info.getRemoteSession());
    }

    /**
     * Deregister {@link AgentEndpointProxyWebsocket} within the OSGI runtime.
     * 
     * @param proxy
     */
    public synchronized void removeProxy(AgentEndpointProxyWebsocket proxy) {
        LOGGER.info("Deregistered AgentEndpointProxy: [{}]", proxy.getAgentId());
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
                        remoteSession.getUpgradeRequest().getRequestURI());
            return;
        }

        // Read remote agentId from the querystring
        String remoteMatcherEndpointId = queryString.get("agentId");
        if (remoteMatcherEndpointId == null || remoteMatcherEndpointId.length() == 0) {
            remoteSession.close();
            LOGGER.warn("Rejecting connection from remote agent [{}], agentId is missing from querystring",
                        remoteMatcherEndpointId);
            return;
        }

        // Search for existing remote agent, reject connection if already connected
        if (findInfoByRemoteId(remoteMatcherEndpointId) != null) {
            LOGGER.warn("Rejecting connection from remote agent [{}] for already existing connection.",
                        remoteMatcherEndpointId);
            remoteSession.close();
            return;
        }

        createAgentProxy(remoteMatcherEndpointId, remoteSession);
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
    public synchronized void onClose(Session remoteSession, int statusCode,
                                     String reason) {
        // Find existing session
        ConnectionInfo info = findInfoByRemoteSession(remoteSession);
        if (info == null) {
            LOGGER.warn("Received disconnect for non existing session.");
            return;
        }

        // Remove remote session from agent proxy and local session collection
        removeInfoByRemoteId(info.getRemoteAgentId());
        LOGGER.info("Agent disconnect detected remote agent: [{}], local agent", info.getRemoteAgentId(),
                    info.getLocalAgentId());
        info.getLocalAgent().remoteMatcherProxyDisconnected();

        // Delete agentProxy
        try {
            info.getLocalAgentConfig().delete();
        } catch (IOException e) {
            LOGGER.warn("Failed to delete agentProxy [{}], reason: {}", info.getLocalAgentId(), e);
        } catch (IllegalStateException e) {
            LOGGER.info("AgentProxy configuration already deleted [{}], reason: {}", info.getLocalAgentId());
        }
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
        ConnectionInfo info = findInfoByRemoteSession(remoteSession);
        if (info == null) {
            LOGGER.warn("Received bid update for non existing session.");
            return;
        }

        // Find associated local agentproxy
        LOGGER.info("Received bid update from remote agent [{}] for local agent [{}]",
                    info.getRemoteAgentId(), info.getLocalAgent().getAgentId());

        // Decode the JSON data
        PmJsonSerializer serializer = new PmJsonSerializer();
        PmMessage pmMessage = serializer.deserialize(message);
        BidUpdate newBid = ModelMapper.mapBidUpdate((BidModel) pmMessage.getPayload());

        // Relay bid update to local agent
        info.getLocalAgent().updateLocalBid(newBid);
    }

    /**
     * Create instance of new agentProxy
     * 
     * @param remoteMatcherEndpointId
     *            id of remote matcherproxy
     * @param remoteSession
     *            remote session created by matcherproxy
     */
    private void createAgentProxy(String remoteMatcherEndpointId, Session remoteSession) {
        // Generate unique agentId for agentProxy
        String newAgentId = UUID.randomUUID().toString();

        // Create instance of new agent
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("agentId", newAgentId);
        properties.put("desiredParentId", desiredParentId);

        Configuration agentConfig;
        try {
            agentConfig = configurationAdmin.createFactoryConfiguration(AgentEndpointProxyWebsocket.class.getName(),
                                                                        null);
            // Store connection info. The AgentProxy instance will be added after it registered itself (addReference)
            ConnectionInfo info = new ConnectionInfo();
            info.setRemoteAgentId(remoteMatcherEndpointId);
            info.setRemoteSession(remoteSession);
            info.setLocalAgentConfig(agentConfig);
            info.setLocalAgentId(newAgentId);
            addInfo(info);

            // Create instance of agentProxy
            agentConfig.update(properties);
        } catch (IllegalStateException e) {
            LOGGER.warn("ConfigurationAdmin is not available, unable to create new AgentProxy [{}], rejecting connection from remote agent [{}]",
                        e,
                        remoteMatcherEndpointId);
            remoteSession.close();
            removeInfoByRemoteId(remoteMatcherEndpointId);
        } catch (IOException e) {
            LOGGER.warn("Failed to create new agent enpoint proxy instance [{}], rejecting connection from remote agent [{}]",
                        e,
                        remoteMatcherEndpointId);
            remoteSession.close();
            removeInfoByRemoteId(remoteMatcherEndpointId);
        }
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
