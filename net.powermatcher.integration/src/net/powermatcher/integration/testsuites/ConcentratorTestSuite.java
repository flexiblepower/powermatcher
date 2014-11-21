package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.concentrator.ConcentratorResilienceTestICF;
import net.powermatcher.integration.concentrator.ConcentratorResilienceTestICQ;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ConcentratorResilienceTestICF.class, ConcentratorResilienceTestICQ.class})
public class ConcentratorTestSuite {

}
