package net.powermatcher.simulator.prototype.dependencyengine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.WindowConstants;

import net.powermatcher.simulator.prototype.pmcore.Agent;
import net.powermatcher.simulator.prototype.pmcore.Auctioneer;
import net.powermatcher.simulator.prototype.pmcore.Concentrator;
import net.powermatcher.simulator.prototype.pmcore.DeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.Matcher;
import net.powermatcher.simulator.prototype.scenario.AuctioneerNodeDescriptor;
import net.powermatcher.simulator.prototype.scenario.ClusterDescriptor;
import net.powermatcher.simulator.prototype.scenario.ConcentratorNodeDescriptor;
import net.powermatcher.simulator.prototype.scenario.DeviceAgentNodeDescriptor;
import net.powermatcher.simulator.prototype.scenario.NodeDescriptor;
import net.powermatcher.simulator.prototype.scenario.Scenario;
import net.powermatcher.simulator.prototype.viz.PriceVisualizationAgent;
import net.powermatcher.simulator.prototype.viz.Visualization;

public class ActivityLinkFactory {
	public static void main(String[] args) {
		Scenario scenario = Scenario.getExample1();
		DependencyEngine engine = new DependencyEngine(new Date(0), 1,
				TimeUnit.MINUTES);

		for (ClusterDescriptor cd : scenario.getClusters()) {
			new ActivityLinkFactory().generateLinkedActivities(cd, engine);
		}
		engine.start();
	}

	// TODO remove dependency on engine?
	public void generateLinkedActivities(ClusterDescriptor cluster,
			DependencyEngine engine) {
		TimeUnit unit = TimeUnit.MINUTES;

		AuctioneerNodeDescriptor root = (AuctioneerNodeDescriptor) cluster
				.getRoot();
		Auctioneer auctioneer = (Auctioneer) createNode(root);

		// create visualization
		Visualization visualization = new Visualization(engine);
		visualization.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		visualization.setVisible(true);
		auctioneer.addAgent(new PriceVisualizationAgent(engine, visualization));

		// create auctioneer price activity
		Activity auctioneerActivity = new Activity("AA  price", auctioneer);
		auctioneerActivity
				.scheduleAtFixedRate(engine.getCurrentTime(), 1, unit);
		engine.addActivity(auctioneerActivity);

		addChildren(engine, root, auctioneer, auctioneerActivity,
				auctioneerActivity, unit);
	}

	private void addChildren(DependencyEngine engine, NodeDescriptor parent,
			Matcher parentMatcher, Activity bidActivity,
			Activity priceActivity, TimeUnit unit) {
		for (NodeDescriptor child : parent.getChildren()) {
			if (child instanceof ConcentratorNodeDescriptor) {
				addConcentrator(engine, parentMatcher, bidActivity,
						priceActivity, unit, child);
			} else if (child instanceof DeviceAgentNodeDescriptor) {
				addDeviceAgent(engine, parentMatcher, bidActivity,
						priceActivity, unit, child);
			}
		}
	}

	private void addConcentrator(DependencyEngine engine,
			Matcher parentMatcher, Activity parentBidActivity,
			Activity parentPriceActivity, TimeUnit unit, NodeDescriptor child) {
		// create concentrator
		final Concentrator concentrator = (Concentrator) createNode(child);
		concentrator.setTimeSource(engine);

		// create concentrator bidding activity
		Activity concentratorBidActivity = new Activity("CA  bid", concentrator);
		concentratorBidActivity.scheduleAtFixedRate(engine.getCurrentTime(), 1,
				unit);
		engine.addActivity(concentratorBidActivity);

		// link concentrator to auctioneer
		Link upstream = Link.create(parentMatcher, Matcher.class);
		link(concentratorBidActivity, upstream, parentBidActivity);
		concentrator.setMatcher((Matcher) upstream.getProxy());

		// create concentrator pricing activity
		Activity concentratorPriceActivity = new Activity("CA  price",
				concentrator);
		engine.addActivity(concentratorPriceActivity);

		// link auctioneer to concentrator
		Link downstream = Link.create(concentrator, Agent.class);
		link(parentPriceActivity, downstream, concentratorPriceActivity);
		parentMatcher.addAgent((Agent) downstream.getProxy());

		addChildren(engine, child, concentrator, concentratorBidActivity,
				concentratorPriceActivity, unit);
	}

	private void addDeviceAgent(DependencyEngine engine, Matcher parentMatcher,
			Activity bidActivity, Activity priceActivity, TimeUnit unit,
			NodeDescriptor child) {
		// create concentrator
		final DeviceAgent deviceAgent = (DeviceAgent) createNode(child);
		deviceAgent.setTimeSource(engine);

		Activity deviceAgentBidActivity = new Activity("DA  bid", deviceAgent);
		engine.addActivity(deviceAgentBidActivity);
		deviceAgentBidActivity.scheduleAtFixedRate(engine.getCurrentTime(), 1,
				unit);

		Link upstream = Link.create(parentMatcher, Matcher.class);
		link(deviceAgentBidActivity, upstream, bidActivity);
		deviceAgent.setMatcher((Matcher) upstream.getProxy());

		Activity deviceAgentControlActivity = new Activity("DA  control",
				deviceAgent);
		engine.addActivity(deviceAgentControlActivity);

		Link downstream = Link.create(deviceAgent, Agent.class);
		link(priceActivity, downstream, deviceAgentControlActivity);
		parentMatcher.addAgent((Agent) downstream.getProxy());
	}

	private static void link(Activity source, Link link, Activity sink) {
		link.addDependency(source);
		sink.addDependency(link);
	}

	private Object createNode(NodeDescriptor node) {
		try {
			Constructor<?> constructor = Class.forName(
					node.getAgentComponentName()).getConstructor(String.class);
			return constructor.newInstance(node.getConfiguration().properties.get("id"));
		} catch (SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
