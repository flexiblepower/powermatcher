package net.powermatcher.api.testsuite;

import net.powermatcher.api.data.test.ArrayBidTest;
import net.powermatcher.api.data.test.MarketBasisTest;
import net.powermatcher.api.data.test.PriceTest;
import net.powermatcher.api.monitoring.test.EventTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ArrayBidTest.class, PriceTest.class, EventTest.class, MarketBasisTest.class })
public class ApiTestSuite {

}
