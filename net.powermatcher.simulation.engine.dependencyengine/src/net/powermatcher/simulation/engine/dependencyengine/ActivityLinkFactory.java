package net.powermatcher.simulation.engine.dependencyengine;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.simulation.configuration.AuctioneerNodeDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.FPAIDeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.MarketBasisDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ComponentManager;
import net.powermatcher.simulation.logging.Broker;

// TODO this class should be prettier
public class ActivityLinkFactory {
	private Set<Composer<?>> compositionManagers = new HashSet<Composer<?>>();
	private Broker broker;
	private DependencyEngine engine;
	private ComponentManager componentManager;
	private MarketBasis marketBasis;

	public ActivityLinkFactory(DependencyEngine engine, MarketBasisDescriptor mbd, Broker broker,
			ComponentManager componentManager) {
		this.engine = engine;
		this.broker = broker;
		this.componentManager = componentManager;
		this.marketBasis = this.createMarketBasis(mbd);
	}

	private MarketBasis createMarketBasis(MarketBasisDescriptor marketBasisDescriptor) {
		return new MarketBasis(marketBasisDescriptor.getCommodity(), marketBasisDescriptor.getCurrency(),
				marketBasisDescriptor.getPriceSteps(), marketBasisDescriptor.getMinimumPrice(),
				marketBasisDescriptor.getMaximumPrice(), 1, 0);
	}

	public Set<Composer<?>> generateLinkedActivities(ClusterDescriptor cluster) throws ComponentCreationException {
		try {
			this.addAuctioneerAndChildren(cluster);
		} catch (ComponentCreationException e) {
			this.compositionManagers.clear();
			throw e;
		}

		for (Composer<?> nodeComposer : this.compositionManagers) {
			for (Activity activity : nodeComposer.getActivities()) {
				this.engine.addActivity(activity);
			}
		}

		return this.compositionManagers;
	}

	private void addAuctioneerAndChildren(ClusterDescriptor cluster) throws ComponentCreationException {
		AuctioneerNodeDescriptor auctioneerNodeDescriptor = (AuctioneerNodeDescriptor) cluster.getRoot();
		AuctioneerComposer auctioneerComposer = new AuctioneerComposer(auctioneerNodeDescriptor,
				this.engine.getStartTime());
		this.compositionManagers.add(auctioneerComposer);
		auctioneerComposer.createNode(this.componentManager);
		auctioneerComposer.initializeNode(this.componentManager, this.engine, this.marketBasis, this.broker);

		this.addChildren(auctioneerComposer);
	}

	private void addChildren(AgentComposer<? extends MatcherAgent> parentComposer) throws ComponentCreationException {
		for (NodeDescriptor<?> child : parentComposer.getNodeDescriptor().getChildren()) {
			if (child instanceof ConcentratorNodeDescriptor) {
				this.addConcentratorAndChildren(parentComposer, (ConcentratorNodeDescriptor) child);
			} else if (child instanceof FPAIDeviceAgentNodeDescriptor) {
				this.addFPAIDeviceAgentAndChildren(parentComposer, (FPAIDeviceAgentNodeDescriptor) child);
			} else if (child instanceof DeviceAgentNodeDescriptor) {
				this.addDeviceAgent(parentComposer, (DeviceAgentNodeDescriptor) child);
			}
		}
	}

	private void addConcentratorAndChildren(AgentComposer<? extends MatcherAgent> parentComposer,
			ConcentratorNodeDescriptor concentratorDescriptor) throws ComponentCreationException {
		ConcentratorComposer concentratorComposer = new ConcentratorComposer(concentratorDescriptor,
				this.engine.getStartTime());
		this.compositionManagers.add(concentratorComposer);
		concentratorComposer.createNode(this.componentManager);
		concentratorComposer.initializeNode(this.componentManager, this.engine, this.marketBasis, this.broker);

		this.bindAgentToMatcher(concentratorComposer, parentComposer);

		this.addChildren(concentratorComposer);
	}

	private void addDeviceAgent(Composer<? extends MatcherAgent> parentComposer,
			DeviceAgentNodeDescriptor agentDescriptor) throws ComponentCreationException {
		DeviceAgentComposer deviceAgentComposer = new DeviceAgentComposer(agentDescriptor, this.engine.getStartTime());
		this.compositionManagers.add(deviceAgentComposer);
		deviceAgentComposer.createNode(this.componentManager);
		deviceAgentComposer.initializeNode(this.componentManager, this.engine, this.marketBasis, this.broker);

		this.bindAgentToMatcher(deviceAgentComposer, parentComposer);
	}

	private void addFPAIDeviceAgentAndChildren(Composer<? extends MatcherAgent> parentComposer,
			FPAIDeviceAgentNodeDescriptor agentDescriptor) throws ComponentCreationException {
		FPAIDeviceAgentComposer fpaiDeviceAgentComposer = new FPAIDeviceAgentComposer(agentDescriptor,
				this.engine.getStartTime());
		this.compositionManagers.add(fpaiDeviceAgentComposer);
		fpaiDeviceAgentComposer.createNode(this.componentManager);
		fpaiDeviceAgentComposer.initializeNode(this.componentManager, this.engine, this.marketBasis, this.broker);

		this.bindAgentToMatcher(fpaiDeviceAgentComposer, parentComposer);
	}

	private void bindAgentToMatcher(Composer<? extends Agent> agent, Composer<? extends MatcherAgent> matcher) {
		Link downstream = Link.create(agent.getNode(), DownMessagable.class);
		Link upstream = Link.create(matcher.getNode(), UpMessagable.class);

		agent.getNode().bind((UpMessagable) upstream.getProxy());
		agent.attachOutgoingLink(upstream, UpMessagable.class);
		matcher.attachIncommingLink(upstream, UpMessagable.class);

		matcher.getNode().bind((DownMessagable) downstream.getProxy());
		matcher.attachOutgoingLink(downstream, DownMessagable.class);
		agent.attachIncommingLink(downstream, DownMessagable.class);
	}
}
