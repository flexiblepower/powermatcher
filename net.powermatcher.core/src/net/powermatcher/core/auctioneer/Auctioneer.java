package net.powermatcher.core.auctioneer;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseMatcherEndpoint;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.concentrator.Concentrator;

import org.flexiblepower.context.FlexiblePowerContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * <p>
 * This class represents an {@link Auctioneer} component which will receive all {@link Bid} of other agents as a single
 * {@link Bid} or as an aggregate {@link Bid} via one or more {@link Concentrator}.
 * </p>
 *
 * It is responsible for defining and sending the {@link MarketBasis} and calculating the equilibrium based on the
 * {@link Bid} from the different agents in the topology. This equilibrium is communicated to the agents down the
 * hierarchy in the form of price update messages.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = Auctioneer.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, MatcherEndpoint.class })
public class Auctioneer
    extends BaseMatcherEndpoint
    implements MatcherEndpoint {

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String agentId();

        @Meta.AD(deflt = "DefaultCluster")
        String clusterId();

        @Meta.AD(deflt = "electricity", description = "Commodity of the market basis")
        String commodity();

        @Meta.AD(deflt = "EUR", description = "Currency of the market basis")
        String currency();

        @Meta.AD(deflt = "100", description = "Number of price steps in the market basis")
        int priceSteps();

        @Meta.AD(deflt = "0", description = "Minimum price of the market basis")
        double minimumPrice();

        @Meta.AD(deflt = "1", description = "Maximum price of the market basis")
        double maximumPrice();

        @Meta.AD(deflt = "600", description = "Nr of seconds before a bid becomes invalidated")
        int bidTimeout();

        @Meta.AD(deflt = "30", description = "Number of seconds between price updates")
        long priceUpdateRate();
    }

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    private Config config;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, ?> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        super.activate(config.agentId());

        MarketBasis marketBasis = new MarketBasis(config.commodity(),
                                                  config.currency(),
                                                  config.priceSteps(),
                                                  config.minimumPrice(),
                                                  config.maximumPrice());

        configure(marketBasis, config.clusterId());
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Override
    public void setContext(FlexiblePowerContext context) {
        super.setContext(context);

        Runnable command = new Runnable() {
            @Override
            public void run() {
                AggregatedBid aggregatedBid = aggregate();
                Price newPrice = determinePrice(aggregatedBid);
                publishPrice(newPrice, aggregatedBid);
            }
        };
        scheduledFuture = context.getScheduler().scheduleAtFixedRate(command,
                                                                     0,
                                                                     config.priceUpdateRate(),
                                                                     TimeUnit.SECONDS);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        scheduledFuture.cancel(false);
        unconfigure();
    }

    /**
     * This method determines the {@link Price}, given the current aggregated {@link Bid}.
     *
     * @param aggregatedBid
     *            the aggregated {@link Bid} used to determin the {@link Price}
     * @return the calculated {@link Price}
     */
    protected Price determinePrice(Bid aggregatedBid) {
        return aggregatedBid.calculateIntersection(0);
    }
}
