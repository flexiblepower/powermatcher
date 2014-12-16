package net.powermatcher.examples;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;
import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.objectiveagent.BaseObjectiveAgent;

@Component(designateFactory = ObjectiveAgent.Config.class, immediate = true, provide = { ObservableAgent.class,
        ObjectiveEndpoint.class })
public class ObjectiveAgent extends BaseObjectiveAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveAgent.class);

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

    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);
        this.setAgentId(config.agentId());

        LOGGER.info("Objective agent activated");
    }

    @Deactivate
    public void deactivate() {
        LOGGER.info("Objective agent deactivated");
        this.setAgentId(null);
    }

    @Override
    public Bid handleAggregateBid(Bid aggregatedBid) {
        MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };

        ArrayBid objectiveBid = new ArrayBid(marketBasis,1, demand);

        ArrayBid aggregatedObjectiveBid = objectiveBid.aggregate(aggregatedBid);

        LOGGER.info("ObjectiveAgent: new aggregated bid: [{}] ", aggregatedObjectiveBid.getDemand());
        return aggregatedObjectiveBid;
    }

    @Override
    public void notifyPriceUpdate(PriceUpdate priceUpdate) {
        LOGGER.info("ObjectiveAgent: received price update [{}] ", priceUpdate);
    }
}
