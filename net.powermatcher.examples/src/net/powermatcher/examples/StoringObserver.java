package net.powermatcher.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final Map<String, List<OutgoingBidEvent>> outgoingBidEvents = new HashMap<String, List<OutgoingBidEvent>>();
    private final Map<String, List<IncomingBidEvent>> incomingBidEvents = new HashMap<String, List<IncomingBidEvent>>();

    private final Map<String, List<OutgoingPriceUpdateEvent>> outgoingPriceEvents = new HashMap<String, List<OutgoingPriceUpdateEvent>>();
    private final Map<String, List<IncomingPriceUpdateEvent>> incomingPriceEvents = new HashMap<String, List<IncomingPriceUpdateEvent>>();

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
            if (!outgoingPriceEvents.containsKey(priceEvent.getAgentId())) {
                outgoingPriceEvents.put(priceEvent.getAgentId(), new ArrayList<OutgoingPriceUpdateEvent>());
            }

            outgoingPriceEvents.get(priceEvent.getAgentId()).add(priceEvent);
        } else if (event instanceof OutgoingBidEvent) {
            OutgoingBidEvent bidEvent = (OutgoingBidEvent) event;
            if (!outgoingBidEvents.containsKey(bidEvent.getAgentId())) {
                outgoingBidEvents.put(bidEvent.getAgentId(), new ArrayList<OutgoingBidEvent>());
            }

            outgoingBidEvents.get(bidEvent.getAgentId()).add(bidEvent);
        } else if (event instanceof IncomingPriceUpdateEvent) {
            IncomingPriceUpdateEvent priceEvent = (IncomingPriceUpdateEvent) event;
            if (!incomingPriceEvents.containsKey(priceEvent.getAgentId())) {
                incomingPriceEvents.put(priceEvent.getAgentId(), new ArrayList<IncomingPriceUpdateEvent>());
            }

            incomingPriceEvents.get(priceEvent.getAgentId()).add(priceEvent);
        } else if (event instanceof IncomingBidEvent) {
            IncomingBidEvent bidEvent = (IncomingBidEvent) event;
            if (!incomingBidEvents.containsKey(bidEvent.getAgentId())) {
                incomingBidEvents.put(bidEvent.getAgentId(), new ArrayList<IncomingBidEvent>());
            }

            incomingBidEvents.get(bidEvent.getAgentId()).add(bidEvent);
        }
    }

    public List<OutgoingBidEvent> getOutgoingBidEvents(String agentId) {
        if (!outgoingBidEvents.containsKey(agentId)) {
            return new ArrayList<OutgoingBidEvent>();
        }

        return new ArrayList<OutgoingBidEvent>(outgoingBidEvents.get(agentId));
    }

    public List<IncomingBidEvent> getIncomingBidEvents(String agentId) {
        if (!incomingBidEvents.containsKey(agentId)) {
            return new ArrayList<IncomingBidEvent>();
        }

        return new ArrayList<IncomingBidEvent>(incomingBidEvents.get(agentId));
    }

    public List<OutgoingPriceUpdateEvent> getOutgoingPriceUpdateEvents(String agentId) {
        if (!outgoingPriceEvents.containsKey(agentId)) {
            return new ArrayList<OutgoingPriceUpdateEvent>();
        }

        return new ArrayList<OutgoingPriceUpdateEvent>(outgoingPriceEvents.get(agentId));
    }

    public List<IncomingPriceUpdateEvent> getIncomingPriceUpdateEvents(String agentId) {
        if (!incomingPriceEvents.containsKey(agentId)) {
            return new ArrayList<IncomingPriceUpdateEvent>();
        }

        return new ArrayList<IncomingPriceUpdateEvent>(incomingPriceEvents.get(agentId));
    }

    public void clearEvents() {
        incomingBidEvents.clear();
        incomingPriceEvents.clear();
        outgoingBidEvents.clear();
        outgoingPriceEvents.clear();
    }
}
