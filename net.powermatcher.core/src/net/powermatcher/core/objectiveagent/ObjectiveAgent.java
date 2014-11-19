//package net.powermatcher.core.objectiveagent;
//
//import java.util.Map;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import aQute.bnd.annotation.component.Activate;
//import aQute.bnd.annotation.component.Component;
//import aQute.bnd.annotation.component.Deactivate;
//import aQute.bnd.annotation.component.Reference;
//import aQute.bnd.annotation.metatype.Configurable;
//import aQute.bnd.annotation.metatype.Meta;
//import net.powermatcher.api.AgentRole;
//import net.powermatcher.api.MatcherRole;
//import net.powermatcher.api.Session;
//import net.powermatcher.api.TimeService;
//import net.powermatcher.api.data.Bid;
//import net.powermatcher.api.data.Price;
//import net.powermatcher.api.data.PricePoint;
//import net.powermatcher.api.monitoring.IncomingPriceEvent;
//import net.powermatcher.api.monitoring.ObservableAgent;
//import net.powermatcher.api.monitoring.OutgoingBidEvent;
////import net.powermatcher.core.monitoring.BaseObservable;
//
//@Component(designateFactory = ObjectiveAgent.Config.class, immediate = true, provide = { ObservableAgent.class,
//        MatcherRole.class })
//public class ObjectiveAgent extends BaseObservable implements AgentRole {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveAgent.class);
//
//    public static interface Config {
//        @Meta.AD(deflt = "objectiveagent")
//        String agentId();
//
//        @Meta.AD(deflt = "100", description = "The price that the objectiveagent should maintain")
//        double desiredPrice();
//
//        @Meta.AD(deflt = "30", description = "Number of seconds between bid updates")
//        long bidUpdateRate();
//    }
//
//    private ScheduledFuture<?> scheduledFuture;
//
//    private ScheduledExecutorService scheduler;
//
//    private Session session;
//
//    private TimeService timeService;
//
//    private String agentId;
//
//    private Price price;
//
//    private double desiredPrice;
//
//    // /**
//    // * Define the update already published (boolean) field.
//    // * Set to true when an updated bid has already been published from setObjectiveBid
//    // * to avoid duplicate publishing of the same bid from doBidUpdate.
//    // */
//    // Denk dat we idd nog iets moeten dat de prijs is gerelateerd aan het bid.
//    // private boolean updateAlreadyPublished;
//
//    @Activate
//    public void activate(Map<String, Object> properties) {
//        Config config = Configurable.createConfigurable(Config.class, properties);
//        agentId = config.agentId();
//
//        this.desiredPrice = config.desiredPrice();
//
//        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                doBidUpdate();
//            }
//        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);
//
//        LOGGER.info("ObjectiveAgent [{}], activated", config.agentId());
//    }
//
//    @Deactivate
//    public void deactivate() {
//        if (session != null) {
//            session.disconnect();
//        }
//
//        scheduledFuture.cancel(false);
//
//        LOGGER.info("ObjectiveAgent [{}], deactivated", agentId);
//    }
//
//    protected void doBidUpdate() {
//        if (session != null) {
//
//            Bid newBid = new Bid(session.getMarketBasis(), new PricePoint(0, this.desiredPrice
//                    - this.price.getCurrentPrice()));
//
//            LOGGER.debug("updateBid({}) for ObjectiveAgent", newBid);
//            session.updateBid(newBid);
////            this.publishEvent(new OutgoingBidEvent(agentId, session.getSessionId(), timeService.currentDate(), newBid));
//        }
//    }
//
//    @Override
//    public void updatePrice(Price newPrice) {
//        LOGGER.debug("updatePrice({})", newPrice);
//
//        this.price = newPrice;
//
////        publishEvent(new IncomingPriceEvent(agentId, session.getSessionId(), timeService.currentDate(), newPrice));
//
//        LOGGER.debug("Received price update for ObjectiveAgent [{}]", newPrice);
//    }
//
//    @Override
//    public void connectToMatcher(Session session) {
//        this.session = session;
//    }
//
//    @Override
//    public void matcherRoleDisconnected(Session session) {
//        this.session = null;
//    }
//
//    @Reference
//    public void setScheduler(ScheduledExecutorService scheduler) {
//        this.scheduler = scheduler;
//    }
//
//    @Reference
//    public void setTimeService(TimeService timeService) {
//        this.timeService = timeService;
//    }
//
////    @Override
////    public String getObserverId() {
////        return this.agentId;
////    }
//}
