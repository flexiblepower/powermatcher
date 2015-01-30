package net.powermatcher.core.concentrator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.WhitelistableMatcherEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.WhitelistEvent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.BidCacheSnapshot;
import net.powermatcher.core.auctioneer.Auctioneer;

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
 * This class represents a {@link Concentrator} component where several instances can be created.
 * </p>
 *
 * <p>
 * The {@link Concentrator} receives {@link Bid} from the agents and forwards this in an aggregate {@link Bid} up in the
 * hierarchy to a {@link Concentrator} or to the {@link Auctioneer}. It will receive price updates from the
 * {@link Auctioneer} and forward them to its connected agents.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = Concentrator.Config.class,
           immediate = true,
           provide = {
                      ObservableAgent.class, MatcherEndpoint.class, AgentEndpoint.class,
                      WhitelistableMatcherEndpoint.class })
public class Concentrator
    extends BaseAgent
    implements MatcherEndpoint,
    AgentEndpoint, WhitelistableMatcherEndpoint {
    private static final Logger LOGGER = LoggerFactory
                                                      .getLogger(Concentrator.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator")
        String agentId();

        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "60", description = "Number of seconds between bid updates")
        long bidUpdateRate();

        @Meta.AD(description = "Valid agents for a connection to the cluster")
        List<String> whiteListAgents();
    }

    /**
     * The schedule that is running the bid updates. This is created in the {@link #activate(Map)} method and cancelled
     * in the {@link #deactivate()} method.
     */
    private ScheduledFuture<?> bidUpdateSchedule;

    /**
     * {@link Session} object for connecting to matcher
     */
    private Session sessionToMatcher;

    /**
     * The {@link Bid} cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    private BidCache aggregatedBids;

    /**
     * Holds the sessions from the agents.
     */
    private final Set<Session> sessionToAgents = new HashSet<Session>();

    /**
     * OSGI configuration meta type with info about the concentrator.
     */
    protected Config config;

    /**
     * Holds the whitelist agents
     */
    private List<String> validAgents = new ArrayList<String>();

    /**
     * OSGI ConfigurationAdmin, stores bundle configuration data persistently.
     */
    private static ConfigurationAdmin configurationAdmin;

    /**
     * Holds the service pid of a bundle from the ConfigurationAdmin
     */
    private String servicePid;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        setServicePid((String) properties.get("service.pid"));
        setAgentId(config.agentId());
        setDesiredParentId(config.desiredParentId());
        setWhiteListAgents(config.whiteListAgents());

        // Since the cleanup is never called, the expiration time is useless
        // TODO: how should we deal with this cleanup?
        aggregatedBids = new BidCache(timeService, 0);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        bidUpdateSchedule.cancel(false);

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void connectToMatcher(Session session) {
        sessionToMatcher = session;
        setClusterId(session.getClusterId());
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
        setClusterId(null);
        sessionToMatcher = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean connectToAgent(Session session) {
        if (sessionToMatcher == null) {
            return false;
        }

        if (validAgents.size() == 0
            || validAgents.contains(session.getAgentId())) {
            sessionToAgents.add(session);
            session.setMarketBasis(sessionToMatcher.getMarketBasis());

            aggregatedBids
                          .updateBid(session.getAgentId(),
                                     new ArrayBid.Builder(
                                                          sessionToMatcher.getMarketBasis())
                                                                                            .setDemand(0)
                                                                                            .build());
            LOGGER.info("Agent connected with session [{}]",
                        session.getSessionId());
            return true;
        } else {
            LOGGER.warn("Agent [{}] is not on whitelist, reject connection",
                        session.getAgentId());
            publishEvent(new WhitelistEvent(getAgentId(),
                                            session.getAgentId(), getClusterId(),
                                            timeService.currentDate()));

            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void agentEndpointDisconnected(Session session) {
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
    public synchronized void handleBidUpdate(Session session, Bid newBid)
                                                                         throws IllegalStateException,
                                                                         IllegalArgumentException {

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
     * {@inheritDoc}
     */
    @Override
    public synchronized void handlePriceUpdate(PriceUpdate priceUpdate) {
        if (priceUpdate == null) {
            String message = "Price cannot be null";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
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

            PriceUpdate agentPriceUpdate = new PriceUpdate(
                                                           priceUpdate.getPrice(), originalAgentBid);

            session.updatePrice(agentPriceUpdate);

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
     * Aggregates the bids and sends them to the matching agent.
     */
    protected synchronized void doBidUpdate() {
        if (sessionToMatcher != null && isInitialized()) {
            Bid aggregatedBid = aggregatedBids.getAggregatedBid(
                                                                sessionToMatcher.getMarketBasis(), true);

            aggregatedBid = transformBid(aggregatedBid);

            sessionToMatcher.updateBid(aggregatedBid);
            publishEvent(new OutgoingBidEvent(sessionToMatcher.getClusterId(),
                                              config.agentId(), sessionToMatcher.getSessionId(), now(),
                                              aggregatedBid, Qualifier.MATCHER));

            LOGGER.debug("Updating aggregated bid [{}]", aggregatedBid);
        }
    }

    /**
     * This method should be overridden when the bid that will be sent has to be changed.
     *
     * @param aggregatedBid
     *            The (input) aggregated bid as calculated normally (the sum of all the bids of the agents).
     * @return The bid that will be sent to the matcher that is connected to this {@link Concentrator}.
     */
    protected Bid transformBid(Bid aggregatedBid) {
        return aggregatedBid;
    }

    /**
     * Used to update the whiteListAgents property in the {@link ConfigurationAdmin}'s properties. It is set to the
     * current value of validAgents.
     */
    private synchronized void updateConfigurationAdmin() {
        try {
            Configuration config = configurationAdmin
                                                     .getConfiguration(getServicePid());

            Dictionary<String, Object> properties = config.getProperties();
            properties.put("whiteListAgents", validAgents);

            config.update(properties);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * @param the
     *            new value of configurationAdmin.
     */
    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        Concentrator.configurationAdmin = configurationAdmin;
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Override
    public void setExecutorService(ScheduledExecutorService scheduler) {
        executorService = scheduler;
        bidUpdateSchedule = executorService.scheduleAtFixedRate(
                                                                new Runnable() {
                                                                    /**
                                                                     * {@inheritDoc}
                                                                     */
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            doBidUpdate();
                                                                        } catch (IllegalStateException
                                                                                 | IllegalArgumentException e) {
                                                                            LOGGER.error("doBidUpate failed for Concentrator "
                                                                                                 + config.agentId(),
                                                                                         e);
                                                                        }
                                                                    }
                                                                },
                                                                0,
                                                                config.bidUpdateRate(),
                                                                TimeUnit.SECONDS);
    }

    /**
     * @return the current value of validAgents.
     */
    protected List<String> getWhiteListAgents() {
        return validAgents;
    }

    /**
     * @param the
     *            new value of whiteListAgents
     */
    protected void setWhiteListAgents(List<String> whiteListAgents) {
        validAgents = whiteListAgents;

        // ConfigAdmin will sometimes generate a filter with 1 empty element.
        // Ignore it.
        if (whiteListAgents != null && !whiteListAgents.isEmpty()
            && whiteListAgents.get(0).isEmpty()) {
            validAgents = new ArrayList<String>();
        }
    }

    @Override
    public boolean canEqual(Object other) {
        return other instanceof Concentrator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        Concentrator other = (Concentrator) ((obj instanceof Concentrator) ? obj
                                                                          : null);
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        // TODO Find a better way to implement this equals.
        // This ones fails when any field is null
        return aggregatedBids.equals(other.aggregatedBids)
               && sessionToMatcher.equals(other.sessionToMatcher)
               && aggregatedBids.equals(other.aggregatedBids)
               && sessionToAgents.equals(other.sessionToAgents)
               && validAgents.equals(other.validAgents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode()
               + 211
               * ((aggregatedBids == null ? 0 : aggregatedBids.hashCode())
                  + (servicePid == null ? 0 : servicePid.hashCode())
                  + sessionToMatcher.hashCode()
                  + (aggregatedBids == null ? 0 : aggregatedBids
                                                                .hashCode()) + (validAgents == null ? 0
                                                                                                   : validAgents.hashCode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getWhiteList() {
        return validAgents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWhiteList(List<String> whiteList) {
        if (whiteList == null) {
            validAgents.clear();
            validAgents.add("");
            updateConfigurationAdmin();
            return;
        }

        for (String agent : whiteList) {
            if (!validAgents.contains(agent)) {
                validAgents.add(agent);
            }
        }

        updateConfigurationAdmin();
    }
}
