package net.powermatcher.core.sessions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.Agent;
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

/**
* <p>
* This class represents a {@link SessionManager} component which will store the
* active sessions between an an {@link AgentRole} and a {@link MatcherRole}
* object.
* </p>
* 
 * <p>
* It is responsible for connecting and disconnecting an {@link Auctioneer},
* {@link Concentrator} and agents. In <code>activeSessions</code> the
* {@link Session} will be stored. The {@link SessionManager} will connect a
* {@link MatcherRole} to an agent and an {@link AgentRole} with a
* {@link MatcherRole}.
* 
 * @author FAN
* @version 1.0
* 
 */
@Component(immediate = true)
public class SessionManager {

       private static final Logger LOGGER = LoggerFactory
                    .getLogger(SessionManager.class);

       /**
       * Holds the agentRoles
       */
       private ConcurrentMap<String, AgentRole> agentRoles = new ConcurrentHashMap<String, AgentRole>();

       /**
       * Holds the matcherRoles
       */
       private ConcurrentMap<String, MatcherRole> matcherRoles = new ConcurrentHashMap<String, MatcherRole>();

       /**
       * Holds the activeSessions
       */
       private Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>();

       /**
       * Holds the desiredConnections
       */
       private Map<String, String> desiredConnections = new ConcurrentHashMap<String, String>();

       @Activate
       public synchronized void activate(Map<String, Object> properties) {
             // // Config config = Configurable.createConfigurable(Config.class,
             // // properties);
             // wantedSessions = new HashSet<String>(config.activeConnections());
             updateConnections();
       }

       @Reference(dynamic = true, multiple = true, optional = true)
       public void addAgentRole(AgentRole agentRole, Map<String, Object> properties) {
             Agent agent = (Agent) agentRole;
             String agentId = agent.getAgentId();

             // Modified agent
             if (desiredConnections.containsKey(agentId)) {
                    desiredConnections.remove(agentId);
             }

             desiredConnections.put(agentId, agent.getDesiredParentId());
             LOGGER.debug("Added new wanted connection: [{}]", agentId + ":" + agent.getDesiredParentId());
             
             if (agentId == null) {
                    LOGGER.warn("Registered an agent with no agentId: " + agentRole);
             } else if (agentRoles.putIfAbsent(agentId, agentRole) != null) {
                    LOGGER.warn("An agent with the id " + agentId
                                 + " was already registered");
             } else {
                    updateConnections();
             }
       }

       @Reference(dynamic = true, multiple = true, optional = true)
       public void addMatcherRole(MatcherRole matcherRole, Map<String, Object> properties) {
             Agent agent = (Agent) matcherRole;
             String matcherId = agent.getAgentId();

             // Modified matcher
             if (desiredConnections.containsKey(matcherId)) {
                    desiredConnections.remove(matcherId);
             }

             // check if auctioneer
             if (agent.getDesiredParentId() != null) {
                    desiredConnections.put(matcherId, agent.getDesiredParentId());
                    LOGGER.debug("Added new wanted connection: [{}]", matcherId + ":" + agent.getDesiredParentId());
             }

             if (matcherId == null) {
                    LOGGER.warn("Registered an matcher with no matcherId: "
                                 + matcherRole);
             } else if (matcherRoles.putIfAbsent(matcherId, matcherRole) != null) {
                    LOGGER.warn("An matcher with the id " + matcherId
                                 + " was already registered");
             } else {
                    updateConnections();
             }
       }

       private synchronized void updateConnections() {
             for (String desiredAgentId : desiredConnections.keySet()) {
            	 String agentId = desiredAgentId;
                 String matcherId = desiredConnections.get(desiredAgentId);

                 AgentRole agentRole = agentRoles.get(desiredAgentId);
                 MatcherRole matcherRole = matcherRoles.get(matcherId);

                 if (agentRole != null && matcherRole != null) {
                 	LOGGER.info("Connecting session: [{}]", agentId + ":"
                                        + matcherId);
                    Session session = new SessionImpl(this, agentRole, agentId,
                                        matcherRole, matcherId, agentId + ":" + matcherId);
                    if (matcherRole.connectToAgent(session)) {
                    	agentRole.connectToMatcher(session);
                        activeSessions.put(agentId + ":" + matcherId, session);
                        LOGGER.info("Added new active session: {}", agentId + ":" + matcherId);
                    }
                 }
             }
       }

       public void removeAgentRole(AgentRole agentRole,
                    Map<String, Object> properties) {
             Agent agent = (Agent) agentRole;
             String agentId = agent.getAgentId();
             if (agentId != null && agentRoles.get(agentId) == agentRole) {
                    agentRoles.remove(agentId);
                    desiredConnections.remove(agentId);
                    LOGGER.info("Removed agentRole: {}", agentId);
             }
       }

       public void removeMatcherRole(MatcherRole matcherRole,
                    Map<String, Object> properties) {
             Agent agent = (Agent) matcherRole;
             String matcherId = agent.getAgentId();
             
             if (matcherId != null && matcherRoles.get(matcherId) == matcherRole) {
                    matcherRoles.remove(matcherId);
                    // check if auctioneer
                    if (agent.getDesiredParentId() != null) {
                    	desiredConnections.remove(matcherId);
                    	LOGGER.info("Removed matcherRole: {}", matcherId);
                    }
             }
       }

       @Modified
       public synchronized void modified(Map<String, Object> properties) {
             // Config config = Configurable.createConfigurable(Config.class,
             // properties);
             // wantedSessions = new HashSet<String>(config.activeConnections());
             updateConnections();
       }

       void disconnected(SessionImpl sessionImpl) {
             activeSessions.remove(sessionImpl.getSessionId());
             // TODO: reconnect? retry?
       }
}
