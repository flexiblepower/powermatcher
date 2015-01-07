package net.powermatcher.core.testsuite;

import net.powermatcher.core.auctioneer.test.AuctioneerTest;
import net.powermatcher.core.auctioneer.test.ObjectiveAuctioneerTest;
import net.powermatcher.core.concentrator.test.ConcentratorTest;
import net.powermatcher.core.concentrator.test.PeakShavingConcentratorTest;
import net.powermatcher.core.monitoring.test.LogRecordTest;
import net.powermatcher.core.monitoring.test.ObservableAgentTest;
import net.powermatcher.core.sessions.test.SessionManagerTest;
import net.powermatcher.core.test.BaseDeviceAgentTest;
import net.powermatcher.core.test.BidCacheElementTest;
import net.powermatcher.core.test.BidCacheTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A testSuite class used to run all test in this eclipse project.
 * 
 * @author FAN
 * @version 2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTest.class, ObjectiveAuctioneerTest.class, ConcentratorTest.class,
        PeakShavingConcentratorTest.class, LogRecordTest.class, ObservableAgentTest.class, SessionManagerTest.class,
        BaseDeviceAgentTest.class, BidCacheElementTest.class, BidCacheTest.class })
public class CoreTestSuite {

}
