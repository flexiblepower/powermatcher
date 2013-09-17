package net.powermatcher.fpai.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.powermatcher.core.scheduler.service.TimeService;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.flexiblepower.rai.values.PowerValue;

public class AllocationAnalyzer {

    private static double getWattFromDemand(Allocation allocation) {
        Element element = allocation.getEnergyProfile().get(0);
        double energyWMS = element.getEnergy().getValueAs(EnergyUnit.WATTHOUR) * 3600000;
        double durationMS = element.getDuration().getMilliseconds();
        double watt = energyWMS / durationMS;
        return watt;
    }

    public static void assertAllocationWithDemand(Allocation allocation, TimeService timeService, PowerValue demand) {
        assertNotNull(allocation);
        assertAllocationStartsNow(allocation, timeService);
        assertEquals(getWattFromDemand(allocation), demand.getValueAs(PowerUnit.WATT), 0.0001);
    }

    public static void assertDemandAtMost(Allocation allocation, TimeService timeService, PowerValue demand) {
        assertNotNull(allocation);
        assertAllocationStartsNow(allocation, timeService);
        double watt = getWattFromDemand(allocation);
        double demandWatt = demand.getValueAs(PowerUnit.WATT);
        assertTrue("Demand should be at most " + demandWatt + ", was " + watt, watt <= demandWatt);
    }

    public static void assertDemandAtLeast(Allocation allocation, TimeService timeService, PowerValue demand) {
        assertNotNull(allocation);
        assertAllocationStartsNow(allocation, timeService);
        double watt = getWattFromDemand(allocation);
        double demandWatt = demand.getValueAs(PowerUnit.WATT);
        assertTrue("Demand should be at least " + demandWatt + ", was " + watt, watt >= demandWatt);
    }

    public static void assertNotRunningAllocation(Allocation allocation, TimeService timeService) {
        assertNotNull(allocation);
        assertAllocationStartsNow(allocation, timeService);
        assertEquals(0, allocation.getEnergyProfile().get(0).getEnergy().getValue(), 0.001);
    }

    public static void assertRunningAllocation(Allocation allocation, TimeService timeService) {
        assertNotNull(allocation);
        assertAllocationStartsNow(allocation, timeService);
        assertFalse("Should be running allocation, but allocation has a demand of 0 watt",
                    allocation.getEnergyProfile().get(0).getEnergy().getValue() == 0);
    }

    public static void assertAllocationStartsNow(Allocation allocation, TimeService timeService) {
        assertNotNull(allocation);
        assertEquals(timeService.currentTimeMillis(), allocation.getStartTime().getTime());
    }

}
