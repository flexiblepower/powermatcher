package net.powermatcher.core.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class used to create an {@link Observer}.
 * The {@link Observer} searches for {@link Observable} services and adds itself.
 * 
 * {@link Observable} services are able to call the update method of 
 * {@link Observer} with {@link UpdateEvent} events.
 */
public abstract class BaseObserver implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(BaseObserver.class);

	/**
	 * Holds the available {@link Observable} services
	 */
	private ConcurrentMap<String, Observable> observables = new ConcurrentHashMap<String, Observable>();

	/**
	 * Holds the current {@link Observable} services being observed
	 */
	private ConcurrentMap<String, Observable> observing = new ConcurrentHashMap<String, Observable>();

	/**
	 * Filter containing all observableId's which must be observed.
	 */
	protected abstract List<String> filter();
	
	/**
	 * Add an {@link Observable} to the list of available {@link Observable} services
	 * @param observable {@link Observable} to add.
	 * @param properties configuration properties of {@link Observable} service
	 */
	public void addObservable(Observable observable, Map<String, Object> properties) {
		String observableId = observable.getObserverId();
		if (observables.putIfAbsent(observableId, observable) != null) {
			logger.warn("An observable with the id " + observableId + " was already registered");
		}
		
		updateObservables();
	}

	/**
	 * Removes an {@link Observable} from the list of available {@link Observable} services
	 * @param observable {@link Observable} to remove.
	 * @param properties configuration properties of {@link Observable} service
	 */
	public void removeObservable(Observable observable, Map<String, Object> properties) {
		String observableId = observable.getObserverId();

		// Check whether actually observing and remove
		if (observing.get(observableId) == observable) {
			observable.removeObserver(this);
		}
	}
	
	/**
	 * Update the connections to the {@link Observable} services.
	 * The filter is taken into account is present:
	 * <ul>
	 * 	<li>
	 * 		Filter is NULL or empty, all available {@link Observable} services will be observed.
	 * 	</li>
	 * 	<li>
	 * 		Filter is not empty, only {@link Observable} services from filter will be observed.
	 * 	</li>
	 * </ul>
	 */
	public void updateObservables() {
		for (String observableId : this.observables.keySet()) {
			// Check against filter whether observable should be observed
			if (this.filter() != null && this.filter().size() > 0 && 
					!this.filter().contains(observableId)) {
				// Remove observer when still observing
				if (this.observing.containsKey(observableId)) {
					Observable toRemove = this.observing.remove(observableId);
					toRemove.removeObserver(this);
					logger.info("Detached from observable [{}]", observableId);
				}
				
				continue;
			}

			this.addObservable(observableId);
		}
	}
	
	/**
	 * Start observing the specified {@link Observable} service.
	 * @param observableId id of {@link Observable}.
	 */
	private void addObservable(String observableId) {
		// Only attach to new observers
		if (!this.observing.containsKey(observableId)) {
			Observable observable = this.observables.get(observableId);
			observable.addObserver(this);
			observing.put(observableId, observable);
			logger.info("Attached to observable [{}]", observableId);
		}
	}
}
