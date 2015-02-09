package net.powermatcher.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.core.monitoring.BaseObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link SimpleObserver} is an example implementation of the {@link BaseObserver} interface. You can add
 * {@link ObservableAgent}s and it can receive {@link AgentEvent}s from them.
 *
 * @author FAN
 * @version 2.0
 */
@Component(immediate = true, designateFactory = SimpleObserver.Config.class)
public class SimpleObserver
    extends BaseObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleObserver.class);

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
     *
     * @param properties
     *            updated configuration properties
     */
    @Activate
    public synchronized void activate(Map<String, Object> properties) {
        processConfig(properties);
    }

    /**
     * OSGi calls this method to modify a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Modified
    public synchronized void modified(Map<String, Object> properties) {
        processConfig(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Reference(dynamic = true, multiple = true, optional = true)
    public void addObservable(ObservableAgent observable, Map<String, Object> properties) {
        super.addObservable(observable, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getFilter() {
        return filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(AgentEvent event) {
        LOGGER.info("Received event: {}", event);
    }

    /**
     * This method processes the data in the Config interfaces of this class.
     *
     * @param properties
     *            the configuration properties
     */
    private void processConfig(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        filter = config.filter();

        // ConfigAdmin will sometimes generate a filter with 1 empty element.
        // Ignore it.
        if (filter != null && !filter.isEmpty() && filter.get(0).isEmpty()) {
            filter = new ArrayList<String>();
        }

        updateObservables();
    }
}
