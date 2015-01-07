package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.test.SendReceiveBidTestCBF1;
import net.powermatcher.integration.test.SendReceiveBidTestCBQ1;
import net.powermatcher.integration.test.SendReceivePriceTestCPF1;
import net.powermatcher.integration.test.SendReceivePriceTestCPQ1;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author FAN
 * @version 2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ SendReceivePriceTestCPF1.class, SendReceivePriceTestCPQ1.class, SendReceiveBidTestCBF1.class,
        SendReceiveBidTestCBQ1.class })
public class CommunicationTestSuite {
}
