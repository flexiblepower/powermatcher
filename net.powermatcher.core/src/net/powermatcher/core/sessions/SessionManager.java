package net.powermatcher.core.sessions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * <p>
 * This class represents a {@link SessionManager} component which will store the active sessions between an an
 * {@link AgentRole} and a {@link MatcherRole} object.
 * </p>
 * 
 * <p>
 * It is responsible for connecting and disconnecting an {@link Auctioneer}, {@link Concentrator} and agents. In
 * <code>activeSessions</code> the {@link Session} will be stored. The {@link SessionManager} will connect a
 * {@link MatcherRole} to an agent and an {@link AgentRole} with a {@link MatcherRole}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(immediate = true, designate = SessionManager.Config.class)
public class SessionManager {
    public static interface Config {
        @Meta.AD
        List<String> activeConnections();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private static final String KEY_AGENT_ID = "agentId";

    private static final String KEY_MATCHER_ID = "matcherId";

    /**
     * Holds the agentRoles
     */
    private ConcurrentMap<String, AgentRole> agentRoles = new ConcurrentHashMap<String, AgentRole>();

    /**
     * Holds the matcherRoles
     */
    private ConcurrentMap<String, MatcherRole> matcherRoles = new ConcurrentHashMap<String, MatcherRole>();

    /**
     * Holds the wantedSessions
     */
    private Set<String> wantedSessions;

    /**
     * Holds the activeSessions
     */
    private Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>();

    @Activate
    public synchronized void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        wantedSessions = new HashSet<String>(config.activeConnections());
        updateConnections(true);
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addAgentRole(AgentRole agentRole, Map<String, Object> properties) {
        String agentId = getAgentId(properties);
        if (agentId == null) {
            LOGGER.warn("Registered an agent with no agentId: " + agentRole);
        } else if (agentRoles.putIfAbsent(agentId, agentRole) != null) {
            LOGGER.warn("An agent with the id " + agentId + " was already registered");
        } else {
            updateConnections(true);
        }
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addMatcherRole(MatcherRole matcherRole, Map<String, Object> properties) {
        String matcherId = getMatcherId(properties);
        if (matcherId == null) {
            LOGGER.warn("Registered an matcher with no matcherId: " + matcherRole);
        } else if (matcherRoles.putIfAbsent(matcherId, matcherRole) != null) {
            LOGGER.warn("An matcher with the id " + matcherId + " was already registered");
        } else {
            updateConnections(true);
        }
    }

    public void removeAgentRole(AgentRole agentRole, Map<String, Object> properties) {
        String agentId = getAgentId(properties);
        if (agentId != null && agentRoles.get(agentId) == agentRole) {
            agentRoles.remove(agentId);
        }
    }

    private String getAgentId(Map<String, Object> properties) {
        if (!properties.containsKey(KEY_AGENT_ID)) {
            return null;
        }
        return properties.get(KEY_AGENT_ID).toString();
    }

    private String getMatcherId(Map<String, Object> properties) {
        if (!properties.containsKey(KEY_MATCHER_ID)) {
            return null;
        }
        return properties.get(KEY_MATCHER_ID).toString();
    }

    public void removeMatcherRole(MatcherRole matcherRole, Map<String, Object> properties) {
        String matcherId = getMatcherId(properties);
        if (matcherId != null && matcherRoles.get(matcherId) == matcherRole) {
            matcherRoles.remove(matcherId);
        }
    }

    @Modified
    public synchronized void modified(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        wantedSessions = new HashSet<String>(config.activeConnections());
        updateConnections(true);
    }

    private synchronized void updateConnections(boolean firstTry) {
        // TODO Refactor this code to not nest more than 3 if/for/while/switch/try statements.
        if (wantedSessions != null) {
            Set<String> sessionsToRemove = new HashSet<String>(activeSessions.keySet());
            sessionsToRemove.removeAll(wantedSessions);

            Set<String> sessionsToCreate = new HashSet<String>(wantedSessions);
            sessionsToCreate.removeAll(activeSessions.keySet());

            for (String sessionId : sessionsToRemove) {
                LOGGER.info("Disconnecting session: {}", sessionId);
                Session session = activeSessions.remove(sessionId);
                session.disconnect();
            }

            boolean retry = false;
            for (String sessionId : sessionsToCreate) {
                String[] split = sessionId.split("::");
                if (split.length != 2) {
                    LOGGER.warn("Illegal configuration for connection: " + sessionId);
                } else {
                    String agentId = split[0];
                    String matcherId = split[1];

                    AgentRole agentRole = agentRoles.get(agentId);
                    MatcherRole matcherRole = matcherRoles.get(matcherId);

                    if (agentRole != null && matcherRole != null) {
                        LOGGER.info("Connecting session: [{}]", sessionId);
                        Session session = new SessionImpl(this, agentRole, agentId, matcherRole, matcherId, sessionId);
                        if (matcherRole.connectToAgent(session)) {
                            agentRole.connectToMatcher(session);
                            activeSessions.put(sessionId, session);
                        } else {
                            // TODO: better check?
                            retry = true;
                        }
                    }
                }
            }

            if (firstTry && retry) {
                updateConnections(false);
            }
        }
    }

    void disconnected(SessionImpl sessionImpl) {
        activeSessions.remove(sessionImpl.getSessionId());
        // TODO: reconnect? retry?
    }
}
