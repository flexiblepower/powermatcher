package net.powermatcher.examples;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link StoringObserver} is an example implementation of the {@link BaseObserver} interface. You can add
 * {@link ObservableAgent}s and it can receive {@link AgentEvent}s from them.
 * 
 * @author FAN
 * @version 2.0
 */
@Component(immediate = true, designate = StoringObserver.Config.class)
public class StoringObserver
    implements AgentObserver {

    private final Map<String, OutgoingBidEvent> outgoingBidEvents = new HashMap<String, OutgoingBidEvent>();
    private final Map<String, IncomingBidEvent> incomingBidEvents = new HashMap<String, IncomingBidEvent>();

    private final Map<String, OutgoingPriceUpdateEvent> outgoingPriceEvents = new HashMap<String, OutgoingPriceUpdateEvent>();
    private final Map<String, IncomingPriceUpdateEvent> incomingPriceEvents = new HashMap<String, IncomingPriceUpdateEvent>();

    /**
     * This interface describes the configuration of this {@link StoringObserver}. It defines the filter for the
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
     * Adds an {@link ObservableAgent} to this {@link StoringObserver}. This will register itself with the object.
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
     * Removes an {@link ObservableAgent} from this {@link StoringObserver}. This will unregister itself with the
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
     * Stores {@link OutgoingBidEvent} and {@link OutgoingPriceUpdateEvent} in an internal list.
     * 
     * Price and Bid events are stored in a separate list.
     * 
     * @param event
     *            The {@link AgentEvent} that is to be stored.
     */
    @Override
    public void handleAgentEvent(AgentEvent event) {
        if (event instanceof OutgoingPriceUpdateEvent) {
            OutgoingPriceUpdateEvent priceEvent = (OutgoingPriceUpdateEvent) event;
            outgoingPriceEvents.put(priceEvent.getAgentId(), priceEvent);
        } else if (event instanceof OutgoingBidEvent) {
            OutgoingBidEvent bidEvent = (OutgoingBidEvent) event;
            outgoingBidEvents.put(bidEvent.getAgentId(), bidEvent);
        } else if (event instanceof IncomingPriceUpdateEvent) {
            IncomingPriceUpdateEvent priceEvent = (IncomingPriceUpdateEvent) event;
            incomingPriceEvents.put(priceEvent.getAgentId(), priceEvent);
        } else if (event instanceof IncomingBidEvent) {
            IncomingBidEvent bidEvent = (IncomingBidEvent) event;
            incomingBidEvents.put(bidEvent.getAgentId(), bidEvent);
        }
    }

    public Map<String, OutgoingBidEvent> getOutgoingBidEvents() {
        return new HashMap<String, OutgoingBidEvent>(outgoingBidEvents);
    }

    public Map<String, IncomingBidEvent> getIncomingBidEvents() {
        return new HashMap<String, IncomingBidEvent>(incomingBidEvents);
    }

    public Map<String, OutgoingPriceUpdateEvent> getOutgoingPriceEvents() {
        return new HashMap<String, OutgoingPriceUpdateEvent>(outgoingPriceEvents);
    }

    public Map<String, IncomingPriceUpdateEvent> getIncomingPriceEvents() {
        return new HashMap<String, IncomingPriceUpdateEvent>(incomingPriceEvents);
    }

    public void clearEvents() {
        incomingBidEvents.clear();
        incomingPriceEvents.clear();
        outgoingBidEvents.clear();
        outgoingPriceEvents.clear();
    }
}
