package net.powermatcher.core.sessions;

import java.util.Map;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;

public interface SessionManagerInterface {

    Map<String, Session> getActiveSessions();

    Map<String, MatcherEndpoint> getMatcherEndpoints();

    Map<String, AgentEndpoint> getAgentEndpoints();
}
