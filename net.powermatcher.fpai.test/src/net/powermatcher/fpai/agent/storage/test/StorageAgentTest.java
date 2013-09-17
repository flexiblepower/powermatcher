package net.powermatcher.fpai.agent.storage.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
import org.flexiblepower.rai.ResourceType;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StorageAgentTest {

    private static final String APPLIANCE_ID = "appliance-id";
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

    @Before
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

        resourceManager = new MockResourceManager(APPLIANCE_ID, ResourceType.STORAGE);
        agent.bind(resourceManager);

        parent = new MockMatcherService();
        agent.bind(parent);

        timeService.setAbsoluteTime(System.currentTimeMillis());
    }

    @After
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
    @Test
    public void testBidResponse() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.stateOfCharge(0.5f);
        b.selfDischarge(new PowerValue(0, PowerUnit.WATT));
        b.totalCapacity(new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));
        b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(1000, PowerUnit.WATT),
                                                                  new PowerValue(2000, PowerUnit.WATT))));
        // Iterate over all the price steps
        for (int i = 0; i < 100; i++) {
            double price = ((MAXIMUM_PRICE - MINIMUM_PRICE) / 100f) * i + MINIMUM_PRICE;
            b.validFrom(timeService.getDate());
            b.validThru(new Date(timeService.currentTimeMillis() + 60000));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, price));
            BidInfo bid = parent.getLastBid(agent.getId(), 1000);
            Allocation allocation = resourceManager.getLastAllocation(1000);
            AllocationAnalyzer.assertAllocationWithDemand(allocation, timeService, new PowerValue(bid.getDemand(price),
                                                                                                  PowerUnit.WATT));
            timeService.stepInTime(1000);
        }
    }

    @Test
    public void testBidValues() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0.5f);
        int[] wattValues = { 735, 1000000, 0 };
        for (int watts : wattValues) {
            timeService.stepInTime(1000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(watts, PowerUnit.WATT))));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            assertEquals(watts, parent.getLastBid(agent.getId(), 1000).getDemand()[0], 0.001);
        }
    }

    @Test
    public void testValidFrom() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0f);
        b.validFrom(timeService.getDate());
        b.validThru(new Date(timeService.currentTimeMillis() + 10000)); // Valid
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        BidInfo bid1 = parent.getLastBid(agent.getId(), 1000);

        timeService.stepInTime(1000);

        b.stateOfCharge(1f); // changed
        b.validFrom(new Date(timeService.currentTimeMillis() + 10000));
        b.validThru(new Date(timeService.currentTimeMillis() + 10001)); // INVALID
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        BidInfo bid2 = parent.getLastBid(agent.getId(), 1000);

        // bid2 should either be null (because it has not yet been processed) or equal to bid1
        if (bid2 != null) {
            BidAnalyzer.assertBidsEqual(bid1, bid2);
        }
    }

    @Test
    public void testValidThruExpired() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0f);
        b.validFrom(timeService.getDate());
        b.validThru(new Date(timeService.currentTimeMillis() + 10000)); // Valid
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        parent.getLastBid(agent.getId(), 1000);

        timeService.stepInTime(1000);

        b.stateOfCharge(1f); // changed
        b.validFrom(new Date(timeService.currentTimeMillis() - 10001));
        b.validThru(new Date(timeService.currentTimeMillis() - 10000)); // INVALID
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        BidInfo bid2 = parent.getLastBid(agent.getId(), 1000);

        // Since there is no valid control space at the moment, bid2 should be flat
        if (bid2 != null) {
            BidAnalyzer.assertFlatBidWithValue(bid2, new PowerValue(0, PowerUnit.WATT));
        }
    }

    @Test
    public void testMinOnCharge() {
        // init
        timeService.stepInTime(60000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0).minOnPeriod(new Duration(1, TimeUnit.MINUTES));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

        // Make the agent charge
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
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
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID)); // Triggers bid

            // Check allocation
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE)); // Triggers allocation
            allocation = resourceManager.getLastAllocation(1000);
            BidInfo lastBid = parent.getLastBid(agent.getId(), 1000);
            System.out.println(lastBid);
            System.out.println(allocation.getEnergyProfile());
            System.out.println("Second " + seconds);
            if (seconds < 60) {
                // Must run situation
                AllocationAnalyzer.assertRunningAllocation(allocation, timeService);
                BidAnalyzer.assertDemandAtLeast(lastBid, new PowerValue(1000, PowerUnit.WATT));
            } else {
                // MinTurnOn period is over, should turn off now
                AllocationAnalyzer.assertDemandAtMost(allocation, timeService, new PowerValue(0, PowerUnit.WATT));
                BidAnalyzer.assertDemandAtMost(lastBid, new PowerValue(0, PowerUnit.WATT));
            }
        }
    }

    @Test
    public void testMinOnDischarge() {
        // init
        timeService.stepInTime(3600000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(1.0f)
         .minOnPeriod(new Duration(1, TimeUnit.MINUTES))
         .minOffPeriod(new Duration(0, TimeUnit.SECONDS));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

        // Make the agent discharge
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE)); // Triggers allocation
        Allocation allocation = resourceManager.getLastAllocation(1000);
        AllocationAnalyzer.assertDemandAtMost(allocation, timeService, new PowerValue(-1, PowerUnit.WATT));

        // Now we let it run for two minutes. The SOC = 1, the minOnPeriod one minute, so the agent must tell the device
        // to stop discharging
        for (int seconds = 0; seconds < 120; seconds++) {
            timeService.stepInTime(1000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));

            // Check bid
            b.stateOfCharge(0);
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID)); // Triggers bid

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
                BidAnalyzer.assertDemandAtMost(lastBid, new PowerValue(-1, PowerUnit.WATT));
            } else {
                // MinTurnOn period is over, should turn off or charge now
                AllocationAnalyzer.assertDemandAtLeast(allocation, timeService, new PowerValue(0, PowerUnit.WATT));
                BidAnalyzer.assertDemandAtLeast(lastBid, new PowerValue(0, PowerUnit.WATT));
            }
        }
    }

    @Test
    public void testMinOff() {
        // make device turn on, make sure minturnon and minturnoff periods are over
        timeService.stepInTime(120000);
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        timeService.stepInTime(60000);
        b.stateOfCharge(0.5f).minOffPeriod(new Duration(1, TimeUnit.MINUTES));
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
        timeService.stepInTime(60000);
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.stateOfCharge(0.51f).minOffPeriod(new Duration(1, TimeUnit.MINUTES));
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
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
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, turnoffPrice)); // Triggers allocation
        AllocationAnalyzer.assertDemandAtMost(resourceManager.getLastAllocation(1000),
                                              timeService,
                                              new PowerValue(0, PowerUnit.WATT));

        // Now we let it run for two minutes. The SOC = 0.5, the minOffPeriod one minute, so the agent must tell the
        // device to start running again after one minute.
        for (int seconds = 0; seconds < 120; seconds++) {
            timeService.stepInTime(1000);
            // Check bid
            b.stateOfCharge(0);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID)); // Triggers bid

            // Check allocation
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE)); // Triggers allocation
            if (seconds < 60) {
                // In minturnoff period
                AllocationAnalyzer.assertNotRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
                BidAnalyzer.assertFlatBidWithValue(parent.getLastBid(agent.getId(), 1000),
                                                   new PowerValue(0, PowerUnit.WATT));
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
    @Test
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
        b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(chargeSpeedW, PowerUnit.WATT))));
        b.selfDischarge(new PowerValue(dischargeSpeedW, PowerUnit.WATT));
        b.totalCapacity(new EnergyValue(capacityKWH, EnergyUnit.KILO_WATTHOUR));
        b.minOnPeriod(new Duration(minOnHours, TimeUnit.HOURS));
        b.minOffPeriod(new Duration(0, TimeUnit.HOURS));
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));

        // if(SOC >= criticalSOC) the device should NOT turn on
        float criticalSOC = 1 - (((chargeSpeedW - dischargeSpeedW) * minOnHours) / (capacityKWH * 1000));
        // Make the device not turn on and let it discharge until SOC < criticalSOC
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        float curSOC = 1;
        float deltaSOCperHour = dischargeSpeedW / (capacityKWH * 1000);
        boolean charging = false;
        for (int minute = 0; minute < 60; minute++) {
            timeService.stepInTime(60000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 60000));
            curSOC -= deltaSOCperHour / 60f;
            b.stateOfCharge(curSOC);
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
            Allocation a = resourceManager.getLastAllocation(1000);
            if (!charging && curSOC >= criticalSOC) {
                // Should NOT turn on
                AllocationAnalyzer.assertDemandAtMost(a, timeService, new PowerValue(0, PowerUnit.WATT));
                BidAnalyzer.assertDemandAtMost(parent.getLastBid(agent.getId(), 1000),
                                               new PowerValue(0, PowerUnit.WATT));
            } else {
                // Should charge, SOC is below critical SOC
                charging = true;
                AllocationAnalyzer.assertRunningAllocation(a, timeService);
                curSOC += (chargeSpeedW / (capacityKWH * 1000)) / 60f;
                BidAnalyzer.assertDemandBid(parent.getLastBid(agent.getId(), 1000));
            }
        }

    }

    @Test
    public void testDrainPrevention() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0.1f);
        b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
        b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(2000, PowerUnit.WATT))));
        b.selfDischarge(new PowerValue(1000, PowerUnit.WATT));
        b.totalCapacity(new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));
        b.minOnPeriod(new Duration(0, TimeUnit.HOURS));
        b.minOffPeriod(new Duration(0.5, TimeUnit.HOURS));

        double criticalEneregyWH = 1000 * 0.5;
        double criticalSOC = criticalEneregyWH / 1000;
        // Make it discharge
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        Allocation a = resourceManager.getLastAllocation(1000);
        AllocationAnalyzer.assertRunningAllocation(a, timeService);

        for (float curSOC = 0; curSOC <= 1; curSOC += 0.05) {
            timeService.stepInTime(60000);
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            b.stateOfCharge(curSOC);
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            a = resourceManager.getLastAllocation(1000);
            if (curSOC < criticalSOC) {
                AllocationAnalyzer.assertRunningAllocation(a, timeService);
            } else {
                AllocationAnalyzer.assertDemandAtMost(a, timeService, new PowerValue(0, PowerUnit.WATT));
            }
        }
    }

    @Test
    public void testTargetStateOfChargeHigher() {
        float targetSOC = 1;
        long targetTime = timeService.currentTimeMillis() + 3600000;
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.target(targetSOC, new Date(targetTime));
        b.stateOfCharge(0f);
        b.selfDischarge(new PowerValue(0, PowerUnit.WATT));
        b.totalCapacity(new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));
        b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(100, PowerUnit.WATT)),
                                              new PowerConstraint(new PowerValue(1000, PowerUnit.WATT))));
        b.dischargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(100, PowerUnit.WATT)),
                                                 new PowerConstraint(new PowerValue(1000, PowerUnit.WATT))));
        for (int minute = 0; minute < 120; minute++) {
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            BidAnalyzer.assertDemandAtMost(parent.getLastBid(agent.getId(), 1000), new PowerValue(1000, PowerUnit.WATT));
            AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
            timeService.stepInTime(60000);
        }
    }

    @Test
    public void testTargetStateOfChargeLower() {
        float targetSOC = 0;
        long targetTime = timeService.currentTimeMillis() + 3600000;
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.target(targetSOC, new Date(targetTime));
        b.stateOfCharge(1);
        b.selfDischarge(new PowerValue(0, PowerUnit.WATT));
        b.totalCapacity(new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));
        b.chargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(100, PowerUnit.WATT)),
                                              new PowerConstraint(new PowerValue(1000, PowerUnit.WATT))));
        b.dischargeSpeed(new PowerConstraintList(new PowerConstraint(new PowerValue(100, PowerUnit.WATT)),
                                                 new PowerConstraint(new PowerValue(1000, PowerUnit.WATT))));
        for (int minute = 0; minute < 120; minute++) {
            b.validFrom(timeService.getDate()).validThru(new Date(timeService.currentTimeMillis() + 10000));
            resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
            agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MAXIMUM_PRICE));
            BidAnalyzer.assertDemandAtLeast(parent.getLastBid(agent.getId(), 1000), new PowerValue(-1000,
                                                                                                   PowerUnit.WATT));
            AllocationAnalyzer.assertRunningAllocation(resourceManager.getLastAllocation(1000), timeService);
            timeService.stepInTime(60000);
        }
    }

    @Test
    public void testStorageFull() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(1);
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        BidAnalyzer.assertDemandAtMost(parent.getLastBid(agent.getId(), 1000), new PowerValue(0, PowerUnit.WATT));
        AllocationAnalyzer.assertDemandAtMost(resourceManager.getLastAllocation(1000),
                                              timeService,
                                              new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testStorageEmpty() {
        StorageControlSpaceBuilder b = new StorageControlSpaceBuilder();
        b.stateOfCharge(0);
        resourceManager.updateControlSpace(b.build(APPLIANCE_ID));
        agent.updatePriceInfo(new PriceInfo(MARKET_BASIS, MINIMUM_PRICE));
        BidAnalyzer.assertDemandAtLeast(parent.getLastBid(agent.getId(), 1000), new PowerValue(0, PowerUnit.WATT));
        AllocationAnalyzer.assertDemandAtLeast(resourceManager.getLastAllocation(1000),
                                               timeService,
                                               new PowerValue(0, PowerUnit.WATT));
    }

}
