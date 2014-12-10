package net.powermatcher.core.sessions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(immediate = true)
public class SessionManager implements SessionManagerInterface {

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

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addAgentEndpoint(AgentEndpoint agentEndpoint) {
        Agent agent = (Agent) agentEndpoint;
        String agentId = agent.getAgentId();

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

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        Agent agent = (Agent) matcherEndpoint;
        String agentId = agent.getAgentId();

        if (agentId == null) {
            LOGGER.warn("Registered an matcher with no matcherId: " + matcherEndpoint);
        } else if (matcherEndpoints.putIfAbsent(agentId, matcherEndpoint) != null) {
            LOGGER.warn("An matcher with the id " + agentId + " was already registered");
        } else {
            updateConnections();
        }
    }

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

    public void removeMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        Agent agent = (Agent) matcherEndpoint;
        String matcherId = agent.getAgentId();

        if (matcherId != null && matcherEndpoints.get(matcherId) == matcherEndpoint) {
            matcherEndpoints.remove(matcherId);
            updateConnections();
        }
    }

    void disconnected(SessionImpl sessionImpl) {
        activeSessions.remove(sessionImpl.getSessionId());
    }

    @Override
    public Map<String, AgentEndpoint> getAgentEndpoints() {
        return new HashMap<String, AgentEndpoint>(agentEndpoints);
    }

    @Override
    public Map<String, MatcherEndpoint> getMatcherEndpoints() {
        return new HashMap<String, MatcherEndpoint>(matcherEndpoints);
    }

    /**
     * Returns the active sessions from the SessionManager.
     * 
     * @return {@link Session}
     */
    @Override
    public Map<String, Session> getActiveSessions() {
        return new HashMap<String, Session>(activeSessions);
    }
}
