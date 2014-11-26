package net.powermatcher.examples;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.text.html.MinimalHTMLWriter;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.core.BaseAgent;

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
public class Freezer extends BaseAgent implements AgentEndpoint {
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
    private Session session;
    private TimeService timeService;
    private int bidNumber;
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
        if (session != null) {
            session.disconnect();
        }
        scheduledFuture.cancel(false);
        LOGGER.info("Agent [{}], deactivated", this.getAgentId());
    }

    protected void doBidUpdate() {
        if (session != null) {
            if (session.getMarketBasis() != null) {
                //This is a producing agent, so it's -maximumDemand
                double demand = minimumDemand + (maximumDemand - minimumDemand) * generator.nextDouble();
                Bid newBid = new Bid(session.getMarketBasis(), new PricePoint(0, demand));
                incrBidNumber();
                Bid newBidNr = new Bid(newBid, getBidNumber());
                LOGGER.debug("updateBid({})", newBidNr);
                session.updateBid(newBidNr);
                this.publishEvent(new OutgoingBidEvent(session.getClusterId(),this.getAgentId(), session.getSessionId(),
                        timeService.currentDate(), newBidNr, Qualifier.AGENT));
            }
        }
    }

    @Override
    public void updatePrice(Price newPrice) {
        LOGGER.debug("updatePrice({})", newPrice);
        publishEvent(new IncomingPriceEvent(session.getClusterId(), this.getAgentId(), session.getSessionId(), timeService.currentDate(),
                newPrice, Qualifier.AGENT));
        LOGGER.debug("Received price update [{}]", newPrice);
    }

    @Override
    public void connectToMatcher(Session session) {
        this.session = session;
    }

    @Override
    public void matcherEndpointDisconnected(Session session) {
        this.session = null;
    }

    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }
    
    public int getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(int bidNumber) {
        this.bidNumber = bidNumber;
    }

    private void incrBidNumber() {
        this.bidNumber++;
    }
}