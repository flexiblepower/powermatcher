package net.powermatcher.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.AggregatedBidEvent;
import net.powermatcher.core.BaseAgentEndpoint;

/**
 * An Objective Agent is a special type of PowerMatcher agent, that can be used to utilize the flexibility available in
 * the PowerMatcher cluster. Using an Objective Agent, you could for example sell the flexibility of a cluster on an
 * imbalance market.
 *
 * The Objective Agent is a special type of AgentEndpoint, which doesn't represent a device, but represents business
 * logic. The Objective Agent uses bids to influence the PowerMatcher cluster.
 *
 * For example, an Objective Agent sends an must-run bid with a demand of 10kW. The Auctioneer will always determine the
 * price that achieves a balanced cluster. Since there is no actual load of 10kW, the cluster will start to produce
 * 10kW.
 *
 * This Objective Agent is an (non realistic) example of how you could implement such an agent. Using the
 * ObservableAgent interface the Objective Agent can monitor the aggregated bid of the Auctioneer, and therefore know
 * what flexibility is available in the cluster. This Objective Agent will ask the cluster to produce 1000 Watts by
 * sending a must-run bid, but only if there is enough flexibility available in the cluster. Otherwise it will ask the
 * cluster for no flexbility by sending a must-off bid.
 *
 * @author FAN
 * @version 2.1
 */
@Component(designateFactory = ObjectiveAgent.Config.class, immediate = true, provide = { AgentEndpoint.class,
                                                                                         ObservableAgent.class })
public class ObjectiveAgent
    extends BaseAgentEndpoint
    implements AgentObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveAgent.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(description = "AgentID of this agent", deflt = "objectiveagent")
               String agentId();

        @Meta.AD(description = "AgentID of the Auctioneer", deflt = "auctioneer")
               String auctioneerId();
    }

    /**
     * OSGI configuration meta type with info about the objective agent.
     */
    private Config config;

    /**
     * List to temporarily store ObservableAgents that are received before the component is activated. This list is
     * emptied in the activate method.
     */
    private final List<ObservableAgent> observableAgents = new ArrayList<ObservableAgent>();

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        init(config.agentId(), config.auctioneerId());

        LOGGER.info("Objective agent activated");

        for (ObservableAgent observableAgent : observableAgents) {
            // Retry now the component is activated
            addObservableAgent(observableAgent);
        }
        observableAgents.clear();
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Override
    @Deactivate
    public void deactivate() {
        LOGGER.info("Objective agent deactivated");
    }

    /**
     * Receive a reference to an ObservableAgent and subscribe to the AgentEvents if it is the Auctioneer, otherwise
     * ignore it.
     *
     * Since this method may be called before the component is activated, we put all the received ObservableAgents in a
     * list so we can retry to subscribe after the component is activated
     *
     * @param agent
     *            ObservableAgent that might be the Auctioneer
     */
    @Reference(multiple = true, optional = true, dynamic = true)
    public void addObservableAgent(ObservableAgent agent) {
        if (config == null) {
            // The component is not yet activated, we'll try again in the activate method
            observableAgents.add(agent);
        } else {
            // The component is activated
            if (config.auctioneerId().equals(agent.getAgentId())) {
                agent.addObserver(this);
                LOGGER.info("Objective agent subscribed to aggregated bids from the Auctioneer");
            }
        }
    }

    public void removeObservableAgent(ObservableAgent agent) {
        if (config == null) {
            // The component is not yet activated, we'll try again in the activate method
            observableAgents.remove(agent);
        } else {
            // The component is activated
            if (config.auctioneerId().equals(agent.getAgentId())) {
                agent.removeObserver(this);
                LOGGER.info("Objective agent subscribed to aggregated bids from the Auctioneer");
            }
        }
    }

    /**
     * This specific implementation sends a static {@link Bid} to manipulate the cluster.
     */
    private void handleAggregatedBid(Bid aggregatedBid) {
        LOGGER.info("Received aggregated bid: [{}] ", aggregatedBid.getDemand());
        AgentEndpoint.Status currentStatus = getStatus();
        if (currentStatus.isConnected()) {
            // Can the cluster produce more than the asked amount of energy?
            if (aggregatedBid.getMinimumDemand() < -1000) {
                // The cluster has the ability to produce config.flexibilityToUse() Watts
                // We ask the cluster to produce config.flexibilityToUse() by demanding this amount of energy in an
                // non flexible bid
                LOGGER.info("Asking the cluster to produce 1000W");
                publishBid(Bid.flatDemand(currentStatus.getMarketBasis(), 1000));
            } else {
                // We don't ask anything from the cluster, we send a must off bid
                LOGGER.info("Not asking the cluster to produce 1000W");
                publishBid(Bid.flatDemand(currentStatus.getMarketBasis(), 0));
            }
        }
    }

    @Override
    public void handleAgentEvent(AgentEvent event) {
        // We are only interested in AggregatedBidEvents
        if (event instanceof AggregatedBidEvent) {
            handleAggregatedBid(((AggregatedBidEvent) event).getAggregatedBid());
        }
    }
}
