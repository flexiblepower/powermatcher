package net.powermatcher.core.monitoring.test;

import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;
import net.powermatcher.core.mock.MockBaseObservable;
import net.powermatcher.mock.MockObserver;

import org.junit.Test;

import static org.junit.Assert.*;

public class BaseObservableTest {

    @Test
    public void oneObserverTest() {
        MockObserver observer = new MockObserver();

        MockBaseObservable observable = new MockBaseObservable();
        observable.addObserver(observer);
        observable.publishEvent(createDummyEvent());

        assertTrue(observer.hasReceivedEvent());
    }

    @Test
    public void twoObserversTest() {
        MockObserver observer1 = new MockObserver();
        MockObserver observer2 = new MockObserver();

        MockBaseObservable observable = new MockBaseObservable();
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

        MockBaseObservable observable = new MockBaseObservable();
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

        MockBaseObservable observable = new MockBaseObservable();
        observable.addObserver(observer1);
        observable.removeObserver(observer1);
        observable.removeObserver(observer1);
    }

    @Test
    public void noObserversTest() {
        MockBaseObservable observable = new MockBaseObservable();

        MockObserver observer1 = new MockObserver();
        observable.addObserver(observer1);
        observable.removeObserver(observer1);

        observable.publishEvent(createDummyEvent());
        assertFalse(observer1.hasReceivedEvent());
    }

    private UpdateEvent createDummyEvent() {
        MarketBasis marketBasis = new MarketBasis("Electicity", "EUR", 10, 0.0, 100.0);
        return new IncomingPriceUpdateEvent("agent1", "sessionId", new Date(), new Price(marketBasis, 0));
    }
}
