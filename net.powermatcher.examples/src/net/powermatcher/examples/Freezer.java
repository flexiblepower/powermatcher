package net.powermatcher.examples;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.core.BaseDeviceAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link Freezer} is a implementation of a {@link BaseDeviceAgent}. It represents a dummy freezer. {@link Freezer}
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
    extends BaseDeviceAgent
    implements AgentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(Freezer.class);

    private static Random generator = new Random();

    public static interface Config {
        @Meta.AD(deflt = "concentrator")
        String desiredParentId();

        @Meta.AD(deflt = "freezer")
        String agentId();

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
        setAgentId(config.agentId());
        setDesiredParentId(config.desiredParentId());

        minimumDemand = config.minimumDemand();
        maximumDemand = config.maximumDemand();

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        Session session = getSession();
        if (session != null) {
            session.disconnect();
        }
        scheduledFuture.cancel(false);
        LOGGER.info("Agent [{}], deactivated", getAgentId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doBidUpdate() {
        if (getMarketBasis() != null) {
            double demand = minimumDemand + (maximumDemand - minimumDemand)
                            * generator.nextDouble();

            PricePoint pricePoint1 = new PricePoint(new Price(getMarketBasis(),
                                                              getMarketBasis().getMinimumPrice()), demand);
            PricePoint pricePoint2 = new PricePoint(new Price(getMarketBasis(),
                                                              getMarketBasis().getMaximumPrice()), minimumDemand);

            Bid newBid = createBid(pricePoint1, pricePoint2);
            LOGGER.debug("updateBid({})", newBid);
            publishBid(newBid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        LOGGER.debug("Received price update [{}], current bidNr = {}",
                     priceUpdate, getBidNumberGenerator().get());
        publishEvent(new IncomingPriceUpdateEvent(getClusterId(),
                                                  getAgentId(),
                                                  getSession().getSessionId(),
                                                  now(),
                                                  priceUpdate));
    }

    @Override
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
        scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                doBidUpdate();
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);
    }
}
