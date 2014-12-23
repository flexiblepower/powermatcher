package net.powermatcher.integration.testsuites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTestSuite.class, ConcentratorTestSuite.class, CommunicationTestSuite.class })
public class IntegrationTestSuite {

}
