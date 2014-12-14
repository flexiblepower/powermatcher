package net.powermatcher.examples;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseDeviceAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Freezer.Config.class, immediate = true, provide = { ObservableAgent.class,
        AgentEndpoint.class })
public class Freezer extends BaseDeviceAgent implements AgentEndpoint {
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

    private ScheduledFuture<?> scheduledFuture;
    private ScheduledExecutorService scheduler;
    private TimeService timeService;
    private double minimumDemand;
    private double maximumDemand;

    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.setAgentId(config.agentId());
        this.setDesiredParentId(config.desiredParentId());
        this.minimumDemand = config.minimumDemand();
        this.maximumDemand = config.maximumDemand();
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);
        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    @Deactivate
    public void deactivate() {
        Session session = getSession();
        if (session != null) {
            session.disconnect();
        }
        scheduledFuture.cancel(false);
        LOGGER.info("Agent [{}], deactivated", this.getAgentId());
    }

    protected void doBidUpdate() {
        if (getMarketBasis() != null) {
            double demand = minimumDemand + (maximumDemand - minimumDemand) * generator.nextDouble();

            PricePoint pricePoint1 = new PricePoint(new Price(getMarketBasis(), 
                    getMarketBasis().getMinimumPrice()), demand);
            PricePoint pricePoint2 = new PricePoint(new Price(getMarketBasis(), 
                    getMarketBasis().getMaximumPrice()), minimumDemand);

            Bid newBid = createBid(pricePoint1, pricePoint2);
            LOGGER.debug("updateBid({})", newBid);
            publishBid(newBid);
        }
    }

    @Override
    public void updatePrice(PriceUpdate newPrice) {
        LOGGER.debug("Received price update [{}], current bidNr = {}", newPrice, getCurrentBidNr());
        super.updatePrice(newPrice);
    }

    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    protected Date now() {
        return timeService.currentDate();
    }
}
