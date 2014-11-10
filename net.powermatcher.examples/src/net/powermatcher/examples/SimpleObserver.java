package net.powermatcher.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.UpdateEvent;
import net.powermatcher.core.monitoring.ObserverBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Example Observer which simply writes log entries of received events.
 */
@Component(immediate = true, designateFactory = SimpleObserver.Config.class)
public class SimpleObserver extends ObserverBase {

	private static final Logger logger = LoggerFactory.getLogger(SimpleObserver.class);

	private List<String> filter;
	
	/**
	 * OSGI configuration of the {@link SimpleObserver}
	 */
	public static interface Config {
		@Meta.AD(required = false)
		List<String> filter();
	}

	/**
	 * Activate the component.
	 * @param properties updated configuration properties
	 */
	@Activate
	public synchronized void activate(Map<String, Object> properties) {
		processConfig(properties);
	}
	
	/**
	 * Handle configuration modifications.
	 * @param properties updated configuration properties
	 */
	@Modified
	public synchronized void modified(Map<String, Object> properties) {
		processConfig(properties);
	}

	@Override
	@Reference(dynamic = true, multiple = true, optional = true)
	public void addObservable(Observable observable, Map<String, Object> properties) {
		super.addObservable(observable, properties);
	}	

	@Override
	public void removeObservable(Observable observable, Map<String, Object> properties) {
		super.removeObservable(observable, properties);
	}

	@Override
	protected List<String> filter() {
		return this.filter;
	}
	
	@Override
	public void update(UpdateEvent event) {
		logger.info("Received event: {}", event);
	}
	
	private void processConfig(Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class, properties);
		this.filter = config.filter();

		// ConfigAdmin will sometimes generate a filter with 1 empty element. Ignore it. 
		if (filter != null && filter.size() > 0 && filter.get(0).length() == 0) {
			this.filter = new ArrayList<String>(); 
		}

		updateObservables();
	}
}
