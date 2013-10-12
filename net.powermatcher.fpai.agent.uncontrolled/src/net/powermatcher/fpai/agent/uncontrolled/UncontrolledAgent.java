package net.powermatcher.fpai.agent.uncontrolled;

import java.util.Date;
import java.util.Map;

import javax.measure.Measurable;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.fpai.agent.uncontrolled.UncontrolledAgent.Config;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.UncontrolledControlSpace;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.flexiblepower.time.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class UncontrolledAgent extends FPAIAgent<UncontrolledControlSpace> implements
                                                                          AgentConnectorService,
                                                                          LoggingConnectorService,
                                                                          TimeConnectorService,
                                                                          SchedulerConnectorService {

    private final static Logger logger = LoggerFactory.getLogger(UncontrolledAgent.class);

    static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.05;

    private double significanceLevel;

    public UncontrolledAgent() {
        super();
        significanceLevel = DEFAULT_SIGNIFICANCE_LEVEL;
    }

    public UncontrolledAgent(ConfigurationService configuration) {
        super(configuration);
        init(configuration.getProperties());
    }

    @Activate
    public void init(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        significanceLevel = config.significance_level();
    }

    @Override
    protected Allocation
            createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, UncontrolledControlSpace controlSpace) {
        // the device is uncontrollable, so no allocation can be created
        return null;
    }

    @Override
    protected BidInfo createBid(UncontrolledControlSpace controlSpace, MarketBasis marketBasis) {
        // check if control space is provided
        if (controlSpace == null) {
            logger.error("Not creating bid, control space is not valid: {}", controlSpace);
            return null;
        }

        // check if control space is still valid
        if (controlSpace.getValidThru() != null && controlSpace.getValidThru().before(new Date(getCurrentTimeMillis()))) {
            logger.error("Not creating bid, control space is no longer valid: {}", controlSpace);
            return null;
        }

        if (marketBasis == null) {
            logger.error("Not creating bid, market basis is not valid: {}", marketBasis);
            return null;
        }

        // Element lastProfileElement = getLastProfileElementBefore(controlSpace,
        // new Date(getTimeSource().currentTimeMillis()));

        Element lastProfileElement = controlSpace.getEnergyProfile()
                                                 .getElementForOffset(TimeUtil.difference(new Date(getTimeSource().currentTimeMillis()),
                                                                                          controlSpace.getStartTime()));

        // if there is no such element, there isn't enough information to build a bid.
        if (lastProfileElement == null) {
            logger.error("Not creating bid, control space doesn't correctly report the current power level in its power profile {} starting at {}",
                         controlSpace.getEnergyProfile(),
                         controlSpace.getStartTime());
            return null;
        }

        // calculate the current power level and return a must run bid with that

        Measurable<Power> currentDemand = null;
        try {
            currentDemand = lastProfileElement.getAveragePower();
        } catch (Exception e) {
            logger.error("Not creating bid, demand last profile element {} in control space is out of range to express as watts",
                         lastProfileElement);
            return null;
        }

        return new BidInfo(marketBasis, new PricePoint(0, currentDemand.doubleValue(SI.WATT)));
    }

    /**
     * @return Returns whether the current demand changed significantly given the significanceLevel. Any change from or
     *         to 0 is considered significant.
     */
    protected boolean isStateChangeSignificant(double previousDemand, double currentDemand) {
        // any change from or to the zero value is considered to be significant
        if (previousDemand == 0 || currentDemand == 0) {
            return true;
        }

        return Math.abs((previousDemand - currentDemand) / previousDemand) >= significanceLevel;
    }

    public static interface Config extends AgentConfiguration {
        @Override
        @Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT)
        public String cluster_id();

        @Override
        public String id();

        @Override
        @Meta.AD(required = false, deflt = IdentifiableObjectConfiguration.ENABLED_DEFAULT_STR)
        public boolean enabled();

        @Override
        @Meta.AD(required = false, deflt = UPDATE_INTERVAL_DEFAULT_STR)
        public int update_interval();

        @Override
        @Meta.AD(required = false,
                 deflt = AGENT_BID_LOG_LEVEL_DEFAULT,
                 optionValues = { NO_LOGGING, PARTIAL_LOGGING, FULL_LOGGING },
                 optionLabels = { NO_LOGGING_LABEL, PARTIAL_LOGGING_LABEL, FULL_LOGGING_LABEL })
        public String agent_bid_log_level();

        @Override
        @Meta.AD(required = false,
                 deflt = AGENT_PRICE_LOG_LEVEL_DEFAULT,
                 optionValues = { NO_LOGGING, FULL_LOGGING },
                 optionLabels = { NO_LOGGING_LABEL, FULL_LOGGING_LABEL })
        public String agent_price_log_level();

        @Meta.AD(required = false, deflt = UncontrolledAgent.DEFAULT_SIGNIFICANCE_LEVEL + "")
        public double significance_level();
    }
}
