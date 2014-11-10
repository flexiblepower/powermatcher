package net.powermatcher.core.monitoring;

import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

import org.junit.Test;
import static org.junit.Assert.*;

public class ObservableBaseTest {

	private class ObserverMock implements Observer {

		private boolean hasReceivedEvent;
		
		@Override
		public void update(UpdateEvent event) {
			this.hasReceivedEvent = true;
		}
	}
	
	private class ObservableBaseMock extends ObservableBase {

		@Override
		public String getObserverId() {
			return "ObservableMock";
		}
		
	}
	
	@Test
	public void oneObserverTest() {
		ObserverMock observer = new ObserverMock();
		
		ObservableBaseMock observable = new ObservableBaseMock();
		observable.addObserver(observer);
		observable.publishEvent(createDummyEvent());
		
		assertTrue(observer.hasReceivedEvent);
	}	

	@Test
	public void twoObserversTest() {
		ObserverMock observer1 = new ObserverMock();
		ObserverMock observer2 = new ObserverMock();
		
		ObservableBaseMock observable = new ObservableBaseMock();
		observable.addObserver(observer1);
		observable.addObserver(observer2);
		observable.publishEvent(createDummyEvent());
		
		assertTrue(observer1.hasReceivedEvent);
		assertTrue(observer2.hasReceivedEvent);
	}	

	@Test
	public void removeObserversTest() {
		ObserverMock observer1 = new ObserverMock();
		ObserverMock observer2 = new ObserverMock();
		
		ObservableBaseMock observable = new ObservableBaseMock();
		observable.addObserver(observer1);
		observable.addObserver(observer2);
		observable.removeObserver(observer2);
		observable.publishEvent(createDummyEvent());
		
		assertTrue(observer1.hasReceivedEvent);
		assertFalse(observer2.hasReceivedEvent);
	}	

	@Test
	public void duplicateRemoveObserversTest() {
		ObserverMock observer1 = new ObserverMock();
		
		ObservableBaseMock observable = new ObservableBaseMock();
		observable.addObserver(observer1);
		observable.removeObserver(observer1);
		observable.removeObserver(observer1);
	}

	@Test
	public void noObserversTest() {
		ObservableBaseMock observable = new ObservableBaseMock();
		
		ObserverMock observer1 = new ObserverMock();
		observable.addObserver(observer1);
		observable.removeObserver(observer1);

		observable.publishEvent(createDummyEvent());
		assertFalse(observer1.hasReceivedEvent);
	}

	private UpdateEvent createDummyEvent() {
		MarketBasis marketBasis = new MarketBasis("Electicity", "EUR", 10, 0.0, 100.0);
		return new IncomingPriceUpdateEvent("agent1", "sessionId", new Date(), new Price(marketBasis, 0));		
	}
}
