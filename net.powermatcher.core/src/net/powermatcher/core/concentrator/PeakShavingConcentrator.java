package net.powermatcher.core.concentrator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.IncomingBidEvent;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.api.monitoring.OutgoingPriceEvent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.BidCacheSnapshot;
import net.powermatcher.core.auctioneer.Auctioneer;

/**
 * <p>
 * This class represents a {@link PeakShavingConcentrator} component where several instances can be created.
 * </p>
 * 
 * <p>
 * The {@link PeakShavingConcentrator} receives {@link Bid} from the agents and forwards this in an aggregate
 * {@link Bid} up in the hierarchy to a {@link PeakShavingConcentrator} or to the {@link Auctioneer}. It will receive
 * price updates from the {@link Auctioneer} and forward them to its connected agents. TODO: add PeakShavingConcentrator
 * comment
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(designateFactory = PeakShavingConcentrator.Config.class, immediate = true, provide = {
        ObservableAgent.class, MatcherEndpoint.class, AgentEndpoint.class })
public class PeakShavingConcentrator extends BaseAgent implements MatcherEndpoint, AgentEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeakShavingConcentrator.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "600", description = "Nr of seconds before a bid becomes invalidated")
        int bidTimeout();

        @Meta.AD(deflt = "60", description = "Number of seconds between bid updates")
        long bidUpdateRate();

        @Meta.AD(deflt = "peakshavingconcentrator")
        String agentId();

        @Meta.AD(deflt = "peakshavingconcentrator")
        String matcherId();

        @Meta.AD(deflt = "-10")
        double floor();

        @Meta.AD(deflt = "10")
        double ceiling();
    }

    /**
     * TimeService that is used for obtaining real or simulated time.
     */
    private TimeService timeService;

    /**
     * Minimum power level applied in 'peak shaving'
     */
    protected double floor = -Double.MAX_VALUE;

    /**
     * Maximum power level applied in 'peak shaving'
     */
    protected double ceiling = Double.MAX_VALUE;

    /**
     * The current aggregated bid, based on the bids received from the concentrator's children
     */
    protected Bid aggregatedBidIn = null;

    /**
     * The current aggregated bid propagated to the concentrator's parent
     */
    protected Bid aggregatedBidOut = null;

    /**
     * OSGI configuration meta type with info about the concentrator.
     */
    private Config config;

    /**
     * The {@link Bid} cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    private BidCache aggregatedBids;

    /**
     * Holds the sessions from the agents.
     */
    private Set<Session> sessionToAgents = new HashSet<Session>();

    /**
     * Scheduler that can schedule commands to run after a given delay, or to execute periodically.
     */
    private ScheduledExecutorService scheduler;

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * {@link Session} object for connecting to matcher
     */
    private Session sessionToMatcher;

    /**
     * Measured flow as reported via the peak shaving interface
     */
    protected double measuredFlow = Double.NaN;

    /**
     * The current price received from the concentrator's parent
     */
    protected Price priceIn = null;

    /**
     * The current price propagated to the concentrator's children
     */
    protected Price priceOut = null;

    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);

        this.setAgentId(config.agentId());
        this.setDesiredParentId(config.desiredParentId());
        this.setCeiling(config.ceiling());
        this.setFloor(config.floor());

        if (this.ceiling < this.floor) {
            throw new IllegalArgumentException("The floor constraint shouldn't be higher than the ceiling constraint");
        }

        this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());

        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    doBidUpdate();
                } catch (IllegalStateException | IllegalArgumentException e) {
                    LOGGER.error("doBidUpate failed for Concentrator " + config.agentId(), e);
                }
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    @Deactivate
    public void deactivate() {
        scheduledFuture.cancel(false);

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    @Override
    public synchronized boolean connectToAgent(Session session) {
        if (this.sessionToMatcher == null) {
            return false;
        }

        this.sessionToAgents.add(session);
        session.setMarketBasis(this.sessionToMatcher.getMarketBasis());
        session.setClusterId(this.sessionToMatcher.getClusterId());

        this.aggregatedBids.updateBid(session.getAgentId(), new Bid(this.sessionToMatcher.getMarketBasis()));
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    @Override
    public void agentEndpointDisconnected(Session session) {
        // Find session
        if (!sessionToAgents.remove(session)) {
            return;
        }

        this.aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
    }

    @Override
    public synchronized void connectToMatcher(Session session) {
        this.sessionToMatcher = session;
    }

    @Override
    public synchronized void matcherEndpointDisconnected(Session session) {
        for (Session agentSession : sessionToAgents.toArray(new Session[sessionToAgents.size()])) {
            agentSession.disconnect();
        }
        this.sessionToMatcher = null;

    }

    // call: sessionImpl.updateBid(Bid)
    @Override
    public void updateBid(Session session, Bid newBid) {

        if (!sessionToAgents.contains(session)) {
            throw new IllegalStateException("No session found");
        }
        if (!newBid.getMarketBasis().equals(this.sessionToMatcher.getMarketBasis())) {
            throw new IllegalArgumentException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        this.publishEvent(new IncomingBidEvent(session.getClusterId(), config.agentId(), session.getSessionId(),
                timeService.currentDate(), "agentId", newBid, Qualifier.AGENT));

        // Update agent in aggregatedBids
        this.aggregatedBids.updateBid(session.getAgentId(), newBid);

        // TODO: peakshaving call here? handleAggregatedBidUpdate(newBid)?

        LOGGER.info("Received from session [{}] bid update [{}] ", session.getSessionId(), newBid);
    }

    /**
     * sends the aggregatedbids to the matcher this method has temporarily been made public due to issues with the
     * scheduler. TODO fix this asap
     */
    // call: in run:activate()
    public synchronized void doBidUpdate() {
        if (sessionToMatcher != null) {
            Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.sessionToMatcher.getMarketBasis());

            // Peakshaving call
            Bid transformedBid = transformAggregatedBid(aggregatedBid);
            this.sessionToMatcher.updateBid(transformedBid);

            // old code
            // this.sessionToMatcher.updateBid(aggregatedBid);
            publishEvent(new OutgoingBidEvent(sessionToMatcher.getClusterId(), config.agentId(),
                    sessionToMatcher.getSessionId(), timeService.currentDate(), aggregatedBid, Qualifier.MATCHER));

            LOGGER.debug("Updating aggregated bid [{}]", aggregatedBid);
        }
    }

    // sessionImpl.updatePrice(Price): dit is een methode die wordt aangeroepen door nieuwe code
    @Override
    public void updatePrice(Price newPrice) {
        if (newPrice == null) {
            LOGGER.error("Price cannot be null");
            return;
        }

        LOGGER.debug("Received price update [{}]", newPrice);
        this.publishEvent(new IncomingPriceEvent(sessionToMatcher.getClusterId(), this.config.agentId(),
                this.sessionToMatcher.getSessionId(), timeService.currentDate(), newPrice, Qualifier.AGENT));

        // Find bidCacheSnapshot belonging to the newly received price update
        BidCacheSnapshot bidCacheSnapshot = this.aggregatedBids.getMatchingSnapshot(newPrice.getBidNumber());
        if (bidCacheSnapshot == null) {
            // ignore price and log warning
            return;
        }

        // Publish new price to connected agents
        for (Session session : this.sessionToAgents) {
            Integer originalAgentBid = bidCacheSnapshot.getBidNumbers().get(session.getAgentId());
            if (originalAgentBid == null) {
                // ignore price for this agent and log warning
                continue;
            }

            Price agentPrice = new Price(newPrice.getMarketBasis(), newPrice.getCurrentPrice(), originalAgentBid);

            // call peakshaving code.
            Price adjustedPrice = adjustPrice(agentPrice);
            session.updatePrice(adjustedPrice);

            // session.updatePrice(agentPrice);

            this.publishEvent(new OutgoingPriceEvent(session.getClusterId(), this.config.agentId(), session
                    .getSessionId(), timeService.currentDate(), newPrice, Qualifier.MATCHER));

        }
    }

    // @Override
    protected synchronized Price adjustPrice(final Price newPrice) {
        // if the given price is null, the price can't be adjusted, so we let
        // the framework handle the null price
        if (newPrice == null) {
            return null;
        }

        // we can only adjust if we know the aggregated bid curve
        if (this.aggregatedBidIn == null) {
            this.priceIn = newPrice;
            return this.priceOut = newPrice;
        }

        // transform prices to indices
        int priceInIndex = newPrice.getMarketBasis().toPriceStep(newPrice.getCurrentPrice());
        int priceOutIndex = priceInIndex;

        // create a copy of the aggregated bid to calculate the demand function
        // and add the uncontrolled flow to the demand function
        Bid demandFunction = new Bid(this.aggregatedBidIn);
        double uncontrolledFlow = this.getUncontrolledFlow();
        if (!Double.isNaN(uncontrolledFlow)) {
            demandFunction = demandFunction.transpose(uncontrolledFlow);
        }

        // determine the expected allocation
        double allocation = demandFunction.getDemand(newPrice.getCurrentPrice());

        // Adjust the price so that the allocation is within flow
        // constraints (taking uncontrolled flow into account)
        if (!Double.isNaN(this.ceiling) && allocation > this.ceiling) {
            priceOutIndex = this.findFirstIndexOfUnclippedRegion(demandFunction.getDemand(), this.ceiling);

            // if there is no unclipped region we use the lowest price
            if (priceOutIndex == -1) {
                priceOutIndex = demandFunction.getMarketBasis().getPriceSteps() - 1;
            }
        } else if (allocation < this.floor) {
            priceOutIndex = this.findLastIndexOfUnclippedRegion(demandFunction.getDemand(), this.floor);

            // if there is no unclipped region we use the highest price
            if (priceOutIndex == -1) {
                priceOutIndex = 0;
            }
        }

        // set the new price
        this.priceIn = newPrice;
        return this.priceOut = new Price(newPrice.getMarketBasis(), newPrice.getMarketBasis().toPrice(priceOutIndex));
    }

    // @Override
    protected synchronized Bid transformAggregatedBid(final Bid newAggregatedBid) {
        // if the given bid is null, then there is nothing to transform, so we
        // let the next framework handle the null bid
        if (newAggregatedBid == null) {
            return null;
        }

        // Copy the aggregated bid to transform into a new aggregated bid
        Bid newAggregatedBidOut = new Bid(newAggregatedBid);

        // add the uncontrolled flow
        if (!Double.isNaN(this.getUncontrolledFlow())) {
            newAggregatedBidOut = newAggregatedBidOut.transpose(this.getUncontrolledFlow());
        }

        // clip above the ceiling and blow the floor
        newAggregatedBidOut = this.clipAbove(newAggregatedBidOut, this.ceiling);
        newAggregatedBidOut = this.clipBelow(newAggregatedBidOut, this.floor);

        // remove the uncontrolled flow again
        if (!Double.isNaN(this.getUncontrolledFlow())) {
            newAggregatedBidOut = newAggregatedBidOut.transpose(-this.getUncontrolledFlow());
        }

        // remember what the incoming and outgoing aggregated bids were.
        this.aggregatedBidIn = newAggregatedBid;
        this.aggregatedBidOut = newAggregatedBidOut;

        return this.aggregatedBidOut;
    }

    // public synchronized void setFlowConstraints(final double newCeiling, final double newFloor) {
    // if (Double.isNaN(newCeiling) || Double.isNaN(newFloor)) {
    // throw new IllegalArgumentException("The floor and ceiling must be a number (and not NaN).");
    // }
    //
    // if (newCeiling < newFloor) {
    // throw new IllegalArgumentException("The floor constraint shouldn't be higher than the ceiling constraint!");
    // }
    //
    // // store the new ceiling and floor
    // // and if either or both of them are infinity, use the heighest or
    // // lowest possible number in stead to ensure good results from the
    // // algorithm
    // this.ceiling = Double.isInfinite(newCeiling) ? Double.MAX_VALUE : newCeiling;
    // this.floor = Double.isInfinite(newFloor) ? Double.MIN_VALUE : newFloor;
    //
    // if (this.immediateUpdate) {
    // // do an update of the aggregated bid (clip with the new ceiling and
    // // floor).
    // if (this.aggregatedBidIn != null) {
    // //this.handleAggregatedBidUpdate(this.aggregatedBidIn);
    // transformAggregatedBid(this.aggregatedBidIn);
    // }
    //
    // // and update the price towards the concentrator's children (ensuring
    // // that the new constraints are met).
    // if (this.priceIn != null) {
    //
    // // call peakshaving code.
    // adjustPrice(this.priceIn);
    //
    // //this.updatePriceInfo(this.priceIn);
    // }
    // }
    // }

    private synchronized double getUncontrolledFlow() {
        // the uncontrolled flow can only be calculated if the measured flow is
        // known
        if (Double.isNaN(this.measuredFlow)) {
            return Double.NaN;
        }

        // retrieve the current allocation
        double allocation = this.getAllocation();

        // if that isn't known the uncontrolled flow can't be calculated
        if (Double.isNaN(allocation)) {
            return Double.NaN;
        }

        // calculate the uncontrolled flow using the difference between the
        // measured flow and the allocation for the cluster
        return this.measuredFlow - allocation;
    }

    private synchronized double getAllocation() {
        // calculating an allocation is only feasible if the aggregated bid and
        // current price are known.
        if (this.aggregatedBidIn == null || this.priceOut == null) {
            return Double.NaN;
        }

        // use the framework to determine the allocation
        return this.aggregatedBidIn.getDemand(this.priceOut.getCurrentPrice());
    }

    /**
     * Clip a bid such that no power value in the bid exceeds the given ceiling. Any value in the resulting bid will
     * have resulted from the given bid (i.e. no new power level values will have been introduced).
     * 
     * @param bid
     *            The bid to clip.
     * @param ceiling
     *            The ceiling to clip the bid to.
     * @return The clipped bid.
     */
    private Bid clipAbove(final Bid bid, final double ceiling) {
        double[] demand = bid.getDemand();

        // find start of unclipped region
        int start = this.findFirstIndexOfUnclippedRegion(demand, ceiling);

        // if there is no unclipped region we use the last (lowest value)
        if (start == -1) {
            Arrays.fill(demand, demand[demand.length - 1]);
        } else {
            // replace part above ceiling with first point in unclipped region
            double firstUnclippedPoint = demand[start];
            for (int i = 0; i < start; i++) {
                demand[i] = firstUnclippedPoint;
            }
        }
        return new Bid(bid.getMarketBasis(), demand);
    }

    /**
     * Clip a bid such that no power value in the bid exceeds the given floor. Any value in the resulting bid will have
     * resulted from the given bid (i.e. no new power level values will have been introduced).
     * 
     * @param bid
     *            The bid to clip.
     * @param floor
     *            The floor to clip the bid to.
     * @return The clipped bid.
     */
    private Bid clipBelow(final Bid bid, final double floor) {
        double[] demand = bid.getDemand();

        // find end of unclipped region
        int end = this.findLastIndexOfUnclippedRegion(demand, floor);

        if (end == -1) {
            // if there is no unclipped region we use the first (highest value)
            Arrays.fill(demand, demand[0]);
        } else {
            // replace part below floor with last point in unclipped region
            double lastUnclippedPoint = demand[end];
            for (int i = end + 1; i < demand.length; i++) {
                demand[i] = lastUnclippedPoint;
            }
        }
        return new Bid(bid.getMarketBasis(), demand);
    }

    /**
     * Finds the first index in a demand function for which the allocation doesn't exceed the given ceiling. Starting at
     * the lowest price (index), the first index of the region which won't be clipped is the first value in the demand
     * function which is lower than or equal to the ceiling.
     * 
     * @param demandFunction
     *            The demand function as an array of power flow values, where positive flow is demand.
     * @param floor
     *            The ceiling which defines the maximum value the unclipped region has.
     * @return The first index of the unclipped region or -1 if there is no region which isn't below the ceiling.
     */
    private int findFirstIndexOfUnclippedRegion(final double[] demandFunction, final double ceiling) {
        for (int i = 0; i < demandFunction.length; i++) {
            if (demandFunction[i] <= ceiling) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the first index in a demand function for which the allocation doesn't exceed the given floor; the last
     * index of the region which won't be clipped is the last value in the demand function which is greater than or
     * equal to the ceiling.
     * 
     * @param demandFunction
     *            The demand function as an array of power flow values, where positive flow is demand.
     * @param floor
     *            The floor which defines the minimum value the unclipped region has.
     * @return The last index of the unclipped region or -1 if there is no region which isn't above the floor.
     */
    private int findLastIndexOfUnclippedRegion(final double[] demandFunction, final double floor) {
        for (int i = demandFunction.length - 1; i >= 0; i--) {
            if (demandFunction[i] >= floor) {
                return i;
            }
        }

        return -1;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    protected double getCeiling() {
        return ceiling;
    }

    protected void setCeiling(double ceiling) {
        this.ceiling = ceiling;
    }

    protected double getFloor() {
        return floor;
    }

    protected void setFloor(double floor) {
        this.floor = floor;
    }
}
