package net.powermatcher.runtime.agents;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(immediate = true)
public class AgentInitializer {
    private static final Logger logger = LoggerFactory.getLogger(AgentInitializer.class);

    private final Set<Agent> agents = new HashSet<Agent>();
    private final FlexiblePowerContext runtimeContext = new PowerMatcherContext();

    @Activate
    public void activate() {
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addAgentEndpoint(AgentEndpoint agentEndpoint) {
        addAgent(agentEndpoint);
    }

    public void removeAgentEndpoint(AgentEndpoint agentEndpoint) {
        removeAgent(agentEndpoint);
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        addAgent(matcherEndpoint);
    }

    public void removeMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
        removeAgent(matcherEndpoint);
    }

    private synchronized void addAgent(Agent agent) {
        if (!agents.contains(agent)) {
            agent.setContext(runtimeContext);
            agents.add(agent);
            logger.debug("Detected agent with id [{}]", agent.getAgentId());
        }
    }

    private synchronized void removeAgent(Agent agent) {
        if (agents.remove(agent)) {
            logger.debug("Removed agent with id [{}]", agent.getAgentId());
        }
    }

}
