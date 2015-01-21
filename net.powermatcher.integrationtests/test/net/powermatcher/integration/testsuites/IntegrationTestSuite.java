package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.oscillation.OscillationPreventionTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author FAN
 * @version 2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTestSuite.class, ConcentratorTestSuite.class, CommunicationTestSuite.class,
        OscillationPreventionTest.class })
public class IntegrationTestSuite {

}
