package net.powermatcher.api.testsuite;

import net.powermatcher.api.data.test.BidTest;
import net.powermatcher.api.data.test.PriceTest;
import net.powermatcher.api.monitoring.test.EventTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BidTest.class, PriceTest.class, EventTest.class })
public class ApiTestSuite {

}
