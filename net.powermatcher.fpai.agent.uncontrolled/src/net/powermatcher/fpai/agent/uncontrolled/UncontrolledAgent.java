package net.powermatcher.fpai.agent.uncontrolled;

import java.util.Date;
import java.util.Map;

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
import org.flexiblepower.rai.ControlSpace;
import org.flexiblepower.rai.UncontrolledLGControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class UncontrolledAgent extends FPAIAgent implements
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
    protected Allocation createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, ControlSpace controlSpace) {
        // the device is uncontrollable, so no allocation can be created
        return null;
    }

    @Override
    protected BidInfo createBid(ControlSpace controlSpace, MarketBasis marketBasis) {
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

        UncontrolledLGControlSpace uncontrolledResourceControlSpace = (UncontrolledLGControlSpace) controlSpace;
        Element lastProfileElement = getLastProfileElementBefore(uncontrolledResourceControlSpace,
                                                                 new Date(getTimeSource().currentTimeMillis()));

        // if there is no such element, there isn't enough information to build a bid.
        if (lastProfileElement == null) {
            logger.error("Not creating bid, control space doesn't correctly report the current power level in its power profile {} starting at {}",
                         uncontrolledResourceControlSpace.getEnergyProfile(),
                         uncontrolledResourceControlSpace.getStartTime());
            return null;
        }

        // calculate the current power level and return a must run bid with that
        double energy = lastProfileElement.getEnergy().getValueAs(EnergyUnit.JOULE);
        double duration = lastProfileElement.getDuration().getValueAs(TimeUnit.SECONDS);

        double currentDemand = energy / duration;
        if (Double.isNaN(currentDemand) || Double.isInfinite(currentDemand)) {
            logger.error("Not creating bid, demand last profile element {} in control space is out of range to express as watts",
                         lastProfileElement);
            return null;
        }

        return new BidInfo(marketBasis, new PricePoint(0, currentDemand));
    }

    /**
     * @return Returns the last profile element in the energy profile of the given control space which starts before the
     *         given target time.
     */
    private Element getLastProfileElementBefore(UncontrolledLGControlSpace controlSpace, Date targetTime) {
        EnergyProfile energyProfile = controlSpace.getEnergyProfile();
        Date profileStartTime = controlSpace.getStartTime();

        // check input
        if (energyProfile == null || energyProfile.size() == 0 || profileStartTime == null) {
            return null;
        }

        // calculate the offset in the profile based on the target time and the start time
        long targetOffset = targetTime.getTime() - profileStartTime.getTime();

        long elementOffset = 0;
        Element currentProfileElement = null;

        // progress over the profile until an element has been reached starting after or on the target offset
        for (EnergyProfile.Element profileElement : energyProfile) {
            if (elementOffset < targetOffset) {
                currentProfileElement = profileElement;
            } else {
                break;
            }

            // check if duration provided for element
            if (profileElement.getDuration() == null) {
                return null;
            }

            elementOffset += profileElement.getDuration().getMilliseconds();
        }

        // return the the profile element found (possibly null)
        return currentProfileElement;
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
