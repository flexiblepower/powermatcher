package net.powermatcher.runtime.sessions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;

import org.junit.experimental.theories.Theories;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * <p>
 * This class represents a {@link SessionManager} component which will store the active sessions between an an
 * {@link AgentEndpoint} and a {@link MatcherEndpoint} object.
 * </p>
 * 
 * <p>
 * It is responsible for connecting and disconnecting an {@link Auctioneer}, {@link Concentrator} and agents. In
 * <code>activeSessions</code> the {@link Session} will be stored. The {@link SessionManager} will connect a
 * {@link MatcherEndpoint} to an agent and an {@link AgentEndpoint} with a {@link MatcherEndpoint}.
 * </p>
 * 
 * @author FAN
 * @version 2.0
 */
@Component(immediate = true)
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Holds the agentEndpoints
     */
    private ConcurrentMap<String, AgentEndpoint> agentEndpoints = new ConcurrentHashMap<String, AgentEndpoint>();

    /**
     * Holds the matcherEndpoints
     */
    private ConcurrentMap<String, MatcherEndpoint> matcherEndpoints = new ConcurrentHashMap<String, MatcherEndpoint>();

    /**
     * Holds the activeSessions
     */
    private Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>();

    /**
     * Holds the desiredConnections
     */
    private Map<String, String> desiredConnections = new ConcurrentHashMap<String, String>();

    /**
     * Holds the agentId's in the cluster
     */
    private List<String> agentIds = new ArrayList<String>();

    /**
     * OSGI ConfigurationAdmin, stores bundle configuration data persistently.
     */
    private ConfigurationAdmin configurationAdmin;

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addAgentEndpoint(AgentEndpoint agentEndpoint) {
        Agent agent = (Agent) agentEndpoint;
        String agentId = agent.getAgentId();

        if (isNotUniqueAgentId(agentId, agentEndpoint, null)) {
            // Modified agent
            if (desiredConnections.containsKey(agentId)) {
                desiredConnections.remove(agentId);
            }

            desiredConnections.put(agentId, agent.getDesiredParentId());
            LOGGER.debug("Added new wanted connection: [{}]", agentId + ":" + agent.getDesiredParentId());

            if (agentId == null) {
                LOGGER.warn("Registered an agent with no agentId: " + agentEndpoint);
            } else if (agentEndpoints.putIfAbsent(agentId, agentEndpoint) != null) {
                LOGGER.warn("An agent with the id " + agentId + " was already registered");
            } else {
                updateConnections();
            }
        }
        updateConnections();
    }

    /**
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public synchronized void activate() {
        updateConnections();
    }

    /**
     * Adds a {@link MatcherEndpoint} to the matcherEndpoints map. It will also check if the agentId is unique.
     * 
     * @param matcherEndpoint
     *            the new {@link MatcherEndpoint}
     */
    @Reference(dynamic = true, multiple = true, optional = true)
    public void addMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        Agent agent = (Agent) matcherEndpoint;
        String agentId = agent.getAgentId();

        if (isNotUniqueAgentId(agentId, null, matcherEndpoint)) {

            if (agentId == null) {
                LOGGER.warn("Registered an matcher with no matcherId: " + matcherEndpoint);
            } else if (matcherEndpoints.putIfAbsent(agentId, matcherEndpoint) != null) {
                LOGGER.warn("An matcher with the id " + agentId + " was already registered");
            } else {
                updateConnections();
            }
        }
    }

    /**
     * This method is called whenever an {@link Agent} is added or removed.
     */
    private synchronized void updateConnections() {
        for (String desiredAgentId : desiredConnections.keySet()) {
            String agentId = desiredAgentId;
            String matcherId = desiredConnections.get(desiredAgentId);

            AgentEndpoint agentEndpoint = agentEndpoints.get(desiredAgentId);
            MatcherEndpoint matcherEndpoint = matcherEndpoints.get(matcherId);

            if (agentEndpoint != null && matcherEndpoint != null) {
                final String sessionId = agentId + ":" + matcherId;
                if (activeSessions.containsKey(sessionId)) {
                    // session already exists
                    continue;
                }
                LOGGER.info("Connecting session: [{}]", agentId + ":" + matcherId);
                Session session = new SessionImpl(this, agentEndpoint, agentId, matcherEndpoint, matcherId, sessionId);
                if (matcherEndpoint.connectToAgent(session)) {
                    agentEndpoint.connectToMatcher(session);
                    activeSessions.put(sessionId, session);
                    LOGGER.info("Added new active session: {}", sessionId);
                }
            } else {
                final String removeSessionId = desiredAgentId + ":" + matcherId;
                if (activeSessions.containsKey(removeSessionId)) {
                    Session session = activeSessions.remove(removeSessionId);
                    session.disconnect();
                }
            }
        }
    }

    /**
     * Removes the given {@link AgentEndpoint} from agentEndpoints.
     * 
     * @param agentEndpoint
     *            the {@link AgentEndpoint} that will be removed.
     */
    public void removeAgentEndpoint(AgentEndpoint agentEndpoint) {
        Agent agent = (Agent) agentEndpoint;
        String agentId = agent.getAgentId();
        if (agentId != null && agentEndpoints.get(agentId) == agentEndpoint) {
            agentEndpoints.remove(agentId);
            updateConnections();
            desiredConnections.remove(agentId);
            LOGGER.info("Removed agentEndpoint: {}", agentId);
        }
    }

    /**
     * Checks to see if the new agentId already exists in {@link Theories} cluster.
     * 
     * @param agentId
     *            the agentId of {@link Agent} that has to be checked for uniqueness.
     * @param agentEndpoint
     *            the {@link AgentEndpoint} if this is an {@link AgentEndpoint}, <code>null</code> if not.
     * @param matcherEndpoint
     *            the {@link MatcherEndpoint} if this is an {@link MatcherEndpoint}, <code>null</code> if not.
     * @return
     */
    private boolean isNotUniqueAgentId(String agentId, AgentEndpoint agentEndpoint, MatcherEndpoint matcherEndpoint) {
        if (agentIds.contains(agentId)) {
            if (agentEndpoint != null && agentEndpoints.get(agentId) != null) {
                AgentEndpoint oldAgentEndpoint = agentEndpoints.get(agentId);
                deleteAgentId(agentId, oldAgentEndpoint, null);

                return false;
            } else if (matcherEndpoint != null && (matcherEndpoints.get(agentId) != null)) {
                MatcherEndpoint oldMatcherEndpoint = matcherEndpoints.get(agentId);
                deleteAgentId(agentId, null, oldMatcherEndpoint);

                return false;
            }
        } else {
            if (!agentIds.contains(agentId)) {
                agentIds.add(agentId);
            }
        }
        return true;
    }

    /**
     * Removes a managed service from OSGi with the {@link ConfigurationAdmin}.
     * 
     * @param AgentId
     *            the agentId of the managed service that will be removed
     * @param agentEndpoint
     *            the {@link AgentEndpoint} if this is an {@link AgentEndpoint}, <code>null</code> if not.
     * @param matcherEndpoint
     *            the {@link MatcherEndpoint} if this is an {@link MatcherEndpoint}, <code>null</code> if not.
     */
    private void deleteAgentId(String AgentId, AgentEndpoint agentEndpoint, MatcherEndpoint matcherEndpoint) {
        String pidOldAgentEndpoint;
        try {
            for (Configuration c : configurationAdmin.listConfigurations(null)) {
                if (agentEndpoint != null) {
                    pidOldAgentEndpoint = agentEndpoint.getServicePid();
                } else {
                    pidOldAgentEndpoint = matcherEndpoint.getServicePid();
                }
                String pidConfigAgentEndpoint = (String) c.getProperties().get("service.pid");
                if ((agentEndpoint.getAgentId().equals((String) c.getProperties().get("agentId")))
                        && (!pidOldAgentEndpoint.equals(pidConfigAgentEndpoint))) {
                    LOGGER.error("AgentId " + agentEndpoint.getAgentId() + "was already registered.");
                    c.delete();
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (InvalidSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Removes the given {@link MatcherEndpoint} from matcherEndpoints.
     * 
     * @param matcherEndpoint
     */
    public void removeMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        Agent agent = (Agent) matcherEndpoint;
        String matcherId = agent.getAgentId();

        if (matcherId != null && matcherEndpoints.get(matcherId) == matcherEndpoint) {
            matcherEndpoints.remove(matcherId);
            updateConnections();
        }
    }

    /**
     * This is called by a {@link Session} when it disconnects.
     * 
     * @param sessionImpl
     */
    void disconnected(SessionImpl sessionImpl) {
        activeSessions.remove(sessionImpl.getSessionId());
    }

    /**
     * @return a copy of agentEndpoints.
     */
    public Map<String, AgentEndpoint> getAgentEndpoints() {
        return new HashMap<String, AgentEndpoint>(agentEndpoints);
    }

    /**
     * @return a copy of the matcherEndpoints
     */
    public Map<String, MatcherEndpoint> getMatcherEndpoints() {
        return new HashMap<String, MatcherEndpoint>(matcherEndpoints);
    }

    /**
     * @return a copy of activeSessions.
     */
    public Map<String, Session> getActiveSessions() {
        return new HashMap<String, Session>(activeSessions);
    }

    /**
     * @param the
     *            new {@link ConfigurationAdmin} value.
     */
    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    /**
     * @param the
     *            new <code>List</code> of agentId <code>Strings</code>
     */
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }
}
