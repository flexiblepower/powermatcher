package net.powermatcher.runtime;

import net.powermatcher.api.Agent.Status;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a matching pair of {@link MatcherEndpoint} and {@link AgentEndpoint}. A PotentialSession always has an
 * {@link AgentEndpoint} (and thus its desired parent), but does not have to have a valid {@link MatcherEndpoint} right
 * now.
 */
public class PotentialSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(PotentialSession.class);

    private final AgentEndpoint agentEndpoint;
    private MatcherEndpoint matcherEndpoint;
    private volatile SessionImpl session;

    public PotentialSession(AgentEndpoint agentEndpoint) {
        if (agentEndpoint == null) {
            throw new NullPointerException("Agent can not be null");
        }
        this.agentEndpoint = agentEndpoint;
    }

    public AgentEndpoint getAgentEndpoint() {
        return agentEndpoint;
    }

    public String getAgentId() {
        return agentEndpoint.getAgentId();
    }

    public MatcherEndpoint getMatcherEndpoint() {
        return matcherEndpoint;
    }

    public String getMatcherId() {
        return agentEndpoint.getDesiredParentId();
    }

    public void setMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        if (session != null) {
            disconnect();
        }

        if (matcherEndpoint != null && !matcherEndpoint.getAgentId().equals(agentEndpoint.getDesiredParentId())) {
            throw new IllegalArgumentException("Desired parent of AgentEndpoint does not match the new MatcherEndpoint");
        }
        this.matcherEndpoint = matcherEndpoint;
    }

    /**
     * Try to build an actual {@link Session}
     *
     * @return true if something changed
     */
    public synchronized boolean tryConnect() {
        if (session == null && matcherEndpoint != null) {
            Status matcherStatus = matcherEndpoint.getStatus();
            Status agentStatus = agentEndpoint.getStatus();
            if (matcherStatus.isConnected() && !agentStatus.isConnected()) {
                session = new SessionImpl(agentEndpoint, matcherEndpoint, this);
                synchronized (session) {
                    try {
                        // This synchronized block makes sure the whole connection is made before updates can be sent
                        // Also see that in the SessionImpl the update*() methods are synchronized
                        matcherEndpoint.connectToAgent(session);
                        agentEndpoint.connectToMatcher(session);
                        LOGGER.debug("Connected MatcherEndpoint '{}' with AgentEndpoint '{}' with Session {}",
                                     matcherEndpoint.getAgentId(),
                                     agentEndpoint.getAgentId(),
                                     session.getSessionId());
                        session.setConnected();
                        return true;
                    } catch (IllegalStateException ex) {
                        session = null;
                        LOGGER.warn("Could not connect agent[{}] to matcher[{}]: {}",
                                    agentEndpoint.getAgentId(),
                                    matcherEndpoint.getAgentId(),
                                    ex.getMessage());
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method is called from the {@link SessionImpl} when the session is disconnected. The {@link MatcherEndpoint},
     * the {@link AgentEndpoint} or the {@link SessionManager} can trigger a disconnect.
     */
    public synchronized void disconnected() {
        if (session != null) {
            LOGGER.debug("Session {} between MatcherEndpoint '{}' with AgentEndpoint '{}' was disconnected",
                         session.getSessionId(),
                         matcherEndpoint.getAgentId(),
                         agentEndpoint.getAgentId());
            session = null;
        }
    }

    /**
     * Disconnect the current session (if any)
     */
    public synchronized void disconnect() {
        if (session != null) {
            // This method will call this.disconnected()
            session.disconnect();
        }
    }
}
