package net.powermatcher.core.testsuite;

import net.powermatcher.core.auctioneer.test.AuctioneerTest;
import net.powermatcher.core.concentrator.test.ConcentratorTest;
import net.powermatcher.core.monitoring.test.BaseObservableTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTest.class, ConcentratorTest.class, BaseObservableTest.class })
public class CoreTestSuite {

}
