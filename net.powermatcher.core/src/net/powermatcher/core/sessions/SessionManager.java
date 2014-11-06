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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(immediate = true, designate = SessionManager.Config.class)
public class SessionManager {
	private static final Logger logger = LoggerFactory
			.getLogger(SessionManager.class);

	private static final String KEY_AGENT_ID = "agentId";
	private static final String KEY_MATCHER_ID = "matcherId";

	public static interface Config {
		@Meta.AD
		List<String> activeConnections();
	}

	private ConcurrentMap<String, AgentRole> agentRoles = new ConcurrentHashMap<String, AgentRole>();
	private Set<String> wantedSessions;
	private Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>();
	private ConcurrentMap<String, MatcherRole> matcherRoles = new ConcurrentHashMap<String, MatcherRole>();

	
	@Reference(dynamic = true, multiple = true, optional = true)
	public void addAgentRole(AgentRole agentRole, Map<String, Object> properties) {
		String agentId = getAgentId(properties);
		if (agentId == null) {
			logger.warn("Registered an agent with no agentId: " + agentRole);
		} else if (agentRoles.putIfAbsent(agentId, agentRole) != null) {
			logger.warn("An agent with the id " + agentId
					+ " was already registered");
		} else {
			updateConnections(true);
		}
	}

	@Reference(dynamic = true, multiple = true, optional = true)
	public void addMatcherRole(MatcherRole matcherRole,
			Map<String, Object> properties) {
		String matcherId = getMatcherId(properties);
		if (matcherId == null) {
			logger.warn("Registered an matcher with no matcherId: "
					+ matcherRole);
		} else if (matcherRoles.putIfAbsent(matcherId, matcherRole) != null) {
			logger.warn("An matcher with the id " + matcherId
					+ " was already registered");
		}  else {
			updateConnections(true);
		}
	}
	
	public void removeAgentRole(AgentRole agentRole,
			Map<String, Object> properties) {
		String agentId = getAgentId(properties);
		if (agentId != null) {
			if (agentRoles.get(agentId) == agentRole) {
				agentRoles.remove(agentId);
			}
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

	public void removeMatcherRole(MatcherRole matcherRole,
			Map<String, Object> properties) {
		String matcherId = getMatcherId(properties);
		if (matcherId != null) {
			if (matcherRoles.get(matcherId) == matcherRole) {
				matcherRoles.remove(matcherId);
			}
		}
	}

	@Activate
	public synchronized void activate(Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class,
				properties);
		wantedSessions = new HashSet<String>(config.activeConnections());
		updateConnections(true);
	}

	@Modified
	public synchronized void modified(Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class,
				properties);
		wantedSessions = new HashSet<String>(config.activeConnections());
		updateConnections(true);
	}

	private synchronized void updateConnections(boolean firstTry) {
		if (wantedSessions != null) {
			HashSet<String> sessionsToRemove = new HashSet<String>(
					activeSessions.keySet());
			sessionsToRemove.removeAll(wantedSessions);

			HashSet<String> sessionsToCreate = new HashSet<String>(
					wantedSessions);
			sessionsToCreate.removeAll(activeSessions.keySet());

			for (String sessionId : sessionsToRemove) {
				logger.info("Disconnecting session: {}", sessionId);
				Session session = activeSessions.remove(sessionId);
				session.disconnect();
			}

			boolean retry = false;
			for (String sessionId : sessionsToCreate) {
				String[] split = sessionId.split("::");
				if (split.length != 2) {
					logger.warn("Illegal configuration for connection: "
							+ sessionId);
				} else {
					String agentId = split[0];
					String matcherId = split[1];

					AgentRole agentRole = agentRoles.get(agentId);
					MatcherRole matcherRole = matcherRoles.get(matcherId);

					if (agentRole != null && matcherRole != null) {
						logger.info("Connecting session: {}", sessionId);
						Session session = new SessionImpl(this, agentRole,
								agentId, matcherRole, matcherId, sessionId);
						if (matcherRole.connectToAgent(session)) {
							agentRole.connectToMatcher(session);
							activeSessions.put(sessionId, session);
						} else {
							retry = true; // TODO: better check?
						}
					}
				}
			}
			
			if(firstTry && retry) {
				updateConnections(false);
			}
		}
	}

	void disconnected(SessionImpl sessionImpl) {
		activeSessions.remove(sessionImpl.getSessionId());
		// TODO: reconnect? retry?
	}
}
