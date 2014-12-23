package net.powermatcher.api.testsuite;

import net.powermatcher.api.data.test.ArrayBidTest;
import net.powermatcher.api.data.test.MarketBasisTest;
import net.powermatcher.api.data.test.PointBidTest;
import net.powermatcher.api.data.test.PricePointTest;
import net.powermatcher.api.data.test.PriceStepTest;
import net.powermatcher.api.data.test.PriceTest;
import net.powermatcher.api.data.test.PriceUpdateTest;
import net.powermatcher.api.monitoring.test.EventTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ArrayBidTest.class, MarketBasisTest.class, PointBidTest.class, PricePointTest.class,
        PriceStepTest.class, PriceTest.class, PriceUpdateTest.class, EventTest.class })
public class ApiTestSuite {

}
