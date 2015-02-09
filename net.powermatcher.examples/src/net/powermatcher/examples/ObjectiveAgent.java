package net.powermatcher.examples;

import java.util.Map;

import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * {@link ObjectiveAgent} is an example implementation of {@link BaseObjectiveAgent}. It does nothing with the
 * {@link PriceUpdate}s it gets from {@link ObjectiveAgent#notifyPriceUpdate(PriceUpdate priceUpdate)}.
 * {@link ObjectiveAgent#handleAggregateBid(Bid)} sends a static {@link Bid} to manipulate the cluster.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = ObjectiveAgent.Config.class, immediate = true, provide = {
                                                                                        ObservableAgent.class,
                                                                                        ObjectiveEndpoint.class })
public class ObjectiveAgent
    extends BaseAgent
    implements ObjectiveEndpoint {

    private static final Logger LOGGER = LoggerFactory
                                                      .getLogger(ObjectiveAgent.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "objectiveagent")
        String agentId();
    }

    /**
     * OSGI configuration meta type with info about the objective agent.
     */
    private Config config;

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        setAgentId(config.agentId());

        LOGGER.info("Objective agent activated");
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        LOGGER.info("Objective agent deactivated");
        setAgentId(null);
    }

    /**
     * This specific implementation sends a static {@link Bid} to manipulate the cluster.
     *
     * {@inheritDoc}
     */
    @Override
    public Bid handleAggregateBid(Bid aggregatedBid) {
        MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY,
                                                  CURRENCY_EUR, 5, -1.0d, 7.0d);
        double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };

        ArrayBid objectiveBid = new ArrayBid(marketBasis, 1, demand);

        ArrayBid aggregatedObjectiveBid = objectiveBid.aggregate(aggregatedBid);

        LOGGER.info("ObjectiveAgent: new aggregated bid: [{}] ",
                    aggregatedObjectiveBid.getDemand());
        return aggregatedObjectiveBid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPrice(Price price) {
        LOGGER.info("ObjectiveAgent: received price update [{}] ", price);
    }
}
