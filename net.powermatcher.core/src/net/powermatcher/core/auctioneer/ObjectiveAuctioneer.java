package net.powermatcher.core.auctioneer;

import java.util.Map;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.concentrator.Concentrator;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

/**
 * <p>
 * This class represents an {@link ObjectiveAuctioneer} component which will receive all {@link Bid} of other agents as
 * a single {@link Bid} or as an aggregate {@link Bid} via one or more {@link Concentrator}. If ObjectiveAgent are
 * active, the {@link ObjectiveAuctioneer} will also receive a {@link Bid} from the ObjectiveAgent as a single
 * {@link Bid} .
 * </p>
 *
 * <p>
 * It is responsible for defining and sending the {@link MarketBasis} and calculating the equilibrium based on the
 * {@link Bid} from the different agents in the topology and the objective agent. This equilibrium is communicated to
 * the agents down the hierarchy in the form of price update messages and to the objective agent.
 *
 * In order of aggregation the {@link Bid} from the device agents and objective agents, the {@link ObjectiveAuctioneer}
 * will first aggregate the device agents bid and secondly aggregate the {@link Bid} from the objective agent. After the
 * aggregation the {@link ObjectiveAuctioneer} will determine the price and sends it to the {@link Concentrator} /
 * device agents and the objective agent.
 * </p>
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = ObjectiveAuctioneer.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, MatcherEndpoint.class })
public class ObjectiveAuctioneer
    extends Auctioneer {

    public interface Config
        extends Auctioneer.Config {
    }

    /**
     * Holds the objective agent
     */
    private ObjectiveEndpoint objectiveEndpoint;

    /**
     * Used to inject an {@link ObjectiveEndpoint} instance into this class.
     *
     * @param objectiveEndpoint
     *            the new {@link ObjectiveEndpoint}
     */
    @Reference(dynamic = true, optional = true)
    public void addObjectiveEndpoint(ObjectiveEndpoint objectiveEndpoint) {
        this.objectiveEndpoint = objectiveEndpoint;
    }

    /**
     * Removes the current {@link ObjectiveEndpoint}.
     *
     * @param objectiveEndpoint
     *            the {@link ObjectiveEndpoint} that will be removed.
     */
    public void removeObjectiveEndpoint(ObjectiveEndpoint objectiveEndpoint) {
        if (this.objectiveEndpoint == objectiveEndpoint) {
            this.objectiveEndpoint = null;
            LOGGER.debug("Removed objective agent");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Activate
    @Override
    public void activate(final Map<String, ?> properties) {
        super.activate(properties);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void performUpdate(AggregatedBid aggregatedBid) {
        ObjectiveEndpoint ep = objectiveEndpoint;
        if (ep != null) {
            Bid bid = ep.handleAggregateBid(aggregatedBid);
            Price price = bid.calculateIntersection(0);
            ep.notifyPrice(price);
            publishPrice(price, aggregatedBid);
        } else {
            super.performUpdate(aggregatedBid);
        }
    }
}
