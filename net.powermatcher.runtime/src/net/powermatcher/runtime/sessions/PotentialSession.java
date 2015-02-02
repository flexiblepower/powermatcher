package net.powermatcher.runtime.sessions;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a matching pair of {@link MatcherEndpoint} and {@link AgentEndpoint}. A PotentialSession does not have to
 * be complete right away.
 */
public class PotentialSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(PotentialSession.class);

    private AgentEndpoint agentEndpoint;
    private MatcherEndpoint matcherEndpoint;
    private SessionImpl session;

    public AgentEndpoint getAgentEndpoint() {
        return agentEndpoint;
    }

    public String getAgentId() {
        if (agentEndpoint == null) {
            return null;
        } else {
            return agentEndpoint.getAgentId();
        }
    }

    public void setAgentEndpoint(AgentEndpoint agentEndpoint) {
        if (agentEndpoint != null && matcherEndpoint != null) {
            if (!matcherEndpoint.getAgentId().equals(agentEndpoint.getDesiredParentId())) {
                throw new IllegalArgumentException("Desired parent of AgentEndpoint does not match the current MatcherEndpoint");
            }
        }
        this.agentEndpoint = agentEndpoint;
    }

    public MatcherEndpoint getMatcherEndpoint() {
        return matcherEndpoint;
    }

    public String getMatcherId() {
        if (matcherEndpoint == null) {
            if (agentEndpoint == null) {
                return null;
            } else {
                return agentEndpoint.getDesiredParentId();
            }
        } else {
            return matcherEndpoint.getAgentId();
        }
    }

    public void setMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        if (agentEndpoint != null && matcherEndpoint != null) {
            if (!matcherEndpoint.getAgentId().equals(agentEndpoint.getDesiredParentId())) {
                throw new IllegalArgumentException("Desired parent of AgentEndpoint does not match the new MatcherEndpoint");
            }
        }
        this.matcherEndpoint = matcherEndpoint;
    }

    /**
     * Try to build an actual {@link Session}
     *
     * @return true if something changed
     */
    public boolean tryConnect() {
        if (session == null && matcherEndpoint != null && agentEndpoint != null) {
            SessionImpl newSession = new SessionImpl(agentEndpoint, matcherEndpoint, this);
            if (matcherEndpoint.connectToAgent(newSession)) {
                // Success!
                session = newSession;
                agentEndpoint.connectToMatcher(newSession);
                LOGGER.debug("Connected MatcherEndpoint '{}' with AgentEndpoint '{}' with Session {}",
                             matcherEndpoint.getAgentId(),
                             agentEndpoint.getAgentId(),
                             newSession.getSessionId());
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called from the {@link SessionImpl} when the session is disconnected. The {@link MatcherEndpoint},
     * the {@link AgentEndpoint} or the {@link SessionManager} can trigger a disconnect.
     */
    public void disconnected() {
        LOGGER.debug("Session {} between MatcherEndpoint '{}' with AgentEndpoint '{}' was disconnected",
                     session.getSessionId(),
                     matcherEndpoint.getAgentId(),
                     agentEndpoint.getAgentId());
        session = null;
    }

    /**
     * Disconnect the current session (if any)
     */
    public void disconnect() {
        if (session != null) {
            // This method will call this.disconnected()
            session.disconnect();
        }
    }

}
