package net.powermatcher.test.helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link TestingObserver} is an example implementation of the {@link BaseObserver} interface. You can add
 * {@link ObservableAgent}s and it can receive {@link AgentEvent}s from them.
 *
 * @author FAN
 * @version 2.0
 */
@Component(immediate = true, designate = TestingObserver.Config.class)
public class TestingObserver
    implements AgentObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingObserver.class);

    private final BlockingQueue<AgentEvent> eventQueue = new LinkedBlockingQueue<AgentEvent>();

    /**
     * This interface describes the configuration of this {@link TestingObserver}. It defines the filter for the
     * {@link ObservableAgent}s that are needed.
     */
    public static interface Config {
        @Meta.AD(required = false,
                 deflt = "",
                 description = "The LDAP filter for the ObservableAgents that we want to monitor. "
                               + "E.g. '(agentId=auctioneer)'")
        String observableAgent_filter();
    }

    /**
     * Adds an {@link ObservableAgent} to this {@link TestingObserver}. This will register itself with the object.
     * Normally this should be called by the OSGi platform using DS. This method has no effect if this was already
     * registered.
     *
     * @param observable
     *            The {@link ObservableAgent} that it should be registered on.
     */
    @Reference(dynamic = true, multiple = true, optional = true)
    public void addObservableAgent(ObservableAgent observable) {
        observable.addObserver(this);
    }

    /**
     * Removes an {@link ObservableAgent} from this {@link TestingObserver}. This will unregister itself with the
     * object. Normally this should be called by the OSGi platform using DS. This method has no effect if this wasn't
     * already registered.
     *
     * @param observable
     *            The {@link ObservableAgent} that it should unregister from.
     */
    public void removeObservableAgent(ObservableAgent observable) {
        observable.removeObserver(this);
    }

    /**
     * Stores {@link OutgoingBidUpdateEvent} and {@link OutgoingPriceUpdateEvent} in an internal list.
     *
     * Price and Bid events are stored in a separate list.
     *
     * @param event
     *            The {@link AgentEvent} that is to be stored.
     */
    @Override
    public void handleAgentEvent(AgentEvent event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException ex) {
            // Should neven happen?
            throw new AssertionError("Unexpected InterruptedException", ex);
        }
    }

    public void expectBidsFrom(long timeout, String... agentIds) throws InterruptedException {
        LOGGER.debug("expectedBidsFrom({})", Arrays.toString(agentIds));
        clear();

        long deadline = System.currentTimeMillis() + timeout * 1000;
        long waitTime = timeout * 1000;

        Set<String> agentIdSet = new HashSet<String>(Arrays.asList(agentIds));
        Set<String> stillLookingFor = new HashSet<String>(agentIdSet);
        while (waitTime > 0) {
            AgentEvent agentEvent = eventQueue.poll(waitTime, TimeUnit.MILLISECONDS);
            if (agentEvent == null) {
                break;
            }

            if (agentEvent instanceof OutgoingBidUpdateEvent) {
                if (!agentIdSet.contains(agentEvent.getAgentId())) {
                    throw new AssertionError("Got a unexpected bid update from: " + agentEvent.getAgentId());
                }

                LOGGER.debug("Correctly detected bid from {}", agentEvent.getAgentId());
                stillLookingFor.remove(agentEvent.getAgentId());
                if (stillLookingFor.isEmpty()) {
                    LOGGER.info("Found all expected bids");
                    return; // Normal completion!
                }
            }

            waitTime = deadline - System.currentTimeMillis();
        }

        throw new AssertionError("Did not receive bid updates from " + stillLookingFor);
    }

    public void
            expectReceivingPriceUpdate(int timeout, Price expectedPrice, String... agentIds) throws InterruptedException {
        LOGGER.debug("expectReceivingPriceUpdate({}, {})", expectedPrice, Arrays.toString(agentIds));
        clear();

        long deadline = System.currentTimeMillis() + timeout * 1000;
        long waitTime = timeout * 1000;
        Price wrongPrice = null;

        Set<String> agentIdSet = new HashSet<String>(Arrays.asList(agentIds));
        Set<String> stillLookingFor = new HashSet<String>(agentIdSet);
        while (waitTime > 0) {
            AgentEvent agentEvent = eventQueue.poll(waitTime, TimeUnit.MILLISECONDS);
            if (agentEvent == null) {
                break;
            }

            if (agentEvent instanceof IncomingPriceUpdateEvent) {
                if (!agentIdSet.contains(agentEvent.getAgentId())) {
                    throw new AssertionError("Got a unexpected incoming price update from: " + agentEvent.getAgentId());
                }

                Price detectedPrice = ((IncomingPriceUpdateEvent) agentEvent).getPriceUpdate().getPrice();
                if (detectedPrice.equals(expectedPrice)) {
                    stillLookingFor.remove(agentEvent.getAgentId());
                    if (stillLookingFor.isEmpty()) {
                        LOGGER.info("Found all expected price updates");
                        return; // Normal completion!
                    }
                } else {
                    wrongPrice = detectedPrice;
                    LOGGER.debug("Detected wrong price, waiting a bit longer: {}", detectedPrice);
                }
            }
            waitTime = deadline - System.currentTimeMillis();
        }

        if (wrongPrice != null) {
            throw new AssertionError("Only detected a wrong price of " + wrongPrice + ", expected " + expectedPrice);
        } else {
            throw new AssertionError("Did not detect incoming price updates from " + stillLookingFor);
        }
    }

    public void expectNothing(int timeout) throws InterruptedException {
        LOGGER.debug("expectNothing()");
        clear();
        AgentEvent event = eventQueue.poll(timeout, TimeUnit.SECONDS);
        if (event != null) {
            throw new AssertionError("Expected no more event, got: " + event);
        }
    }

    private void clear() throws InterruptedException {
        Thread.sleep(50); // For (aggregated) bids that could happen for a short while, because of disconnects
        eventQueue.clear();
    }

    public void clearEvents() {
        eventQueue.clear();
    }
}
