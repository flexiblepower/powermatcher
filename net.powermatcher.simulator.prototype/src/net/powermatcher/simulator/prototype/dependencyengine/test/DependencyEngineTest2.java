package net.powermatcher.simulator.prototype.dependencyengine.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.WindowConstants;

import net.powermatcher.simulator.prototype.dependencyengine.Activity;
import net.powermatcher.simulator.prototype.dependencyengine.DependencyEngine;
import net.powermatcher.simulator.prototype.dependencyengine.Link;
import net.powermatcher.simulator.prototype.pmcore.Agent;
import net.powermatcher.simulator.prototype.pmcore.Auctioneer;
import net.powermatcher.simulator.prototype.pmcore.Concentrator;
import net.powermatcher.simulator.prototype.pmcore.DeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.Matcher;
import net.powermatcher.simulator.prototype.pmcore.RandomMustRunDeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.VariableDeviceAgent;
import net.powermatcher.simulator.prototype.viz.PriceVisualizationAgent;
import net.powermatcher.simulator.prototype.viz.Visualization;

public class DependencyEngineTest2 {
	public static void main(String[] args) throws Exception {
		test(10, 1000, 4);
		// test(2, 2, 4);
	}

	private static void test(int concentratorCount, int agentCount, int threadCount) throws InterruptedException {
		TimeUnit unit = TimeUnit.MINUTES;
		final DependencyEngine engine = new DependencyEngine(new Date(0), 1, unit, threadCount);

		// create auctioneer
		Auctioneer auctioneer = new Auctioneer("AA  ");
		auctioneer.setTimeSource(engine);

		// create visualization
		Visualization visualization = new Visualization(engine);
		visualization.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		visualization.setVisible(true);
		auctioneer.addAgent(new PriceVisualizationAgent(engine, visualization));

		// create auctioneer price activity
		Activity auctioneerActivity = new Activity("AA  price", auctioneer);
		auctioneerActivity.scheduleAtFixedRate(engine.getCurrentTime(), 1, unit);
		engine.addActivity(auctioneerActivity);

		for (int i = 0; i < concentratorCount; i++) {
			// create concentrator
			final Concentrator concentrator = new Concentrator("CA " + i);
			concentrator.setTimeSource(engine);

			// create concentrator bidding activity
			Activity concentratorBidActivity = new Activity("CA  bid", concentrator);
			concentratorBidActivity.scheduleAtFixedRate(engine.getCurrentTime(), 1, unit);
			engine.addActivity(concentratorBidActivity);

			// link concentrator to auctioneer
			Link concentratorToAuctioneer = Link.create(auctioneer, Matcher.class);
			link(concentratorBidActivity, concentratorToAuctioneer, auctioneerActivity);
			concentrator.setMatcher((Matcher) concentratorToAuctioneer.getProxy());

			// create concentrator pricing activity
			Activity concentratorPriceActivity = new Activity("CA  price", concentrator);
			engine.addActivity(concentratorPriceActivity);

			// link auctioneer to concentrator
			Link auctioneerToConcentrator = Link.create(concentrator, Agent.class);
			link(auctioneerActivity, auctioneerToConcentrator, concentratorPriceActivity);
			auctioneer.addAgent((Agent) auctioneerToConcentrator.getProxy());

			for (int j = 0; j < agentCount / 2; j++) {
				final DeviceAgent agent = new RandomMustRunDeviceAgent("DAR" + i + "-" + j);
				agent.setTimeSource(engine);
				linkDeviceAgent(agent, concentrator, concentratorBidActivity, concentratorPriceActivity, engine, unit);
			}

			for (int j = 0; j < agentCount / 2; j++) {
				final DeviceAgent agent = new VariableDeviceAgent("DAV" + i + "-" + j);
				agent.setTimeSource(engine);
				linkDeviceAgent(agent, concentrator, concentratorBidActivity, concentratorPriceActivity, engine, unit);
			}
		}
	}

	private static void linkDeviceAgent(Agent agent, final Concentrator concentrator, Activity concentratorBidActivity,
			Activity concentratorPriceActivity, final DependencyEngine engine, TimeUnit unit) {
		Activity deviceAgentActivity = new Activity("DAR bid", agent);
		engine.addActivity(deviceAgentActivity);
		deviceAgentActivity.scheduleAtFixedRate(engine.getCurrentTime(), 1, unit);

		Link agentToConcentrator = Link.create(concentrator, Matcher.class);
		link(deviceAgentActivity, agentToConcentrator, concentratorBidActivity);
		agent.setMatcher((Matcher) agentToConcentrator.getProxy());

		Activity deviceAgentControlActivity = new Activity("DAR control", agent);
		engine.addActivity(deviceAgentControlActivity);

		Link concentratorToAgent = Link.create(agent, Agent.class);
		link(concentratorPriceActivity, concentratorToAgent, deviceAgentControlActivity);
		concentrator.addAgent((Agent) concentratorToAgent.getProxy());
	}

	private static void link(Activity source, Link link, Activity sink) {
		link.addDependency(source);
		sink.addDependency(link);
	}
}
