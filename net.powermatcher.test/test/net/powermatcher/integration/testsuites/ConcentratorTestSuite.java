package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.concentrator.ConcentratorResilienceTestICF;
import net.powermatcher.integration.concentrator.ConcentratorResilienceTestICQ;
import net.powermatcher.integration.concentrator.ConcentratorTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author FAN
 * @version 2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ConcentratorTest.class, ConcentratorResilienceTestICF.class, ConcentratorResilienceTestICQ.class })
public class ConcentratorTestSuite {

}
