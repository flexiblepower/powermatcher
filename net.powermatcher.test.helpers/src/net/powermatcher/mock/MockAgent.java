package net.powermatcher.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.Agent;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

import org.flexiblepower.context.FlexiblePowerContext;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class MockAgent
    implements Agent, AgentEndpoint, ObservableAgent {

    private final Map<String, Object> agentProperties;
    private PriceUpdate lastPriceUpdate;
    protected Session session;
    private String desiredParentId;

    /**
     * Collection of {@link Observer} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    public MockAgent(String agentId) {
        agentProperties = new HashMap<String, Object>();
        agentProperties.put("agentId", agentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToMatcher(Session session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void matcherEndpointDisconnected(Session session) {
        this.session = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        lastPriceUpdate = priceUpdate;
    }

    public void sendBid(Bid newBid) {
        session.updateBid(newBid);
    }

    /**
     * @return the current value of lastPriceUpdate.
     */
    public PriceUpdate getLastPriceUpdate() {
        return lastPriceUpdate;
    }

    /**
     * @return the current value of agentProperties.
     */
    public Map<String, Object> getAgentProperties() {
        return agentProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentId() {
        return (String) agentProperties.get("agentId");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterId() {
        return session == null ? null : session.getClusterId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDesiredParentId() {
        return desiredParentId;
    }

    public void setDesiredParentId(String desiredParentId) {
        this.desiredParentId = desiredParentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(AgentObserver observer) {
        observers.remove(observer);
    }

    /**
     * @return the current value of session.
     */
    public Session getSession() {
        return session;
    }

    public void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            observer.handleAgentEvent(event);
        }
    }

    @Override
    public void setContext(FlexiblePowerContext context) {
        // TODO Auto-generated method stub

    }

}
