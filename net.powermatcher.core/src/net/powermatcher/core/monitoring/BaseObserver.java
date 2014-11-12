package net.powermatcher.core.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.AgentEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class used to create an {@link AgentObserver}. The {@link AgentObserver} searches for {@link ObservableAgent} services and adds
 * itself.
 * 
 * {@link ObservableAgent} services are able to call the update method of {@link AgentObserver} with {@link AgentEvent} events.
 */
public abstract class BaseObserver implements AgentObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseObserver.class);

    /**
     * Holds the available {@link ObservableAgent} services
     */
    private ConcurrentMap<String, ObservableAgent> observables = new ConcurrentHashMap<String, ObservableAgent>();

    /**
     * Holds the current {@link ObservableAgent} services being observed
     */
    private ConcurrentMap<String, ObservableAgent> observing = new ConcurrentHashMap<String, ObservableAgent>();

    /**
     * Filter containing all observableId's which must be observed.
     */
    protected abstract List<String> filter();

    /**
     * Add an {@link ObservableAgent} to the list of available {@link ObservableAgent} services
     * 
     * @param observable
     *            {@link ObservableAgent} to add.
     * @param properties
     *            configuration properties of {@link ObservableAgent} service
     */
    public void addObservable(ObservableAgent observable, Map<String, Object> properties) {
        String observableId = observable.getObserverId();
        if (observables.putIfAbsent(observableId, observable) != null) {
            LOGGER.warn("An observable with the id " + observableId + " was already registered");
        }

        updateObservables();
    }

    /**
     * Removes an {@link ObservableAgent} from the list of available {@link ObservableAgent} services
     * 
     * @param observable
     *            {@link ObservableAgent} to remove.
     * @param properties
     *            configuration properties of {@link ObservableAgent} service
     */
    public void removeObservable(ObservableAgent observable, Map<String, Object> properties) {
        String observableId = observable.getObserverId();

        // Check whether actually observing and remove
        if (observing.get(observableId) == observable) {
            observable.removeObserver(this);
        }
    }

    /**
     * Update the connections to the {@link ObservableAgent} services. The filter is taken into account is present:
     * <ul>
     * <li>
     * Filter is NULL or empty, all available {@link ObservableAgent} services will be observed.</li>
     * <li>
     * Filter is not empty, only {@link ObservableAgent} services from filter will be observed.</li>
     * </ul>
     */
    public void updateObservables() {
        for (String observableId : this.observables.keySet()) {
            // Check against filter whether observable should be observed
            if (this.filter() != null && !this.filter().isEmpty() && !this.filter().contains(observableId)) {
                // Remove observer when still observing
                if (this.observing.containsKey(observableId)) {
                    ObservableAgent toRemove = this.observing.remove(observableId);
                    toRemove.removeObserver(this);
                    LOGGER.info("Detached from observable [{}]", observableId);
                }

                continue;
            }

            this.addObservable(observableId);
        }
    }

    /**
     * Start observing the specified {@link ObservableAgent} service.
     * 
     * @param observableId
     *            id of {@link ObservableAgent}.
     */
    private void addObservable(String observableId) {
        // Only attach to new observers
        if (!this.observing.containsKey(observableId)) {
            ObservableAgent observable = this.observables.get(observableId);
            observable.addObserver(this);
            observing.put(observableId, observable);
            LOGGER.info("Attached to observable [{}]", observableId);
        }
    }
}
