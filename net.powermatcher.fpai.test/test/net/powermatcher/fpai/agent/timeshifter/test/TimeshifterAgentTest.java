package net.powermatcher.fpai.agent.timeshifter.test;

import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Power;

import junit.framework.Assert;
import junit.framework.TestCase;
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
import org.flexiblepower.rai.TimeShifterControlSpace;
import org.flexiblepower.rai.UncontrolledControlSpace;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.time.TimeUtil;

public class TimeshifterAgentTest extends TestCase {
    private static final String RESOURCE_ID = "appliance-id";
    private static final String CFG_PREFIX = "agent.agent1";
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity", "EUR", 100, 0, 50, 1, 0);
    private static final Measurable<Power> ZERO_POWER = Measure.valueOf(0, WATT);

    private static final double[] PROFILE_VALUES = { 100, 1000, 500, 1000, 400, 300, 300, 300, 300, 300 };
    private static final Measurable<Duration> PROFILE_ElEMENT_DURATION = Measure.valueOf(10, MINUTE);
    private static final EnergyProfile DEMAND_PROFILE = buildProfile(1, PROFILE_VALUES, PROFILE_ElEMENT_DURATION);
    private static final EnergyProfile SUPPLY_PROFILE = buildProfile(-1, PROFILE_VALUES, PROFILE_ElEMENT_DURATION);

    private static final Measurable<Duration> START_WINDOW = Measure.valueOf(1, HOUR);

    /** the parent matcher of the agent */
    private MockMatcherService parent;
    /** the agent under test */
    private TimeshifterAgent agent;
    /** the manager controlled by the agent */
    private MockResourceManager manager;

    private MockScheduledExecutor executor;
    private MockTimeService timeService;

    public void testMustRun() {
        EnergyProfile profile = DEMAND_PROFILE;

        // start a day after the Unix epoch
        long testStartTime = 24 * 60 * 60 * 1000;
        timeService.setAbsoluteTime(testStartTime);

        Date validFrom = new Date(testStartTime);
        Date before = TimeUtil.add(validFrom, START_WINDOW);
        Measurable<Duration> profileDuration = profile.getDuration();
        Date validThru = TimeUtil.add(before, profileDuration);

        TimeShifterControlSpaceBuilder builder = new TimeShifterControlSpaceBuilder();
        builder.setApplianceId(RESOURCE_ID);

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
        timeService.stepInTime(60000);
        executor.executePending();
        // manager.updateControlSpace(controlSpace);
        bid = parent.getLastBid(agent.getId());
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(PROFILE_VALUES[0], WATT));
        assertStarted(controlSpace, manager.getLastAllocation());

        // We don't check if the device was started in time, because starting at the startBefore time is actually to
        // late. This is acceptable (the resource manager should also act in this situation).

        // check that the agent correctly behaves during the execution of the profile
        assertCorrectMustRunBidding(profile, before, TimeUtil.add(before, profileDuration));
    }

    public void testDemand() throws InterruptedException {
        testNormal(DEMAND_PROFILE, true);
    }

    public void testSupply() throws InterruptedException {
        testNormal(SUPPLY_PROFILE, false);
    }

    private void testNormal(EnergyProfile profile, boolean isDemand) {
        // start a day after the Unix epoch
        long testStartTime = 24 * 60 * 60 * 1000;

        Date validFrom = new Date(testStartTime);
        Date after = validFrom;
        Date before = TimeUtil.add(validFrom, START_WINDOW);
        Measurable<Duration> profileDuration = profile.getDuration();
        Date validThru = TimeUtil.add(before, profileDuration);

        TimeShifterControlSpaceBuilder builder = new TimeShifterControlSpaceBuilder();
        builder.setApplianceId(RESOURCE_ID);

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
        timeService.stepInTime(Measure.valueOf(START_WINDOW.doubleValue(SECOND) / 2, SECOND));
        executor.executePending();
        bid = parent.getLastBid(agent.getId());
        assertStepBid(bid, profile);
        Assert.assertNull(manager.getLastAllocation());
        double stepPrice = BidAnalyzer.getStepPrice(bid);

        // startAfter < now < startBefore
        // progress time to three-quarters and assert it's a step, that there is no allocation yet and that maximum
        // accepted price is moving up or down (depending on whether it is supply or demand
        timeService.stepInTime(Measure.valueOf(START_WINDOW.doubleValue(SECOND) / 4, SECOND));
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
        Date endTime = TimeUtil.add(startTime, profileDuration);

        // we lower/raise the price by one extra, to give some room for price rounding in the PowerMatcher core
        price = isDemand ? price - 1 : price + 1;
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, price));

        executor.executePending();
        Allocation allocation = manager.getLastAllocation(0);
        assertStarted(controlSpace, allocation);
        assertStartedInTime(controlSpace, allocation);
        bid = parent.getLastBid(agent.getId());
        double initialDemand = isDemand ? PROFILE_VALUES[0] : -PROFILE_VALUES[0];
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(initialDemand, WATT));

        assertCorrectMustRunBidding(profile, startTime, endTime);
    }

    private void assertStepBid(BidInfo bid, EnergyProfile profile) {
        if (profile.getTotalEnergy().doubleValue(JOULE) > 0) {
            BidAnalyzer.assertStepBid(bid, Measure.valueOf(PROFILE_VALUES[0], WATT), ZERO_POWER, null);
        } else {
            BidAnalyzer.assertStepBid(bid, ZERO_POWER, Measure.valueOf(-PROFILE_VALUES[0], WATT), null);
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
        Assert.assertEquals(manager.getResourceId(), allocation.getResourceId());
        Assert.assertEquals(controlSpace.getId(), allocation.getControlSpaceId());

        // assert the time is the current time
        Assert.assertTrue(timeService.getDate().getTime() >= allocation.getStartTime().getTime());

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

    private Measurable<Power> getCurrentDemand(Date startTime, Date now, EnergyProfile profile) {
        Measurable<Duration> offset = TimeUtil.difference(startTime, now);
        return profile.getElementForOffset(offset).getAveragePower();
    }

    @Override
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

        manager = new MockResourceManager(RESOURCE_ID, UncontrolledControlSpace.class);
        agent.bind(manager);

        parent = new MockMatcherService();
        agent.bind(parent);

        agent.updateMarketBasis(MARKET_BASIS);
    }

    @Override
    public void tearDown() throws Exception {
        agent.unbind(executor);
        agent.unbind(timeService);
        agent.unbind(parent);

        executor.shutdown();
        executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
    }

    private static EnergyProfile
            buildProfile(int multiplier, double[] powerValues, Measurable<Duration> elementDuration) {
        EnergyProfile.Builder builder = EnergyProfile.create().setDuration(elementDuration);

        for (double profileValue : powerValues) {
            builder.add(Measure.valueOf(profileValue * multiplier * elementDuration.doubleValue(SECOND), JOULE));
        }

        return builder.build();
    }
}
