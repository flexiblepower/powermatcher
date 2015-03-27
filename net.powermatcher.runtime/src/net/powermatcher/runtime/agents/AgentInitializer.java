package net.powermatcher.runtime.agents;

import java.util.concurrent.ConcurrentSkipListSet;

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

    private final ConcurrentSkipListSet<String> agents = new ConcurrentSkipListSet<String>();
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

    private void addAgent(Agent agent) {
        if (agents.add(agent.getAgentId())) {
            agent.setContext(runtimeContext);
            logger.debug("Detected agent with id [{}]", agent.getAgentId());
        }
    }

    private void removeAgent(Agent agent) {
        if (agents.remove(agent.getAgentId())) {
            logger.debug("Removed agent with id [{}]", agent.getAgentId());
        }
    }

}
