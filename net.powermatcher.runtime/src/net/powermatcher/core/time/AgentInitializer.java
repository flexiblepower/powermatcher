package net.powermatcher.core.time;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.TimeService;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(immediate = true)
public class AgentInitializer {

	private Set<Agent> agents = new HashSet<Agent>();
	private LoggingScheduler scheduler = new LoggingScheduler();
	private TimeService timeService = new SystemTimeService();

	@Activate
	public void activate() {
	}

	@Reference(dynamic = true, multiple = true, optional = true)
	public void addAgentEndpoint(AgentEndpoint agentEndpoint) {
		addAgent((Agent) agentEndpoint);
	}

	public void removeAgentEndpoint(AgentEndpoint agentEndpoint) {
		removeAgent((Agent) agentEndpoint);
	}

	@Reference(dynamic = true, multiple = true, optional = true)
	public void addMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
		addAgent((Agent) matcherEndpoint);
	}

	public void removeMatcherEndpoint(MatcherEndpoint matcherEndpoint) {
		removeAgent((Agent) matcherEndpoint);
	}

	private synchronized void addAgent(Agent agent) {
		if (!agents.contains(agent)) {
			agent.setExecutorService(scheduler);
			agent.setTimeService(timeService);
			agents.add(agent);
		}
	}

	private synchronized void removeAgent(Agent agent) {
		agents.remove(agent);
	}

}
