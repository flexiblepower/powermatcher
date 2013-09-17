package net.powermatcher.fpai.agent.uncontrolled.test;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import junit.framework.Assert;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.fpai.agent.uncontrolled.UncontrolledAgent;
import net.powermatcher.fpai.test.BidAnalyzer;
import net.powermatcher.fpai.test.MockMatcherService;
import net.powermatcher.fpai.test.MockResourceManager;
import net.powermatcher.fpai.test.MockScheduledExecutor;
import net.powermatcher.fpai.test.PowerMatcherToFPAITimeService;
import net.powermatcher.fpai.test.SystemTimeService;

import org.flexiblepower.rai.ResourceType;
import org.flexiblepower.rai.UncontrolledLGControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit test for {@link net.powermatcher.fpai.agent.uncontrolled.UncontrolledAgent}. */
public class UncontrolledAgentTest {
    private static final String APPLIANCE_ID = "appliance-id";
    private static final String CFG_PREFIX = "agent.agent1";
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity", "EUR", 101, 0, 50, 1, 0);

    private UncontrolledAgent agent;
    private ScheduledExecutorService executor;
    SystemTimeService timeService;
    private MockMatcherService parent;
    private MockResourceManager resourceManager;

    @Test
    public void testControlSpaceUpdated() {
        double[] durationValues = { 0.1, 0.5, 1.0, 2.0 };

        double[] measurementValueNumbers = { Double.MIN_VALUE,
                                            Long.MIN_VALUE,
                                            Integer.MIN_VALUE,
                                            -1000,
                                            -1,
                                            -0.1,
                                            0,
                                            0.1,
                                            1,
                                            1000,
                                            Integer.MAX_VALUE,
                                            Long.MAX_VALUE,
                                            Double.MAX_VALUE };

        EnergyUnit[] measurementUnits = { EnergyUnit.JOULE, EnergyUnit.KILO_WATTHOUR };

        // test the combination of the duration values, time units, measurement value numbers, and measurement units
        for (double durationValue : durationValues) {
            for (TimeUnit timeUnit : TimeUnit.values()) {
                // create a duration value from the variations
                Duration measurementDuration = new Duration(durationValue, timeUnit);

                // skip over tests where the duration of the element in the profile is < 1ms (rounds to 0)
                if (measurementDuration.getMilliseconds() == 0) {
                    continue;
                }

                for (double measurementValueNumber : measurementValueNumbers) {
                    for (EnergyUnit measurementUnit : measurementUnits) {
                        // create a measurement value from the variations
                        EnergyValue measurementValue = new EnergyValue(measurementValueNumber, measurementUnit);

                        // perform the actual test
                        testControlSpaceUpdated(measurementDuration, measurementValue);
                    }
                }
            }
        }
    }

    /**
     * method performs the actual test of the uncontrolled agent with an energy profile based on the given measurement
     * duration and measurement value
     */
    private void testControlSpaceUpdated(Duration measurementDuration, EnergyValue measurementValue) {
        // send the control space and get the bid
        BidInfo bid = sendControlSpace(measurementDuration, measurementValue);

        double expectedPower = measurementValue.getValueAs(EnergyUnit.JOULE) / durationToSeconds(measurementDuration);

        // if the expected power can't be computed with double precision, we don't expect a bid
        if (Double.isInfinite(expectedPower) || Double.isNaN(expectedPower)) {
            Assert.assertNull("Bid received for profile out of spec, may be correct not tested", bid);
        }

        // perform the normal test
        else {
            Assert.assertNotNull("No bid received for energy profile with duration " + measurementDuration
                                 + " with value"
                                 + measurementValue, bid);

            double[] demand = bid.getDemand();

            // check the number of elements in the demand array
            Assert.assertEquals("Bid does not span the market basis", MARKET_BASIS.getPriceSteps(), demand.length);

            // check the first power value to be equal to the expected power
            Assert.assertEquals("Unexpected power value in bid", expectedPower, demand[0], expectedPower / 10e6);

            // check that all following power values equal the first
            for (int i = 1; i < demand.length; i++) {
                Assert.assertEquals("Bid is not must - run (flat)", demand[i], demand[0], demand[0] / 10e6);
            }
        }
    }

    /**
     * sends a control space via the resource manager to the agent based on the given measurement duration and value and
     * returns the bid emitted based on that
     */
    private BidInfo sendControlSpace(Duration measurementDuration, EnergyValue measurementValue) {
        // the control space is valid from now and the profile ends now
        Date validFrom = new Date(System.currentTimeMillis());
        Date validThru = new Duration(10, TimeUnit.SECONDS).addTo(validFrom);
        Date startTime = new Date(System.currentTimeMillis() - measurementDuration.getMilliseconds());

        // construct the profile and control space
        EnergyProfile energyProfile = new EnergyProfile(measurementDuration, measurementValue);
        UncontrolledLGControlSpace controlSpace = new UncontrolledLGControlSpace(APPLIANCE_ID,
                                                                                 validFrom,
                                                                                 validThru,
                                                                                 null,
                                                                                 startTime,
                                                                                 energyProfile);

        // send the control space
        return sendControlSpace(controlSpace);
    }

    private BidInfo sendControlSpace(UncontrolledLGControlSpace controlSpace) {
        // let the resource manager send the new control space to the agent
        resourceManager.updateControlSpace(controlSpace);
        // then retreive the last bid from the agent
        return parent.getLastBid(agent.getId(), 10);
    }

    // TODO use time unit conversion
    private double durationToSeconds(Duration measurementDuration) {
        double milliInDestinationUnit = measurementDuration.getUnit().getMilliSeconds();
        double millisPerSecond = TimeUnit.SECONDS.getMilliSeconds();
        double factor = milliInDestinationUnit / millisPerSecond;

        return measurementDuration.getValue() * factor;
    }

    @Test
    public void testControlSpaceUpdatedGarbadgeIn() {
        Duration oneSecond = new Duration(1, TimeUnit.SECONDS);
        Date validFrom = new Date(System.currentTimeMillis());
        Date validThru = new Duration(10, TimeUnit.SECONDS).addTo(validFrom);
        Date startTime = new Date(System.currentTimeMillis() - oneSecond.getMilliseconds());

        // test with Double.MAX_VALUE kWh for a second which can't be expressed in watts
        EnergyValue maxkWh = new EnergyValue(Double.MAX_VALUE, EnergyUnit.KILO_WATTHOUR);
        EnergyProfile energyProfile = new EnergyProfile(oneSecond, maxkWh);
        UncontrolledLGControlSpace controlSpace = new UncontrolledLGControlSpace(APPLIANCE_ID,
                                                                                 validFrom,
                                                                                 validThru,
                                                                                 null,
                                                                                 startTime,
                                                                                 energyProfile);
        Assert.assertNull("Bid created for 'garbage input'", sendControlSpace(controlSpace));

        // test with null values
        // Assert.assertNull("Bid created for 'garbage input'", sendControlSpace(null));
        Assert.assertNull("Bid created for 'garbage input'",
                          sendControlSpace(new UncontrolledLGControlSpace(APPLIANCE_ID,
                                                                          validFrom,
                                                                          validThru,
                                                                          null,
                                                                          startTime,
                                                                          new EnergyProfile(null, EnergyUnit.JOULE))));

        // test with expired control space
        BidInfo bid = sendControlSpace(new UncontrolledLGControlSpace(APPLIANCE_ID,
                                                                      new Date(0),
                                                                      new Date(1),
                                                                      null,
                                                                      startTime,
                                                                      new EnergyProfile(oneSecond, EnergyUnit.JOULE, 1)));
        // Should be flat bid
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testUpdatePriceInfo() {
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, 1));
        Assert.assertNull("Allocation generated for uncontrollable resource", resourceManager.getLastAllocation(10));
    }

    @Before
    public void setUp() throws Exception {
        Properties cfg = new Properties();
        cfg.put(CFG_PREFIX + ".id", "agent1");
        cfg.put(CFG_PREFIX + ".matcher.id", "concentrator1");
        cfg.put(CFG_PREFIX + ".agent.bid.log.level", AgentConfiguration.FULL_LOGGING);
        cfg.put(CFG_PREFIX + ".agent.price.log.level", AgentConfiguration.FULL_LOGGING);

        agent = new UncontrolledAgent(new PrefixedConfiguration(cfg, CFG_PREFIX));

        timeService = new SystemTimeService();
        agent.bind(timeService);

        executor = new MockScheduledExecutor(new PowerMatcherToFPAITimeService(timeService));
        agent.bind(executor);

        resourceManager = new MockResourceManager(APPLIANCE_ID, ResourceType.UNCONTROLLED);
        agent.bind(resourceManager);

        parent = new MockMatcherService();
        agent.bind(parent);

        agent.updateMarketBasis(MARKET_BASIS);
    }

    @After
    public void tearDown() throws Exception {
        agent.unbind(executor);
        agent.unbind(timeService);
        agent.unbind(parent);

        executor.shutdown();
        executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
    }
}
