package net.powermatcher.core.concentrator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.PeakShavingEvent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.BidCacheSnapshot;
import net.powermatcher.core.auctioneer.Auctioneer;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * <p>
 * This class represents a {@link PeakShavingConcentrator} component where several instances can be created.
 * </p>
 *
 * <p>
 * The {@link PeakShavingConcentrator} receives {@link Bid} from the agents and forwards this in an aggregate
 * {@link Bid} up in the hierarchy to a {@link PeakShavingConcentrator} or to the {@link Auctioneer}. It will receive
 * price updates from the {@link Auctioneer} and forward them to its connected agents. TODO: add PeakShavingConcentrator
 * comment.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = PeakShavingConcentrator.Config.class, immediate = true, provide = {
                                                                                                 ObservableAgent.class,
                                                                                                 MatcherEndpoint.class,
                                                                                                 AgentEndpoint.class })
public class PeakShavingConcentrator
    extends BaseAgent
    implements
    MatcherEndpoint, AgentEndpoint {

    private static final Logger LOGGER = LoggerFactory
                                                      .getLogger(PeakShavingConcentrator.class);

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

        @Meta.AD(deflt = "-10", description = "The floor constraint")
        double floor();

        @Meta.AD(deflt = "10", description = "The ceiling constraint")
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
    protected ArrayBid aggregatedBidIn = null;

    /**
     * The current aggregated bid propagated to the concentrator's parent
     */
    protected ArrayBid aggregatedBidOut = null;

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
    private final Set<Session> sessionToAgents = new HashSet<Session>();

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

    /**
     * OSGI ConfigurationAdmin, stores bundle configuration data persistently.
     */
    private ConfigurationAdmin configurationAdmin;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        processConfig(properties);

        aggregatedBids = new BidCache(timeService,
                                      config.bidTimeout());

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                try {
                    doBidUpdate();
                } catch (IllegalStateException | IllegalArgumentException e) {
                    LOGGER.error(
                                 "doBidUpate failed for Concentrator "
                                         + config.agentId(), e);
                }
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    /**
     * This method processes the data in the Config interfaces of this class
     *
     * @param properties
     *            the configuration properties
     */
    protected void processConfig(Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);

        setAgentId(config.agentId());
        setDesiredParentId(config.desiredParentId());
        setCeiling(config.ceiling());
        setFloor(config.floor());

        if (ceiling < floor) {
            try {
                for (Configuration c : configurationAdmin
                                                         .listConfigurations(null)) {

                    String agentId = (String) c.getProperties().get("agentId");
                    if (agentId.equals(getAgentId())) {
                        c.delete();
                    }
                }
            } catch (IOException | InvalidSyntaxException e) {
                LOGGER.error(e.getMessage());
            }

            throw new IllegalArgumentException(
                                               "The floor constraint shouldn't be higher than the ceiling constraint");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean connectToAgent(Session session) {
        if (sessionToMatcher == null) {
            return false;
        }

        sessionToAgents.add(session);
        session.setMarketBasis(sessionToMatcher.getMarketBasis());

        aggregatedBids.updateBid(session.getAgentId(),
                                 new ArrayBid.Builder(sessionToMatcher.getMarketBasis())
                                                                                        .setDemand(0).build());
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentEndpointDisconnected(Session session) {
        // Find session
        if (!sessionToAgents.remove(session)) {
            return;
        }

        aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]",
                    session.getSessionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void connectToMatcher(Session session) {
        sessionToMatcher = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void matcherEndpointDisconnected(Session session) {
        for (Session agentSession : sessionToAgents
                                                   .toArray(new Session[sessionToAgents.size()])) {
            agentSession.disconnect();
        }
        sessionToMatcher = null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, Bid newBid) {
        if (!sessionToAgents.contains(session)) {
            throw new IllegalStateException("No session found");
        }
        if (!newBid.getMarketBasis().equals(
                                            sessionToMatcher.getMarketBasis())) {
            throw new IllegalArgumentException(
                                               "Marketbasis new bid differs from marketbasis auctioneer");
        }

        publishEvent(new IncomingBidEvent(session.getClusterId(),
                                          config
                                                .agentId(),
                                          session.getSessionId(),
                                          timeService.currentDate(),
                                          "agentId",
                                          newBid,
                                          Qualifier.AGENT));

        // Update agent in aggregatedBids
        aggregatedBids.updateBid(session.getAgentId(), newBid);

        LOGGER.info("Received from session [{}] bid update [{}] ",
                    session.getSessionId(), newBid);
    }

    /**
     * Sends the new aggregated bid to the {@link MatcherEndpoint}
     */
    protected synchronized void doBidUpdate() {
        if (sessionToMatcher != null) {
            ArrayBid aggregatedBid = aggregatedBids.getAggregatedBid(
                                                                     sessionToMatcher.getMarketBasis(), true);

            // Peakshaving call
            ArrayBid transformedBid = transformAggregatedBid(aggregatedBid);
            sessionToMatcher.updateBid(transformedBid);

            publishEvent(new PeakShavingEvent(config.agentId(),
                                              getClusterId(), timeService.currentDate(), floor,
                                              ceiling, aggregatedBid.getDemand(),
                                              transformedBid.getDemand(), null, null));

            publishEvent(new OutgoingBidEvent(sessionToMatcher.getClusterId(),
                                              config.agentId(), sessionToMatcher.getSessionId(),
                                              timeService.currentDate(), aggregatedBid, Qualifier.MATCHER));

            LOGGER.debug("Updating aggregated bid [{}]", aggregatedBid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        if (priceUpdate == null) {
            LOGGER.error("PriceUpdate cannot be null");
            return;
        }

        LOGGER.debug("Received price update [{}]", priceUpdate);
        publishEvent(new IncomingPriceUpdateEvent(sessionToMatcher
                                                                  .getClusterId(),
                                                  config.agentId(),
                                                  sessionToMatcher
                                                                  .getSessionId(),
                                                  timeService.currentDate(),
                                                  priceUpdate,
                                                  Qualifier.AGENT));

        // Find bidCacheSnapshot belonging to the newly received price update
        BidCacheSnapshot bidCacheSnapshot = aggregatedBids
                                                          .getMatchingSnapshot(priceUpdate.getBidNumber());
        if (bidCacheSnapshot == null) {
            // ignore price and log warning
            LOGGER.warn(
                        "Received a price update for a bid that I never sent, id: {}",
                        priceUpdate.getBidNumber());
            return;
        }

        // Publish new price to connected agents
        for (Session session : sessionToAgents) {
            Integer originalAgentBid = bidCacheSnapshot.getBidNumbers().get(
                                                                            session.getAgentId());
            if (originalAgentBid == null) {
                // ignore price for this agent and log warning
                LOGGER.warn(
                            "No matching bid for agent with id: {} in snapShot",
                            session.getAgentId());
                continue;
            }

            PriceUpdate agentPrice = new PriceUpdate(priceUpdate.getPrice(),
                                                     originalAgentBid);

            // call peakshaving code.
            PriceUpdate adjustedPrice = adjustPrice(agentPrice);
            session.updatePrice(adjustedPrice);

            publishEvent(new PeakShavingEvent(config.agentId(),
                                              getClusterId(), timeService.currentDate(), floor,
                                              ceiling, new double[0], new double[0],
                                              agentPrice.getPrice(), adjustedPrice.getPrice()));

            publishEvent(new OutgoingPriceUpdateEvent(session
                                                             .getClusterId(),
                                                      config.agentId(),
                                                      session
                                                             .getSessionId(),
                                                      timeService.currentDate(),
                                                      priceUpdate,
                                                      Qualifier.MATCHER));
        }
    }

    /**
     * Transforms the {@link Price} by peakshaving.
     *
     * @param newPriceUpdate
     *            The {@link PriceUpdate} whose {@link Price} has to be adjusted.
     * @return the {@link PriceUpdate} containing the adjusted {@link Price}.
     */
    private synchronized PriceUpdate adjustPrice(
                                                 final PriceUpdate newPriceUpdate) {
        // if the given price is null, the price can't be adjusted, so we let
        // the framework handle the null price
        if (newPriceUpdate == null) {
            return null;
        }

        // we can only adjust if we know the aggregated bid curve
        if (aggregatedBidIn == null) {
            priceIn = newPriceUpdate.getPrice();
            priceOut = newPriceUpdate.getPrice();
            return newPriceUpdate;
        }

        // transform prices to indices
        int priceInIndex = newPriceUpdate.getPrice().toPriceStep()
                                         .getPriceStep();
        int priceOutIndex = priceInIndex;

        // create a copy of the aggregated bid to calculate the demand function
        // and add the uncontrolled flow to the demand function
        ArrayBid demandFunction = new ArrayBid(aggregatedBidIn);
        double uncontrolledFlow = getUncontrolledFlow();
        if (!Double.isNaN(uncontrolledFlow)) {
            demandFunction = demandFunction.transpose(uncontrolledFlow);
        }

        // determine the expected allocation
        double allocation = demandFunction.getDemandAt(newPriceUpdate
                                                                     .getPrice().toPriceStep());

        // Adjust the price so that the allocation is within flow
        // constraints (taking uncontrolled flow into account)
        if (!Double.isNaN(ceiling) && allocation > ceiling) {
            priceOutIndex = findFirstIndexOfUnclippedRegion(
                                                            demandFunction.getDemand(), ceiling);

            // if there is no unclipped region we use the lowest price
            if (priceOutIndex == -1) {
                priceOutIndex = demandFunction.getMarketBasis().getPriceSteps() - 1;
            }
        } else if (allocation < floor) {
            priceOutIndex = findLastIndexOfUnclippedRegion(
                                                           demandFunction.getDemand(), floor);

            // if there is no unclipped region we use the highest price
            if (priceOutIndex == -1) {
                priceOutIndex = 0;
            }
        }

        // set the new price
        priceIn = newPriceUpdate.getPrice();
        priceOut = new Price(newPriceUpdate.getPrice().getMarketBasis(),
                             newPriceUpdate.getPrice().getPriceValue());

        return new PriceUpdate(priceOut, newPriceUpdate.getBidNumber());
    }

    /**
     * Transforms the aggregated {@link Bid} by peakshaving.
     *
     * @param newAggregatedBid
     *            The aggregated {@link Bid} that has to be transformed.
     * @return the transformed {@link Bid}.
     */
    private synchronized ArrayBid transformAggregatedBid(
                                                         final ArrayBid newAggregatedBid) {
        // if the given bid is null, then there is nothing to transform, so we
        // let the next framework handle the null bid
        if (newAggregatedBid == null) {
            return null;
        }

        // Copy the aggregated bid to transform into a new aggregated bid
        ArrayBid newAggregatedBidOut = new ArrayBid(newAggregatedBid);

        // add the uncontrolled flow
        if (!Double.isNaN(getUncontrolledFlow())) {
            newAggregatedBidOut = newAggregatedBidOut.transpose(getUncontrolledFlow());
        }

        // clip above the ceiling and blow the floor
        newAggregatedBidOut = clipAbove(newAggregatedBidOut, ceiling);
        newAggregatedBidOut = clipBelow(newAggregatedBidOut, floor);

        // remove the uncontrolled flow again
        if (!Double.isNaN(getUncontrolledFlow())) {
            newAggregatedBidOut = newAggregatedBidOut.transpose(-getUncontrolledFlow());
        }

        // remember what the incoming and outgoing aggregated bids were.
        aggregatedBidIn = newAggregatedBid;
        aggregatedBidOut = newAggregatedBidOut;

        return aggregatedBidOut;
    }

    /**
     * @return the difference between the measured flow and the allocation for the cluster.
     */
    private synchronized double getUncontrolledFlow() {
        // the uncontrolled flow can only be calculated if the measured flow is
        // known
        if (Double.isNaN(measuredFlow)) {
            return Double.NaN;
        }

        // retrieve the current allocation
        double allocation = getAllocation();

        // if that isn't known the uncontrolled flow can't be calculated
        if (Double.isNaN(allocation)) {
            return Double.NaN;
        }

        // calculate the uncontrolled flow using the difference between the
        // measured flow and the allocation for the cluster
        return measuredFlow - allocation;
    }

    /**
     * @return the demand at the value of the priceOut in the bid curve of the aggregated {@link Bid}.
     */
    private synchronized double getAllocation() {
        // calculating an allocation is only feasible if the aggregated bid and
        // current price are known.
        if (aggregatedBidIn == null || priceOut == null) {
            return Double.NaN;
        }

        // use the framework to determine the allocation
        return aggregatedBidIn.getDemandAt(priceOut.toPriceStep());
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
    private ArrayBid clipAbove(final ArrayBid bid, final double ceiling) {
        double[] demand = bid.getDemand();

        // find start of unclipped region
        int start = findFirstIndexOfUnclippedRegion(demand, ceiling);

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
        return new ArrayBid.Builder(bid.getMarketBasis())
                                                         .setBidNumber(bid.getBidNumber()).setDemandArray(demand)
                                                         .build();
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
    private ArrayBid clipBelow(final ArrayBid bid, final double floor) {
        double[] demand = bid.getDemand();

        // find end of unclipped region
        int end = findLastIndexOfUnclippedRegion(demand, floor);

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

        return new ArrayBid.Builder(bid.getMarketBasis())
                                                         .setBidNumber(bid.getBidNumber()).setDemandArray(demand)
                                                         .build();
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
    private int findFirstIndexOfUnclippedRegion(final double[] demandFunction,
                                                final double ceiling) {
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
    private int findLastIndexOfUnclippedRegion(final double[] demandFunction,
                                               final double floor) {
        for (int i = demandFunction.length - 1; i >= 0; i--) {
            if (demandFunction[i] >= floor) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @param the
     *            new {@link TimeService} implementation.
     */
    @Override
    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Override
    @Reference
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @param the
     *            new value of configurationAdmin.
     */
    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

    /**
     * @return the current value of ceiling.
     */
    public double getCeiling() {
        return ceiling;
    }

    /**
     * @param the
     *            new value for ceiling
     */
    public void setCeiling(double ceiling) {
        this.ceiling = ceiling;
    }

    /**
     * @return the current value of floor.
     */
    public double getFloor() {
        return floor;
    }

    /**
     * @param the
     *            new value for floor
     */
    public void setFloor(double floor) {
        this.floor = floor;
    }
}
