package net.powermatcher.fpai.agent.timeshifter.test;

import java.util.Date;
import java.util.Properties;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;
import net.powermatcher.fpai.agent.timeshifter.TimeshifterAgent;
import net.powermatcher.fpai.test.BidAnalyzer;
import net.powermatcher.fpai.test.MockMatcherService;
import net.powermatcher.fpai.test.MockResourceManager;
import net.powermatcher.fpai.test.MockScheduledExecutor;
import net.powermatcher.fpai.test.MockTimeService;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.ResourceType;
import org.flexiblepower.rai.TimeShifterControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.flexiblepower.rai.values.PowerValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimeshifterAgentTest {
    private static final String APPLIANCE_ID = "appliance-id";
    private static final String CFG_PREFIX = "agent.agent1";
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity", "EUR", 100, 0, 50, 1, 0);
    private static final PowerValue ZERO_POWER = new PowerValue(0, PowerUnit.WATT);

    private static final double[] PROFILE_VALUES = { 100, 1000, 500, 1000, 400, 300, 300, 300, 300, 300 };
    private static final Duration PROFILE_ElEMENT_DURATION = new Duration(10, TimeUnit.MINUTES);
    private static final EnergyProfile DEMAND_PROFILE = buildProfile(1, PROFILE_VALUES, PROFILE_ElEMENT_DURATION);
    private static final EnergyProfile SUPPLY_PROFILE = buildProfile(-1, PROFILE_VALUES, PROFILE_ElEMENT_DURATION);

    private static final Duration START_WINDOW = new Duration(1, TimeUnit.HOURS);

    /** the parent matcher of the agent */
    private MockMatcherService parent;
    /** the agent under test */
    private TimeshifterAgent agent;
    /** the manager controlled by the agent */
    private MockResourceManager manager;

    private MockScheduledExecutor executor;
    private MockTimeService timeService;

    @Test
    public void testMustRun() {
        EnergyProfile profile = DEMAND_PROFILE;

        // start a day after the Unix epoch
        long testStartTime = 24 * 60 * 60 * 1000;
        timeService.setAbsoluteTime(testStartTime);

        Date validFrom = new Date(testStartTime);
        Date before = add(START_WINDOW, validFrom);
        Duration profileDuration = getDuration(profile);
        Date validThru = add(profileDuration, before);

        TimeShifterControlSpaceBuilder builder = new TimeShifterControlSpaceBuilder();
        builder.setApplianceId(APPLIANCE_ID);

        builder.setValidFrom(validFrom);
        builder.setValidThru(validThru);
        builder.setExpirationTime(before);

        builder.setEnergyProfile(profile);
        builder.setStartAfter(validFrom);
        builder.setStartBefore(before);

        TimeShifterControlSpace controlSpace = builder.build();

        // set unattractive price
        // and update the control space
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MARKET_BASIS.getMaximumPrice()));
        manager.updateControlSpace(controlSpace);

        // now < startAfter
        // assert that the resource can not start yet (must-off bid and no allocation yet)
        executor.executePending();
        BidInfo bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, ZERO_POWER);
        Assert.assertNull(manager.getLastAllocation());

        // startBefore == now
        // assert that the resource has started (allocation and must-run bid with initial power exists)
        timeService.setAbsoluteTime(before.getTime());
        // FIXME this is now required to trigger allocation update, it shouldn't: https://tf.tno.nl/sf/go/artf46488
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MARKET_BASIS.getMaximumPrice()));
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(PROFILE_VALUES[0], PowerUnit.WATT));
        assertStarted(controlSpace, manager.getLastAllocation());

        // We don't check if the device was started in time, because starting at the startBefore time is actually to
        // late. This is acceptable (the resource manager should also act in this situation).

        // check that the agent correctly behaves during the execution of the profile
        assertCorrectMustRunBidding(profile, before, add(profileDuration, before));
    }

    @Test
    public void testDemand() throws InterruptedException {
        testNormal(DEMAND_PROFILE, true);
    }

    @Test
    public void testSupply() throws InterruptedException {
        testNormal(SUPPLY_PROFILE, false);
    }

    private void testNormal(EnergyProfile profile, boolean isDemand) {
        // start a day after the Unix epoch
        long testStartTime = 24 * 60 * 60 * 1000;

        Date validFrom = new Date(testStartTime);
        Date after = validFrom;
        Date before = add(START_WINDOW, validFrom);
        Duration profileDuration = getDuration(profile);
        Date validThru = add(profileDuration, before);

        TimeShifterControlSpaceBuilder builder = new TimeShifterControlSpaceBuilder();
        builder.setApplianceId(APPLIANCE_ID);

        builder.setValidFrom(validFrom);
        builder.setValidThru(validThru);
        builder.setExpirationTime(before);

        builder.setEnergyProfile(profile);
        builder.setStartAfter(after);
        builder.setStartBefore(before);

        TimeShifterControlSpace controlSpace = builder.build();

        // set unattractive price
        // and update the control space
        double unattractivePrice = isDemand ? MARKET_BASIS.getMaximumPrice() : MARKET_BASIS.getMinimumPrice();
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, unattractivePrice));
        manager.updateControlSpace(controlSpace);

        // now < startAfter
        // assert that the resource can not start yet (must-off bid and no allocation yet)
        executor.executePending();
        BidInfo bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, ZERO_POWER);
        Assert.assertNull(manager.getLastAllocation());

        // startAfter = now < startBefore
        // get the bid and assert it's initially still flat and that there is no allocation yet
        timeService.setAbsoluteTime(after.getTime());
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, ZERO_POWER);
        Assert.assertNull(manager.getLastAllocation());

        // startAfter < now < startBefore
        // progress time to half-way and assert it's a step and that there is no allocation yet
        timeService.stepInTime(new Duration(START_WINDOW.getValue() / 2, START_WINDOW.getUnit()));
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        assertStepBid(bid, profile);
        Assert.assertNull(manager.getLastAllocation());
        double stepPrice = BidAnalyzer.getStepPrice(bid);

        // startAfter < now < startBefore
        // progress time to three-quarters and assert it's a step, that there is no allocation yet and that maximum
        // accepted price is moving up or down (depending on whether it is supply or demand
        timeService.stepInTime(new Duration(START_WINDOW.getValue() / 4, START_WINDOW.getUnit()));
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        assertStepBid(bid, profile);
        Assert.assertNull(manager.getLastAllocation());
        if (isDemand) {
            Assert.assertTrue(stepPrice < BidAnalyzer.getStepPrice(bid));
        } else {
            Assert.assertTrue(stepPrice > BidAnalyzer.getStepPrice(bid));
        }

        // price to high, assert no allocation and still same step bid
        stepPrice = BidAnalyzer.getStepPrice(bid);
        double price = isDemand ? unattractivePrice : MARKET_BASIS.getMinimumPrice();
        for (; isDemand ? price > stepPrice : price < stepPrice; price -= isDemand ? 1 : -1) {
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, price));
            executor.executePending();

            bid = parent.getLastBid(agent.getId());
            assertStepBid(bid, profile);
            Assert.assertNull(manager.getLastAllocation());
        }

        // price low enough, assert started (allocation and must-run bid with initial power exists)
        Date startTime = timeService.getDate();
        Date endTime = add(profileDuration, startTime);

        // we lower/raise the price by one extra, to give some room for price rounding in the PowerMatcher core
        price = isDemand ? price - 1 : price + 1;
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, price));

        executor.executePending();
        Allocation allocation = manager.getLastAllocation(0);
        assertStarted(controlSpace, allocation);
        assertStartedInTime(controlSpace, allocation);
        bid = parent.getLastBid(agent.getId());
        double initialDemand = isDemand ? PROFILE_VALUES[0] : -PROFILE_VALUES[0];
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(initialDemand, PowerUnit.WATT));

        assertCorrectMustRunBidding(profile, startTime, endTime);
    }

    private void assertStepBid(BidInfo bid, EnergyProfile profile) {
        if (profile.getTotalEnergy(EnergyUnit.JOULE).getValue() > 0) {
            BidAnalyzer.assertStepBid(bid, new PowerValue(PROFILE_VALUES[0], PowerUnit.WATT), ZERO_POWER, null);
        } else {
            BidAnalyzer.assertStepBid(bid, ZERO_POWER, new PowerValue(-PROFILE_VALUES[0], PowerUnit.WATT), null);
        }
    }

    private void assertCorrectMustRunBidding(EnergyProfile profile, Date startTime, Date endTime) {
        BidInfo bid;
        // progress time and expose the agent to various prices
        // and assert no new allocations are sent and the must-run bid follows the profile
        while (timeService.getDate().before(endTime)) {
            for (double p = MARKET_BASIS.getMinimumPrice(); p <= MARKET_BASIS.getMaximumPrice(); p += 1.5) {
                agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, p));
                executor.executePending();

                Assert.assertNull(manager.getLastAllocation());
                bid = parent.getLastBid(agent.getId());
                BidAnalyzer.assertFlatBidWithValue(bid, getCurrentDemand(startTime, timeService.getDate(), profile));
            }

            timeService.stepInTime(1, TimeUnit.MINUTES);
        }

        // progress time even further and assert the bid ends with a must-off bid
        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, ZERO_POWER);
        Assert.assertNull(manager.getLastAllocation());
    }

    private void assertStarted(TimeShifterControlSpace controlSpace, Allocation allocation) {
        // assert an allocation exists
        Assert.assertNotNull(allocation);

        // assert the appliance id and control space are there
        Assert.assertEquals(manager.getApplianceId(), allocation.getApplianceId());
        Assert.assertEquals(controlSpace.getId(), allocation.getControlSpaceId());

        // assert the time is the current time
        Assert.assertEquals(timeService.getDate(), allocation.getStartTime());

        // assert the energy profile is exactly as in the control space
        Assert.assertEquals(controlSpace.getEnergyProfile(), allocation.getEnergyProfile());
    }

    // assert the start time is in the allowed window
    private void assertStartedInTime(TimeShifterControlSpace controlSpace, Allocation allocation) {
        Date startTime = allocation.getStartTime();
        Date startAfter = controlSpace.getStartAfter();
        Date startBefore = controlSpace.getStartBefore();

        Assert.assertTrue(startAfter.before(startTime) || startAfter.equals(startTime));
        Assert.assertTrue(startBefore.after(startTime) || startBefore.equals(startTime));
    }

    private PowerValue getCurrentDemand(Date startTime, Date now, EnergyProfile profile) {
        Duration offset = new Duration(now.getTime() - startTime.getTime(), TimeUnit.MILLISECONDS);

        // TODO implement in flexiblepower.api
        Element element = profile.getElementForOffset(offset);
        double joules = element.getEnergy().getValueAs(EnergyUnit.JOULE);
        double seconds = element.getDuration().getValueAs(TimeUnit.SECONDS);
        return new PowerValue(joules / seconds, PowerUnit.WATT);
    }

    // TODO implement in flexiblepower.api
    private Date add(Duration duration, Date date) {
        return new Date(duration.getMilliseconds() + date.getTime());
    }

    // TODO implement in flexiblepower.api
    private Duration getDuration(EnergyProfile profile) {
        double duration = 0;

        for (Element e : profile) {
            duration += e.getDuration().getValueAs(TimeUnit.MILLISECONDS);
        }

        return new Duration(duration, TimeUnit.MILLISECONDS);
    }

    @Before
    public void setUp() throws Exception {
        Properties cfg = new Properties();
        cfg.put(CFG_PREFIX + ".id", "agent1");
        cfg.put(CFG_PREFIX + ".matcher.id", "concentrator1");
        cfg.put(CFG_PREFIX + ".agent.bid.log.level", AgentConfiguration.FULL_LOGGING);
        cfg.put(CFG_PREFIX + ".agent.price.log.level", AgentConfiguration.FULL_LOGGING);
        cfg.put(CFG_PREFIX + "." + ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "1");

        agent = new TimeshifterAgent(new PrefixedConfiguration(cfg, CFG_PREFIX));

        timeService = new MockTimeService(new Date(0));
        agent.bind(timeService);

        executor = new MockScheduledExecutor(timeService.getFlexiblePowerTimeService());
        agent.bind(executor);

        manager = new MockResourceManager(APPLIANCE_ID, ResourceType.UNCONTROLLED);
        agent.bind(manager);

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

    private static EnergyProfile buildProfile(int multiplier, double[] powerValues, Duration elementDuration) {
        EnergyProfile.Builder builder = new EnergyProfile.Builder().setDuration(elementDuration);

        for (double profileValue : powerValues) {
            builder.add(profileValue * multiplier * elementDuration.getValueAs(TimeUnit.SECONDS), EnergyUnit.JOULE);
        }

        return builder.build();
    }
}
