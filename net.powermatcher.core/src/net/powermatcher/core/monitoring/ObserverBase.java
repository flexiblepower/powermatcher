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

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Base class used to create an observer.
 * The observer searches for {@link Observable} services and adds itself.
 * 
 * {@link Observable} services are able to call the update method of 
 * {@link Observer} with {@link UpdateEvent} events.
 */
@Component(immediate = true, designateFactory = ObserverBase.Config.class)
public class ObserverBase implements Observer {

	/**
	 * OSGI configuration of the {@link ObserverBase}
	 */
	public static interface Config {
		@Meta.AD(required = false)
		List<String> filter();
	}

	private static final Logger logger = LoggerFactory.getLogger(ObserverBase.class);

	/**
	 * Holds the available observables
	 */
	private ConcurrentMap<String, Observable> observables = new ConcurrentHashMap<String, Observable>();

	/**
	 * Holds the current observables being observed
	 */
	private ConcurrentMap<String, Observable> observing = new ConcurrentHashMap<String, Observable>();

	/**
	 * Filter containing all id's which must be observed.
	 */
	private List<String> filter;
	
	/**
	 * Activate the component.
	 * @param properties updated configuration properties
	 */
	@Activate
	public synchronized void activate(Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class,
				properties);
		
		filter = config.filter();
		updateObservables();
	}
	
	/**
	 * Handle configuration modifications.
	 * @param properties updated configuration properties
	 */
	@Modified
	public synchronized void modified(Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class,
				properties);

		filter = config.filter();
		updateObservables();
	}

	@Reference(dynamic = true, multiple = true, optional = true)
	public void addObservable(Observable observable, Map<String, Object> properties) {
		String observableId = observable.getObserverId();
		if (observables.putIfAbsent(observableId, observable) != null) {
			logger.warn("An observable with the id " + observableId + " was already registered");
		}
		
		updateObservables();
	}

	public void removeObservable(Observable observable, Map<String, Object> properties) {
		String observableId = observable.getObserverId();

		// Check whether actually observing and remove
		if (observing.get(observableId) == observable) {
			observable.removeObserver(this);
		}
	}
	
	/**
	 * Update the connections to the observables.
	 * The filter is taken into account is present.
	 */
	private void updateObservables() {
		for (String observableId : this.observables.keySet()) {
			// Check against filter whether observable should be observed
			if (this.filter != null && this.filter.size() > 0 && 
					!this.filter.contains(observableId)) {
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
	 * Start observing the specified observable.
	 * @param observableId observable to observe.
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
	
	@Override
	public void update(UpdateEvent event) {
		logger.info("Received event: {}", event);
	}
}
