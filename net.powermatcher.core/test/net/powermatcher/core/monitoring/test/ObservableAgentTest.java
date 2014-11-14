package net.powermatcher.core.monitoring.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.AgentEvent;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.core.mock.MockAgent;
import net.powermatcher.core.mock.MockObserver;

import org.junit.Test;

/**
 * Validates the behavior of ObservableAgent as implemented by BaseAgent.
 */
public class ObservableAgentTest {

    @Test
    public void oneObserverTest() {
        MockObserver observer = new MockObserver();

        MockAgent observable = new MockAgent("agentId");
        observable.addObserver(observer);
        observable.publishEvent(createDummyEvent());

        assertTrue(observer.hasReceivedEvent());
    }

    @Test
    public void twoObserversTest() {
        MockObserver observer1 = new MockObserver();
        MockObserver observer2 = new MockObserver();

        MockAgent observable = new MockAgent("agentId");
        observable.addObserver(observer1);
        observable.addObserver(observer2);
        observable.publishEvent(createDummyEvent());

        assertTrue(observer1.hasReceivedEvent());
        assertTrue(observer2.hasReceivedEvent());
    }

    @Test
    public void removeObserversTest() {
        MockObserver observer1 = new MockObserver();
        MockObserver observer2 = new MockObserver();

        MockAgent observable = new MockAgent("agentId");
        observable.addObserver(observer1);
        observable.addObserver(observer2);
        observable.removeObserver(observer2);
        observable.publishEvent(createDummyEvent());

        assertTrue(observer1.hasReceivedEvent());
        assertFalse(observer2.hasReceivedEvent());
    }

    @Test
    public void duplicateRemoveObserversTest() {
        MockObserver observer1 = new MockObserver();

        MockAgent observable = new MockAgent("agentId");
        observable.addObserver(observer1);
        observable.removeObserver(observer1);
        observable.removeObserver(observer1);
    }

    @Test
    public void noObserversTest() {
        MockAgent observable = new MockAgent("agentId");

        MockObserver observer1 = new MockObserver();
        observable.addObserver(observer1);
        observable.removeObserver(observer1);

        observable.publishEvent(createDummyEvent());
        assertFalse(observer1.hasReceivedEvent());
    }

    private AgentEvent createDummyEvent() {
        MarketBasis marketBasis = new MarketBasis("Electicity", "EUR", 10, 0.0, 100.0);
        return new IncomingPriceEvent("auctioneer", "agent1", "sessionId", new Date(), new Price(marketBasis, 0));
    }
}
