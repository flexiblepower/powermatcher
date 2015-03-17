package net.powermatcher.examples;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import javax.measure.Measure;
import javax.measure.unit.SI;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.core.BaseAgentEndpoint;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link Freezer} is a implementation of a {@link BaseAgentEndpoint}. It represents a dummy freezer. {@link Freezer}
 * creates a {@link PointBid} with random {@link PricePoint}s at a set interval. It does nothing with the returned
 * {@link Price}.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = Freezer.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, AgentEndpoint.class })
public class Freezer
    extends BaseAgentEndpoint
    implements AgentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(Freezer.class);

    private static Random generator = new Random();

    public static interface Config
        extends BaseAgentEndpoint.Config {
        @Override
        @Meta.AD(deflt = "freezer", description = "The unique identifier of the agent")
        String agentId();

        @Override
        @Meta.AD(deflt = "concentrator",
                 description = "The agent identifier of the parent matcher to which this agent should be connected")
        public String desiredParentId();

        @Meta.AD(deflt = "30", description = "Number of seconds between bid updates")
        long bidUpdateRate();

        @Meta.AD(deflt = "100", description = "The mimimum value of the random demand.")
        double minimumDemand();

        @Meta.AD(deflt = "121", description = "The maximum value the random demand.")
        double maximumDemand();
    }

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * The mimimum value of the random demand.
     */
    private double minimumDemand;

    /**
     * The maximum value the random demand.
     */
    private double maximumDemand;

    private Config config;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        activate(config);

        minimumDemand = config.minimumDemand();
        maximumDemand = config.maximumDemand();

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
        scheduledFuture.cancel(false);
        LOGGER.info("Agent [{}], deactivated", getAgentId());
    }

    /**
     * {@inheritDoc}
     */
    void doBidUpdate() {
        if (isConnected()) {
            double demand = minimumDemand + (maximumDemand - minimumDemand)
                            * generator.nextDouble();

            publishBid(new PointBid.Builder(getMarketBasis()).add(getMarketBasis().getMinimumPrice(), demand)
                                                             .add(getMarketBasis().getMaximumPrice(), minimumDemand)
                                                             .build());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        LOGGER.debug("Received price update [{}], current bidNr = {}",
                     priceUpdate, getLastBidUpdate().getBidNumber());
        publishEvent(new IncomingPriceUpdateEvent(getClusterId(),
                                                  getAgentId(),
                                                  getSession().getSessionId(),
                                                  now(),
                                                  priceUpdate));
    }

    @Override
    public void setContext(FlexiblePowerContext context) {
        super.setContext(context);
        scheduledFuture = context.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, Measure.valueOf(0, SI.SECOND), Measure.valueOf(config.bidUpdateRate(), SI.SECOND));
    }
}
