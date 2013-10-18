package net.powermatcher.fpai.agent.storage.test;

import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.NonSI.KWH;
import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.measure.Measure;
import javax.measure.unit.NonSI;

import junit.framework.TestCase;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.fpai.agent.storage.StorageAgent;
import net.powermatcher.fpai.test.AllocationAnalyzer;
import net.powermatcher.fpai.test.BidAnalyzer;
import net.powermatcher.fpai.test.MockMatcherService;
import net.powermatcher.fpai.test.MockResourceManager;
import net.powermatcher.fpai.test.MockTimeService;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.values.ConstraintList;

public class StorageAgentTest extends TestCase {

    private static final String RESOURCE_ID = "appliance-id";
    private static final String CFG_PREFIX = "agent.agent1";
    private static final double MINIMUM_PRICE = 0;
    private static final double MAXIMUM_PRICE = 100;
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity",
                                                                    "EUR",
                                                                    100,
                                                                    MINIMUM_PRICE,
                                                                    MAXIMUM_PRICE,
                                                                    1,
                                                                    0);
    private StorageAgent agent;
    private ScheduledExecutorService executor;
    private MockTimeService timeService;
    private MockResourceManager resourceManager;
    private MockMatcherService parent;

    @Override
    public void setUp() {
        Properties cfg = new Properties();
        cfg.put(CFG_PREFIX + ".id", "agent1");
        cfg.put(CFG_PREFIX + ".matcher.id", "concentrator1");
        cfg.put(CFG_PREFIX + ".agent.bid.log.level", AgentConfiguration.FULL_LOGGING);
        cfg.put(CFG_PREFIX + ".agent.price.log.level", AgentConfiguration.FULL_LOGGING);

        agent = new StorageAgent(new PrefixedConfiguration(cfg, CFG_PREFIX));
        agent.updateMarketBasis(MARKET_BASIS);

        executor = Executors.newScheduledThreadPool(2);
        agent.bind(executor);

        timeService = new MockTimeService();
        agent.bind(timeService);

        resourceManager = new MockResourceManager(RESOURCE_ID, StorageControlSpace.class);
        agent.bind(resourceManager);

        parent = new MockMatcherService();
        agent.bind(parent);

        timeService.setAbsoluteTime(System.currentTimeMillis());
    }

    @Override
    public void tearDown() throws InterruptedException {
        agent.unbind(executor);
        agent.unbind(timeService);
        agent.unbind(parent);

        executor.shutdown();
        executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * Test if the agent does what it promises with a bid
     * 
     * We assume the price does not influence the bid
     */
    public void testBidResponse() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.stateOfCharge(0.5f);
        b.selfDischarge(Measure.valueOf(0, WATT));
        b.totalCapacity(Measure.valueOf(0, KWH));
        b.chargeSpeed(ConstraintList.create(WATT).addSingle(1000).addSingle(2000).build());
        // Iterate over all the price steps
        for (int i = 0; i < 100; i++) {
            double price = ((MAXIMUM_PRICE - MINIMUM_PRICE) / 100f) * i + MINIMUM_PRICE;
            b.validFrom(timeService.getDate());
            b.validThru(new Date(timeService.currentTimeMillis() + 60000));
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, price));
            BidInfo bid = parent.getLastBid(agent.getId(), 1000);
            Allocation allocation = resourceManager.getLastAllocation(1000);
            AllocationAnalyzer.assertAllocationWithDemand(allocation,
                                                          timeService,
                                                          Measure.valueOf(bid.getDemand(price), WATT));
            timeService.stepInTime(1000);
        }
    }

    public void testBidValues() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0.5f);
        int[] wattValues = { 735, 1000000, 0 };
        for (int watts : wattValues) {
            timeService.stepInTime(1000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            b.chargeSpeed(ConstraintList.create(WATT).addSingle(watts).build());
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            assertEquals(watts, parent.getLastBid(agent.getId(), 1000).getDemand()[0], 0.001);
        }
    }

    // public void testValidFrom() {
    // TODO This test is temporarily disabled since this functionality has not yet been implemented
    public void ignoreValidFrom() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0f);
        b.validFrom(timeService.getDate());
        b.validThru(new Date(timeService.currentTimeMillis() + 10000)); // Valid
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        BidInfo bid1 = parent.getLastBid(agent.getId(), 1000);

        timeService.stepInTime(1000);

        b.stateOfCharge(1f); // changed
        b.validFrom(new Date(timeService.currentTimeMillis() + 10000));
        b.validThru(new Date(timeService.currentTimeMillis() + 10001)); // INVALID
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        BidInfo bid2 = parent.getLastBid(agent.getId(), 1000);

        // bid2 should either be null (because it has not yet been processed) or equal to bid1
        if (bid2 != null) {
            BidAnalyzer.assertBidsEqual(bid1, bid2);
        }
    }

    public void testValidThruExpired() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0f);
        b.validFrom(timeService.getDate());
        b.validThru(new Date(timeService.currentTimeMillis() + 10000)); // Valid
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        parent.getLastBid(agent.getId(), 1000);

        timeService.stepInTime(1000);

        b.stateOfCharge(1f); // changed
        b.validFrom(new Date(timeService.currentTimeMillis() - 10001));
        b.validThru(new Date(timeService.currentTimeMillis() - 10000)); // INVALID
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        BidInfo bid2 = parent.getLastBid(agent.getId(), 1000);

        // Since there is no valid control space at the moment, bid2 should be flat
        if (bid2 != null) {
            BidAnalyzer.assertFlatBidWithValue(bid2, Measure.valueOf(0, WATT));
        }
    }

    public void testMinOnCharge() {
        // init
        timeService.stepInTime(60000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0).minOnPeriod(Measure.valueOf(1, MINUTE));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

        // Make the agent charge
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE)); // Triggers allocation
        Allocation allocation = resourceManager.getLastAllocation(1000);
        AllocationAnalyzer.assertRunningAllocation(allocation, timeService);

        // Now we let it run for two minutes. The SOC = 1, the minOnPeriod one minute, so the agent must tell the device
        // to shut down after one minute.
        for (int seconds = 0; seconds < 120; seconds++) {
            timeService.stepInTime(1000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

            // Check bid
            b.stateOfCharge(1);
            resourceManager.updateControlSpace(b.build(RESOURCE_ID)); // Triggers bid

            // Check allocation
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE)); // Triggers allocation
            allocation = resourceManager.getLastAllocation(1000);
            BidInfo lastBid = parent.getLastBid(agent.getId(), 1000);
            System.out.println(lastBid);
            System.out.println(allocation.getEnergyProfile().get(0).getAveragePower());
            System.out.println("Second " + seconds);
            if (seconds < 60) {
                // Must charge situation
                AllocationAnalyzer.assertRunningAllocation(allocation, timeService);
                BidAnalyzer.assertDemandAtLeast(lastBid, Measure.valueOf(1000, WATT));
            } else {
                // MinTurnOn period is over, should turn off now
                AllocationAnalyzer.assertDemandAtMost(allocation, timeService, Measure.valueOf(0, WATT));
                BidAnalyzer.assertDemandAtMost(lastBid, Measure.valueOf(1000, WATT));
            }
        }
    }

    public void testMinOnDischarge() {
        // init
        timeService.stepInTime(3600000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(1.0f).minOnPeriod(Measure.valueOf(1, MINUTE)).minOffPeriod(Measure.valueOf(0, SECOND));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

        // Make the agent discharge
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE)); // Triggers allocation
        Allocation allocation = resourceManager.getLastAllocation(1000);
        AllocationAnalyzer.assertDemandAtMost(allocation, timeService, Measure.valueOf(-1, WATT));

        // Now we let it run for two minutes. The SOC = 1, the minOnPeriod one minute, so the agent must tell the device
        // to stop discharging
        for (int seconds = 0; seconds < 120; seconds++) {
            timeService.stepInTime(1000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

            // Check bid
            b.stateOfCharge(0);
            resourceManager.updateControlSpace(b.build(RESOURCE_ID)); // Triggers bid

            // Check allocation
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE)); // Triggers allocation
            allocation = resourceManager.getLastAllocation(1000);
            BidInfo lastBid = parent.getLastBid(agent.getId(), 1000);
            System.out.println(lastBid);
            System.out.println(allocation.getEnergyProfile());
            System.out.println("Second " + seconds);
            if (seconds < 60) {
                // Must discharge situation
                AllocationAnalyzer.assertRunningAllocation(allocation, timeService);
                BidAnalyzer.assertDemandAtMost(lastBid, Measure.valueOf(-1, WATT));
            } else {
                // MinTurnOn period is over, should turn off or charge now
                AllocationAnalyzer.assertDemandAtLeast(allocation, timeService, Measure.valueOf(0, WATT));
                BidAnalyzer.assertDemandAtLeast(lastBid, Measure.valueOf(0, WATT));
            }
        }
    }

    public void testMinOff() {
        // make device turn on, make sure minturnon and minturnoff periods are over
        timeService.stepInTime(120000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        timeService.stepInTime(60000);
        b.stateOfCharge(0.5f).minOffPeriod(Measure.valueOf(1, MINUTE));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
        timeService.stepInTime(60000);
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.stateOfCharge(0.51f).minOffPeriod(Measure.valueOf(1, MINUTE));
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);

        // Find price for turning off
        BidInfo bid = parent.getLastBid(agent.getId(), 1000);
        float turnoffPrice = 0;
        double[] demandArray = bid.getDemand();
        for (int i = 0; i < demandArray.length; i++) {
            if (demandArray[i] == 0) {
                turnoffPrice = MARKET_BASIS.toNormalizedPrice(i);
                break;
            }
        }

        // Make the agent Not charge
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, turnoffPrice)); // Triggers allocation
        AllocationAnalyzer.assertDemandAtMost(resourceManager.getLastAllocation(1000),
                                              timeService,
                                              Measure.valueOf(0, WATT));

        // Now we let it run for two minutes. The SOC = 0.5, the minOffPeriod one minute, so the agent must tell the
        // device to start running again after one minute.
        for (int seconds = 0; seconds < 120; seconds++) {
            timeService.stepInTime(1000);
            // Check bid
            b.stateOfCharge(0);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(RESOURCE_ID)); // Triggers bid

            // Check allocation
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE)); // Triggers allocation
            if (seconds < 60) {
                // In minturnoff period
                AllocationAnalyzer.assertNotRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
                BidAnalyzer.assertFlatBidWithValue(parent.getLastBid(agent.getId(), 1000), Measure.valueOf(0, WATT));
            } else {
                // minturnoff period over, should start charging
                AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
                BidAnalyzer.assertDemandBid(parent.getLastBid(agent.getId(), 1000));
            }
        }
    }

    /**
     * Test if the agent prevents over charging with respect to the minimum on period
     * 
     * In this test we have a buffer with a capacity of 2KWH, a charge speed of 2000W, a self discharge speed of 1000W,
     * which indicates that the buffer is full in exactly 1 hour. The minOnPeriod is 30 minutes. This means that if the
     * SOC is >= 0.5, the agent should NOT turn on the device.
     */
    public void testOverchargePrevention() {
        this.testOverchargePrevention(1, 1000, 0, 0.5f);
        this.testOverchargePrevention(1, 2000, 1000, 0.5f);
    }

    /**
     * Test if the agent will not come in a minOnPeriod in which the SOC will exceed 1
     * 
     * @param capacityKWH
     * @param chargeSpeedW
     * @param dischargeSpeedW
     * @param minOnHours
     */
    public void
            testOverchargePrevention(float capacityKWH, float chargeSpeedW, float dischargeSpeedW, float minOnHours) {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 60000));
        b.stateOfCharge(1f);
        b.chargeSpeed(ConstraintList.create(WATT).addSingle(chargeSpeedW).build());
        b.selfDischarge(Measure.valueOf(dischargeSpeedW, WATT));
        b.totalCapacity(Measure.valueOf(capacityKWH, KWH));
        b.minOnPeriod(Measure.valueOf(minOnHours, HOUR));
        b.minOffPeriod(Measure.valueOf(0, SECOND));
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));

        // if(SOC >= criticalSOC) the device should NOT turn on
        float criticalSOC = 1 - (((chargeSpeedW - dischargeSpeedW) * minOnHours) / (capacityKWH * 1000));
        // Make the device not turn on and let it discharge until SOC < criticalSOC
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        float curSOC = 1;
        float deltaSOCperHour = dischargeSpeedW / (capacityKWH * 1000);
        boolean charging = false;
        for (int minute = 0; minute < 60; minute++) {
            timeService.stepInTime(60000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 60000));
            curSOC -= deltaSOCperHour / 60f;
            b.stateOfCharge(curSOC);
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            Allocation a = resourceManager.getLastAllocation(1000);
            System.out.println("charging: " + charging + " curSOC: " + curSOC + " CriticalSOC: " + criticalSOC);
            if (!charging && curSOC >= criticalSOC) {
                // Should NOT turn on
                AllocationAnalyzer.assertDemandAtMost(a, timeService, Measure.valueOf(0, WATT));
                BidAnalyzer.assertDemandAtMost(parent.getLastBid(agent.getId(), 1000), Measure.valueOf(0, WATT));
            } else {
                // Should charge, SOC is below critical SOC
                charging = true;
                AllocationAnalyzer.assertRunningAllocation(a, timeService);
                curSOC += (chargeSpeedW / (capacityKWH * 1000)) / 60f;
                BidAnalyzer.assertDemandBid(parent.getLastBid(agent.getId(), 1000));
            }
        }

    }

    public void testDrainPrevention() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0.1f);
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.chargeSpeed(ConstraintList.create(WATT).addSingle(2000).build());
        b.selfDischarge(Measure.valueOf(1000, WATT));
        b.totalCapacity(Measure.valueOf(1, KWH));
        b.minOnPeriod(Measure.valueOf(0, SECOND));
        b.minOffPeriod(Measure.valueOf(30, MINUTE));

        double criticalEneregyWH = 1000 * 0.5;
        double criticalSOC = criticalEneregyWH / 1000;
        // Make it discharge
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        Allocation a = resourceManager.getLastAllocation(1000);
        AllocationAnalyzer.assertRunningAllocation(a, timeService);

        for (float curSOC = 0; curSOC <= 1; curSOC += 0.05) {
            timeService.stepInTime(60000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            b.stateOfCharge(curSOC);
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            a = resourceManager.getLastAllocation(1000);
            if (curSOC < criticalSOC) {
                AllocationAnalyzer.assertRunningAllocation(a, timeService);
            } else {
                AllocationAnalyzer.assertDemandAtMost(a, timeService, Measure.valueOf(0, WATT));
            }
        }
    }

    public void testTargetStateOfChargeHigher() {
        timeService.stepInTime(Measure.valueOf(1, NonSI.DAY));
        double targetSOC = 1;
        long targetTime = timeService.currentTimeMillis() + 3600000;
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.target(targetSOC, new Date(targetTime));
        b.stateOfCharge(0f);
        b.selfDischarge(Measure.valueOf(0, WATT));
        b.totalCapacity(Measure.valueOf(1, KWH));
        b.chargeSpeed(ConstraintList.create(WATT).addSingle(100).addSingle(1000).build());
        b.dischargeSpeed(ConstraintList.create(WATT).addSingle(100).addSingle(1000).build());
        for (int minute = 0; minute < 120; minute++) {
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            BidInfo bid = parent.getLastBid(agent.getId(), 1000);
            BidAnalyzer.assertDemandAtMost(bid, Measure.valueOf(1000, WATT));
            System.out.println(bid);
            AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
            timeService.stepInTime(60000);
        }
    }

    public void testTargetStateOfChargeLower() {
        double targetSOC = 0;
        long targetTime = timeService.currentTimeMillis() + 3600000;
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.target(targetSOC, new Date(targetTime));
        b.stateOfCharge(1);
        b.selfDischarge(Measure.valueOf(0, WATT));
        b.totalCapacity(Measure.valueOf(1, KWH));
        b.chargeSpeed(ConstraintList.create(WATT).addSingle(100).addSingle(1000).build());
        b.dischargeSpeed(ConstraintList.create(WATT).addSingle(100).addSingle(1000).build());
        for (int minute = 0; minute < 120; minute++) {
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(RESOURCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            BidAnalyzer.assertDemandAtLeast(parent.getLastBid(agent.getId(), 1000), Measure.valueOf(-1000, WATT));
            AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
            timeService.stepInTime(60000);
        }
    }

    public void testStorageFull() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(1);
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        BidAnalyzer.assertDemandAtMost(parent.getLastBid(agent.getId(), 1000), Measure.valueOf(0, WATT));
        AllocationAnalyzer.assertDemandAtMost(resourceManager.getLastAllocation(1000),
                                              timeService,
                                              Measure.valueOf(0, WATT));
    }

    public void testStorageEmpty() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0);
        resourceManager.updateControlSpace(b.build(RESOURCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        BidAnalyzer.assertDemandAtLeast(parent.getLastBid(agent.getId(), 1000), Measure.valueOf(0, WATT));
        AllocationAnalyzer.assertDemandAtLeast(resourceManager.getLastAllocation(1000),
                                               timeService,
                                               Measure.valueOf(0, WATT));
    }

}
