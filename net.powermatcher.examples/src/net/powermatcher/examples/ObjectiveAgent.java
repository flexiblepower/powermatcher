package net.powermatcher.examples;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.ObjectiveEndpoint;
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

@Component(designateFactory = ObjectiveAgent.Config.class, immediate = true, provide = { ObservableAgent.class, ObjectiveEndpoint.class })
public class ObjectiveAgent extends BaseAgent implements ObjectiveEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveAgent.class);

    public static interface Config {
        @Meta.AD(deflt = "objectiveagent")
        String agentId();

        @Meta.AD(deflt = "objectiveauctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "100", description = "The price that the objectiveagent should maintain")
        double desiredPrice();

        @Meta.AD(deflt = "30", description = "Number of seconds between bid updates")
        long bidUpdateRate();
    }

    private ScheduledFuture<?> scheduledFuture;

    private ScheduledExecutorService scheduler;

    private Session session;

    private TimeService timeService;

    private String agentId;

    private Price price;

    private double desiredPrice;

    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        agentId = config.agentId();

        this.desiredPrice = config.desiredPrice();

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("ObjectiveAgent [{}], activated", config.agentId());
    }

    @Deactivate
    public void deactivate() {
        if (session != null) {
            session.disconnect();
        }

        scheduledFuture.cancel(false);

        LOGGER.info("ObjectiveAgent [{}], deactivated", agentId);
    }

    protected void doBidUpdate() {
        if (session != null) {

            Bid newBid = new Bid(session.getMarketBasis(), new PricePoint(0, this.desiredPrice
                    - this.price.getCurrentPrice()));

            LOGGER.debug("updateBid({}) for ObjectiveAgent", newBid);
            
            session.updateBid(newBid);
            
            this.publishEvent(new OutgoingBidEvent(session.getClusterId(), this.getAgentId(), session.getSessionId(),
                    timeService.currentDate(), newBid, Qualifier.AGENT));
        }
    }

    @Override
    public void notifyPriceUpdate(Price newPrice) {
        LOGGER.debug("updatePrice({})", newPrice);

        this.price = newPrice;

        publishEvent(new IncomingPriceEvent(session.getClusterId(), this.getAgentId(), session.getSessionId(),
                timeService.currentDate(), newPrice, Qualifier.AGENT));

        LOGGER.debug("Received price update for ObjectiveAgent [{}]", newPrice);

    }

    @Override
    public Bid handleAggregateBid(Bid aggregatedBid) {
        //Bid newAggregatedBid = aggregatedBid.
                //this.aggregatedBids.getAggregatedBid(this.sessionToMatcher.getMarketBasis());
        // Hier moet nog gekeken worden hoe dit opgelost moet worden als ik er niet uitkom.
//        for (Bid objectiveBid : objectiveBids) {
//            newAggregatedBid = aggregatedBid.aggregate(objectiveBid);
//        }

        Bid newObjectiveAggregatedBid = null;
        newObjectiveAggregatedBid = aggregatedBid.aggregate(aggregatedBid);
        
        return newObjectiveAggregatedBid;
    }
    
    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }
}
